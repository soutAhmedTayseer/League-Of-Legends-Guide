package com.venom7t.lolguide.presentation.simulator

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.SearchChampionsUseCase
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.usecase.BuildResult
import com.venom7t.lolguide.domain.item.usecase.BuildSimulator
import com.venom7t.lolguide.domain.item.usecase.ObservePurchasableItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

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
}

@HiltViewModel
class BuildSimulatorViewModel @Inject constructor(
    private val observeChampions: ObserveChampionsUseCase,
    private val searchChampions: SearchChampionsUseCase,
    private val observeItems: ObservePurchasableItemsUseCase,
    private val simulator: BuildSimulator,
) : ViewModel() {

    private val _state = MutableStateFlow(BuildSimulatorState())
    val state: StateFlow<BuildSimulatorState> = _state.asStateFlow()

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
    }
}
