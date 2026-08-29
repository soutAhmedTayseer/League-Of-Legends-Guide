package com.venom7t.lolguide.presentation.item

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.usecase.ObservePurchasableItemsUseCase
import com.venom7t.lolguide.domain.item.usecase.RefreshItemsUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.presentation.common.UiText
import com.venom7t.lolguide.presentation.common.toUiText
import org.koin.android.annotation.KoinViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ItemListState(
    val isLoading: Boolean = false,
    val items: ImmutableList<Item> = persistentListOf(),
    val query: String = "",
    val selectedTags: Set<String> = emptySet(),
    /**
     * Tags present in the cached data. Derived rather than hardcoded because
     * Riot adds and retires item tags between patches.
     */
    val availableTags: ImmutableList<String> = persistentListOf(),
    val patchVersion: String? = null,
    val isFilterSheetOpen: Boolean = false,
    val error: UiText? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && items.isEmpty() && query.isBlank() && selectedTags.isEmpty()

    val hasNoResults: Boolean
        get() = !isLoading && items.isEmpty() && (query.isNotBlank() || selectedTags.isNotEmpty())
}

sealed interface ItemListEvent {
    data object ScreenOpened : ItemListEvent
    data object Retry : ItemListEvent
    data class QueryChanged(val query: String) : ItemListEvent
    data class TagToggled(val tag: String) : ItemListEvent
    data object FiltersCleared : ItemListEvent
    data object FilterSheetOpened : ItemListEvent
    data object FilterSheetDismissed : ItemListEvent
    data class ItemClicked(val itemId: String) : ItemListEvent
}

sealed interface ItemListEffect {
    data class NavigateToDetail(val itemId: String) : ItemListEffect
}

@KoinViewModel
class ItemListViewModel (
    private val observeItems: ObservePurchasableItemsUseCase,
    private val refreshItems: RefreshItemsUseCase,
    private val resolvePatch: ResolvePatchUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    private val _state = MutableStateFlow(ItemListState())
    val state: StateFlow<ItemListState> = _state.asStateFlow()

    private val _effects = Channel<ItemListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var allItems: List<Item> = emptyList()
    private var hasStarted = false

    fun onEvent(event: ItemListEvent) {
        when (event) {
            ItemListEvent.ScreenOpened -> start()
            ItemListEvent.Retry -> load()

            is ItemListEvent.QueryChanged -> {
                _state.update { it.copy(query = event.query) }
                recomputeVisible()
            }

            is ItemListEvent.TagToggled -> {
                _state.update { current ->
                    val tags = current.selectedTags
                    current.copy(
                        selectedTags = if (event.tag in tags) tags - event.tag else tags + event.tag
                    )
                }
                recomputeVisible()
            }

            ItemListEvent.FiltersCleared -> {
                _state.update { it.copy(selectedTags = emptySet(), query = "") }
                recomputeVisible()
            }

            ItemListEvent.FilterSheetOpened -> _state.update { it.copy(isFilterSheetOpen = true) }
            ItemListEvent.FilterSheetDismissed -> _state.update { it.copy(isFilterSheetOpen = false) }

            is ItemListEvent.ItemClicked -> viewModelScope.launch {
                _effects.send(ItemListEffect.NavigateToDetail(event.itemId))
            }
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true
        observeCache()
        load()
    }

    private fun observeCache() {
        observeItems()
            .onEach { items ->
                allItems = items
                _state.update { current ->
                    current.copy(
                        availableTags = items
                            .flatMap { it.tags }
                            .distinct()
                            .sorted()
                            .toImmutableList(),
                        isLoading = current.isLoading && items.isEmpty(),
                    )
                }
                recomputeVisible()
            }
            .launchIn(viewModelScope)
    }

    private fun recomputeVisible() {
        _state.update { current ->
            val query = current.query.trim()
            val filtered = allItems.filter { item ->
                // Tags are OR-ed within the category, matching the champion
                // filter convention: picking a second tag widens the result.
                val matchesTags = current.selectedTags.isEmpty() ||
                    item.tags.any { it in current.selectedTags }
                val matchesQuery = query.isEmpty() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.plaintext.contains(query, ignoreCase = true)
                matchesTags && matchesQuery
            }
            current.copy(items = filtered.toImmutableList())
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = allItems.isEmpty(), error = null) }

            val patch = resolvePatch().getOrElse { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = if (allItems.isEmpty()) throwable.toUiText() else null,
                    )
                }
                return@launch
            }

            _state.update { it.copy(patchVersion = patch.version) }

            // Same rule as champions: only hit the network on a patch change or
            // a cold cache, rather than re-downloading the shop every visit.
            val cachedPatch = allItems.firstOrNull()?.patchVersion
            val needsRefresh = allItems.isEmpty() || cachedPatch != patch.version
            if (!needsRefresh) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            refreshItems(patch.version, locale)
                .onSuccess { _state.update { it.copy(isLoading = false, error = null) } }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = if (allItems.isEmpty()) throwable.toUiText() else null,
                        )
                    }
                }
        }
    }
}
