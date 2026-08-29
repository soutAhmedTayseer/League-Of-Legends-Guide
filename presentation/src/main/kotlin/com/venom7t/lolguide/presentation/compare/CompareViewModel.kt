package com.venom7t.lolguide.presentation.compare

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.champion.usecase.CompareChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.ChampionComparison
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.SearchChampionsUseCase
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

/** Which side of the comparison the picker is currently choosing for. */
enum class CompareSlot { LEFT, RIGHT }

@Immutable
data class CompareState(
    val isLoading: Boolean = true,
    val left: Champion? = null,
    val right: Champion? = null,
    val level: Int = ChampionStatCalculator.MAX_LEVEL,
    val comparison: ChampionComparison? = null,
    /** Non-null while the champion picker is open, naming the slot it fills. */
    val pickingFor: CompareSlot? = null,
    val pickerQuery: String = "",
    val pickerResults: ImmutableList<Champion> = persistentListOf(),
)

sealed interface CompareEvent {
    data object ScreenOpened : CompareEvent
    data object BackClicked : CompareEvent
    data class PickerOpened(val slot: CompareSlot) : CompareEvent
    data object PickerDismissed : CompareEvent
    data class PickerQueryChanged(val query: String) : CompareEvent
    data class ChampionPicked(val championId: String) : CompareEvent
    data class LevelChanged(val level: Int) : CompareEvent
    data object Swapped : CompareEvent
}

sealed interface CompareEffect {
    data object NavigateBack : CompareEffect
}

@KoinViewModel
class CompareViewModel (
    private val observeChampions: ObserveChampionsUseCase,
    private val searchChampions: SearchChampionsUseCase,
    private val compareChampions: CompareChampionsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CompareState())
    val state: StateFlow<CompareState> = _state.asStateFlow()

    private val _effects = Channel<CompareEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var allChampions: List<Champion> = emptyList()
    private var hasStarted = false

    fun onEvent(event: CompareEvent) {
        when (event) {
            CompareEvent.ScreenOpened -> start()

            CompareEvent.BackClicked -> viewModelScope.launch {
                _effects.send(CompareEffect.NavigateBack)
            }

            is CompareEvent.PickerOpened -> _state.update {
                it.copy(
                    pickingFor = event.slot,
                    pickerQuery = "",
                    pickerResults = allChampions.excludingOppositeSlot(event.slot, it).toImmutableList(),
                )
            }

            CompareEvent.PickerDismissed -> _state.update {
                it.copy(pickingFor = null, pickerQuery = "")
            }

            is CompareEvent.PickerQueryChanged -> _state.update { current ->
                val matches = searchChampions(allChampions, event.query)
                current.copy(
                    pickerQuery = event.query,
                    pickerResults = matches.excludingOppositeSlot(current.pickingFor, current).toImmutableList(),
                )
            }

            is CompareEvent.ChampionPicked -> pick(event.championId)

            is CompareEvent.LevelChanged -> _state.update { current ->
                val clamped = event.level.coerceIn(
                    ChampionStatCalculator.MIN_LEVEL,
                    ChampionStatCalculator.MAX_LEVEL,
                )
                current.copy(level = clamped).withComparison()
            }

            CompareEvent.Swapped -> _state.update { current ->
                current.copy(left = current.right, right = current.left).withComparison()
            }
        }
    }

    /**
     * Drops whichever champion already occupies the *other* slot -- picking
     * the same champion on both sides would compare it against itself, which
     * is never a useful comparison.
     */
    private fun List<Champion>.excludingOppositeSlot(slot: CompareSlot?, state: CompareState): List<Champion> {
        val opposite = when (slot) {
            CompareSlot.LEFT -> state.right
            CompareSlot.RIGHT -> state.left
            null -> null
        } ?: return this
        return filterNot { it.id == opposite.id }
    }

    private fun pick(championId: String) {
        val champion = allChampions.firstOrNull { it.id == championId } ?: return
        _state.update { current ->
            val updated = when (current.pickingFor) {
                CompareSlot.LEFT -> current.copy(left = champion)
                CompareSlot.RIGHT -> current.copy(right = champion)
                null -> current
            }
            updated.copy(pickingFor = null, pickerQuery = "").withComparison()
        }
    }

    /**
     * Recomputes the comparison, or clears it when a side is missing.
     *
     * Comparing needs both sides, so a half-filled screen shows the pickers
     * rather than a table of one champion's numbers.
     */
    private fun CompareState.withComparison(): CompareState {
        val leftChampion = left
        val rightChampion = right
        return copy(
            comparison = if (leftChampion != null && rightChampion != null) {
                compareChampions(leftChampion, rightChampion, level)
            } else {
                null
            }
        )
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true

        observeChampions()
            .onEach { champions ->
                allChampions = champions
                _state.update { it.copy(isLoading = false).withComparison() }
            }
            .launchIn(viewModelScope)
    }
}
