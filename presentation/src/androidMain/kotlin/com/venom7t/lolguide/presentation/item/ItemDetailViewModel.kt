package com.venom7t.lolguide.presentation.item

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.venom7t.lolguide.domain.common.AppError
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.repository.ItemRepository
import com.venom7t.lolguide.domain.item.usecase.BuildPath
import com.venom7t.lolguide.domain.item.usecase.GetBuildPathUseCase
import com.venom7t.lolguide.domain.item.usecase.GoldEfficiency
import com.venom7t.lolguide.domain.item.usecase.GoldEfficiencyCalculator
import com.venom7t.lolguide.domain.item.usecase.ObservePurchasableItemsUseCase
import com.venom7t.lolguide.presentation.common.UiText
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.navigation.ItemDetailRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ItemDetailState(
    val isLoading: Boolean = true,
    val item: Item? = null,
    val components: ImmutableList<Item> = persistentListOf(),
    val buildsInto: ImmutableList<Item> = persistentListOf(),
    val efficiency: GoldEfficiency? = null,
    val error: UiText? = null,
) {
    val patchVersion: String? get() = item?.patchVersion

    val hasBuildPath: Boolean get() = components.isNotEmpty() || buildsInto.isNotEmpty()
}

sealed interface ItemDetailEvent {
    data object ScreenOpened : ItemDetailEvent
    data object Retry : ItemDetailEvent
    data object BackClicked : ItemDetailEvent
    data class RelatedItemClicked(val itemId: String) : ItemDetailEvent
}

sealed interface ItemDetailEffect {
    data object NavigateBack : ItemDetailEffect
    data class NavigateToItem(val itemId: String) : ItemDetailEffect
}

class ItemDetailViewModel (
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository,
    private val getBuildPath: GetBuildPathUseCase,
    private val observeItems: ObservePurchasableItemsUseCase,
    private val efficiencyCalculator: GoldEfficiencyCalculator,
) : ViewModel() {

    private val itemId: String = savedStateHandle.toRoute<ItemDetailRoute>().itemId

    private val _state = MutableStateFlow(ItemDetailState())
    val state: StateFlow<ItemDetailState> = _state.asStateFlow()

    private val _effects = Channel<ItemDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: ItemDetailEvent) {
        when (event) {
            ItemDetailEvent.ScreenOpened -> {
                if (!hasStarted) {
                    hasStarted = true
                    load()
                }
            }

            ItemDetailEvent.Retry -> load()

            ItemDetailEvent.BackClicked -> viewModelScope.launch {
                _effects.send(ItemDetailEffect.NavigateBack)
            }

            is ItemDetailEvent.RelatedItemClicked -> viewModelScope.launch {
                _effects.send(ItemDetailEffect.NavigateToItem(event.itemId))
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val item = itemRepository.getCachedItem(itemId)
            if (item == null) {
                _state.update {
                    it.copy(isLoading = false, error = AppError.NotFound(itemId).toUiText())
                }
                return@launch
            }

            val path: BuildPath = getBuildPath(item)

            // The price table is derived from the whole cached item set on this
            // patch, not a hardcoded list, so it re-derives itself whenever
            // Riot re-prices a component.
            val allItems = observeItems().first()
            val statValues = efficiencyCalculator.deriveStatGoldValues(allItems)
            val efficiency = if (statValues.isEmpty) {
                null
            } else {
                efficiencyCalculator.calculate(item, statValues)
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    item = item,
                    components = path.components.toImmutableList(),
                    buildsInto = path.buildsInto.toImmutableList(),
                    efficiency = efficiency,
                    error = null,
                )
            }
        }
    }
}
