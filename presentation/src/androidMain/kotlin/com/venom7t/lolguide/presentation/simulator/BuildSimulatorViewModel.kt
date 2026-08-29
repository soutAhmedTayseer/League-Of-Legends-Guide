package com.venom7t.lolguide.presentation.simulator

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.venom7t.lolguide.domain.builds.usecase.GetSavedBuildUseCase
import com.venom7t.lolguide.domain.builds.usecase.SaveBuildUseCase
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.SearchChampionsUseCase
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.usecase.BuildResult
import com.venom7t.lolguide.domain.item.usecase.BuildSimulator
import com.venom7t.lolguide.domain.item.usecase.ObservePurchasableItemsUseCase
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.UiText
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.navigation.BuildSimulatorRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Which panel the item picker fills when open. Null closes it. */
sealed interface SimulatorPicker {
    data object Champion : SimulatorPicker
    data class ItemSlot(val slotIndex: Int) : SimulatorPicker
}

private const val ITEM_SLOT_COUNT = 6

@Immutable
data class BuildSimulatorState(
    val champion: Champion? = null,
    /** Fixed-size, with nulls for empty slots -- a real League inventory. */
    val items: ImmutableList<Item?> = persistentListOf<Item?>().addEmptySlots(),
    val level: Int = ChampionStatCalculator.MAX_LEVEL,
    val result: BuildResult? = null,
    val picking: SimulatorPicker? = null,
    val pickerQuery: String = "",
    val championResults: ImmutableList<Champion> = persistentListOf(),
    val itemResults: ImmutableList<Item> = persistentListOf(),
) {
    companion object {
        private fun ImmutableList<Item?>.addEmptySlots(): ImmutableList<Item?> =
            List(ITEM_SLOT_COUNT) { null }.toImmutableList()
    }
}

sealed interface BuildSimulatorEvent {
    data object ScreenOpened : BuildSimulatorEvent
    data object ChampionPickerOpened : BuildSimulatorEvent
    data class ItemSlotClicked(val slotIndex: Int) : BuildSimulatorEvent
    data object PickerDismissed : BuildSimulatorEvent
    data class PickerQueryChanged(val query: String) : BuildSimulatorEvent
    data class ChampionPicked(val championId: String) : BuildSimulatorEvent
    data class ItemPicked(val itemId: String) : BuildSimulatorEvent
    data class ItemSlotCleared(val slotIndex: Int) : BuildSimulatorEvent
    data class LevelChanged(val level: Int) : BuildSimulatorEvent
    data object SaveBuildClicked : BuildSimulatorEvent
}

sealed interface BuildSimulatorEffect {
    data class ShowSnackbar(val message: UiText) : BuildSimulatorEffect
}

class BuildSimulatorViewModel (
    savedStateHandle: SavedStateHandle,
    private val observeChampions: ObserveChampionsUseCase,
    private val searchChampions: SearchChampionsUseCase,
    private val observeItems: ObservePurchasableItemsUseCase,
    private val simulator: BuildSimulator,
    private val getSavedBuild: GetSavedBuildUseCase,
    private val saveBuild: SaveBuildUseCase,
) : ViewModel() {

    private val savedBuildId: String? = savedStateHandle.toRoute<BuildSimulatorRoute>().savedBuildId

    private val _state = MutableStateFlow(BuildSimulatorState())
    val state: StateFlow<BuildSimulatorState> = _state.asStateFlow()

    private val _effects = Channel<BuildSimulatorEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var allChampions: List<Champion> = emptyList()
    private var allItems: List<Item> = emptyList()
    private var hasStarted = false

    fun onEvent(event: BuildSimulatorEvent) {
        when (event) {
            BuildSimulatorEvent.ScreenOpened -> start()

            BuildSimulatorEvent.ChampionPickerOpened -> _state.update {
                it.copy(
                    picking = SimulatorPicker.Champion,
                    pickerQuery = "",
                    championResults = allChampions.toImmutableList(),
                )
            }

            is BuildSimulatorEvent.ItemSlotClicked -> _state.update {
                it.copy(
                    picking = SimulatorPicker.ItemSlot(event.slotIndex),
                    pickerQuery = "",
                    // Items already in the build stay pickable for a second
                    // copy (e.g. two of the same boots-tier component before
                    // upgrading), so this is not filtered against it.items.
                    itemResults = allItems.toImmutableList(),
                )
            }

            BuildSimulatorEvent.PickerDismissed -> _state.update {
                it.copy(picking = null, pickerQuery = "")
            }

            is BuildSimulatorEvent.PickerQueryChanged -> _state.update { current ->
                when (current.picking) {
                    SimulatorPicker.Champion -> current.copy(
                        pickerQuery = event.query,
                        championResults = searchChampions(allChampions, event.query)
                            .toImmutableList(),
                    )

                    is SimulatorPicker.ItemSlot -> current.copy(
                        pickerQuery = event.query,
                        itemResults = filterItems(event.query).toImmutableList(),
                    )

                    null -> current
                }
            }

            is BuildSimulatorEvent.ChampionPicked -> {
                val champion = allChampions.firstOrNull { it.id == event.championId }
                _state.update {
                    it.copy(champion = champion, picking = null, pickerQuery = "")
                }
                recompute()
            }

            is BuildSimulatorEvent.ItemPicked -> {
                val slot = (_state.value.picking as? SimulatorPicker.ItemSlot)?.slotIndex
                val item = allItems.firstOrNull { it.id == event.itemId }
                if (slot != null && item != null) {
                    _state.update { current ->
                        current.copy(
                            items = current.items.toMutableList()
                                .apply { this[slot] = item }
                                .toImmutableList(),
                            picking = null,
                            pickerQuery = "",
                        )
                    }
                    recompute()
                }
            }

            is BuildSimulatorEvent.ItemSlotCleared -> {
                _state.update { current ->
                    current.copy(
                        items = current.items.toMutableList()
                            .apply { this[event.slotIndex] = null }
                            .toImmutableList(),
                    )
                }
                recompute()
            }

            is BuildSimulatorEvent.LevelChanged -> {
                val clamped = event.level.coerceIn(
                    ChampionStatCalculator.MIN_LEVEL,
                    ChampionStatCalculator.MAX_LEVEL,
                )
                _state.update { it.copy(level = clamped) }
                recompute()
            }

            BuildSimulatorEvent.SaveBuildClicked -> persistCurrentBuild()
        }
    }

    private fun persistCurrentBuild() {
        val current = _state.value
        val champion = current.champion ?: return
        val itemIds = current.items.filterNotNull().map { it.id }
        viewModelScope.launch {
            saveBuild(championId = champion.id, itemIds = itemIds, level = current.level)
                .onSuccess {
                    _effects.send(BuildSimulatorEffect.ShowSnackbar(uiText(R.string.simulator_build_saved)))
                }
                .onFailure { throwable ->
                    _effects.send(BuildSimulatorEffect.ShowSnackbar(throwable.toUiText()))
                }
        }
    }

    /** Items whose name matches [query]. Empty query returns everything. */
    private fun filterItems(query: String): List<Item> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return allItems
        return allItems.filter { it.name.contains(trimmed, ignoreCase = true) }
    }

    private fun recompute() {
        val champion = _state.value.champion
        if (champion == null) {
            _state.update { it.copy(result = null) }
            return
        }
        val selectedItems = _state.value.items.filterNotNull()
        val result = simulator.simulate(champion, selectedItems, _state.value.level)
        _state.update { it.copy(result = result) }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true

        observeChampions()
            .onEach { allChampions = it }
            .launchIn(viewModelScope)

        observeItems()
            .onEach { allItems = it }
            .launchIn(viewModelScope)

        savedBuildId?.let { id -> loadSavedBuild(id) }
    }

    /**
     * Reloads a build saved from a previous simulator session (opened via
     * Champion Detail's saved-builds list) back into the working state.
     * Waits for the champion and item caches to have data rather than
     * reading [allChampions]/[allItems] directly, since this can run before
     * either [observeChampions] or [observeItems] above has emitted yet.
     */
    private fun loadSavedBuild(id: String) {
        viewModelScope.launch {
            val build = getSavedBuild(id) ?: return@launch
            val champions = observeChampions().first { it.isNotEmpty() }
            val items = observeItems().first { it.isNotEmpty() }

            val champion = champions.firstOrNull { it.id == build.championId } ?: return@launch
            val slots = List(ITEM_SLOT_COUNT) { index ->
                build.itemIds.getOrNull(index)?.let { itemId -> items.firstOrNull { it.id == itemId } }
            }

            _state.update {
                it.copy(champion = champion, items = slots.toImmutableList(), level = build.level)
            }
            recompute()
        }
    }
}
