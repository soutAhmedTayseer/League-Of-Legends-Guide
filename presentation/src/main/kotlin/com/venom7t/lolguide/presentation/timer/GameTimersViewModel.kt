package com.venom7t.lolguide.presentation.timer

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.domain.spell.model.SummonerSpell
import com.venom7t.lolguide.domain.spell.repository.SummonerSpellRepository
import com.venom7t.lolguide.domain.timer.model.EnemyLane
import com.venom7t.lolguide.domain.timer.model.GameTimer
import com.venom7t.lolguide.domain.timer.model.GameTimerPreset
import com.venom7t.lolguide.domain.timer.model.SpellTimer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which lane/slot a spell picker or cancel-confirm dialog is acting on. */
data class SpellSlotTarget(val lane: EnemyLane, val slotIndex: Int)

@Immutable
data class GameTimersState(
    val running: ImmutableList<GameTimer> = persistentListOf(),
    val nowEpochMillis: Long = System.currentTimeMillis(),
    /** Summoner's Rift spells only, sorted by name. Empty while loading. */
    val spells: ImmutableList<SummonerSpell> = persistentListOf(),
    val patchVersion: String? = null,
    /** Two slots per lane; null means empty. */
    val laneSlots: ImmutableMap<EnemyLane, ImmutableList<SpellTimer?>> = EnemyLane.entries
        .associateWith { persistentListOf<SpellTimer?>(null, null) }
        .toImmutableMap(),
    val pickingTarget: SpellSlotTarget? = null,
    /** Gates the confirmation dialog for clearing a running spell timer (AGENTS.md §13). */
    val pendingCancelTarget: SpellSlotTarget? = null,
)

sealed interface GameTimersEvent {
    data class PresetStarted(val preset: GameTimerPreset) : GameTimersEvent
    data class TimerCancelled(val timerId: Long) : GameTimersEvent
    data class SpellSlotClicked(val target: SpellSlotTarget) : GameTimersEvent
    data object SpellPickerDismissed : GameTimersEvent
    data class SpellPicked(val spell: SummonerSpell) : GameTimersEvent
    data object SpellCancelConfirmed : GameTimersEvent
    data object SpellCancelDismissed : GameTimersEvent
}

/**
 * Objective timers run entirely offline (Phase 3 plan §"In-game timers").
 * Enemy spell timers need the summoner-spell cache for cooldowns, so this
 * is the one place the screen touches the patch layer at all. The tick loop
 * lives in the ViewModel rather than the Composable so a configuration
 * change (rotation) does not restart either kind of timer.
 */
@HiltViewModel
class GameTimersViewModel @Inject constructor(
    private val spellRepository: SummonerSpellRepository,
    private val resolvePatch: ResolvePatchUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    private val _state = MutableStateFlow(GameTimersState())
    val state: StateFlow<GameTimersState> = _state.asStateFlow()

    private var nextId = 0L

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(TICK_MILLIS)
                val now = System.currentTimeMillis()
                _state.update { current ->
                    current.copy(
                        nowEpochMillis = now,
                        // Expired timers drop off on their own tick rather
                        // than needing an explicit dismiss action.
                        running = current.running.filterNot { it.isExpired(now) }.toImmutableList(),
                        laneSlots = current.laneSlots.mapValues { (_, slots) ->
                            slots.map { slot -> slot?.takeUnless { it.isExpired(now) } }.toImmutableList()
                        }.toImmutableMap(),
                    )
                }
            }
        }
        loadSpells()
    }

    private fun loadSpells() {
        viewModelScope.launch {
            val patch = resolvePatch().getOrNull() ?: return@launch
            spellRepository.getSummonerSpells(patch.version, locale).onSuccess { spells ->
                _state.update {
                    it.copy(
                        spells = spells.filter { spell -> spell.isSummonersRift }
                            .sortedBy { spell -> spell.name }
                            .toImmutableList(),
                        patchVersion = patch.version,
                    )
                }
            }
        }
    }

    fun onEvent(event: GameTimersEvent) {
        when (event) {
            is GameTimersEvent.PresetStarted -> _state.update { current ->
                val timer = GameTimer(
                    id = nextId++,
                    preset = event.preset,
                    startedAtEpochMillis = System.currentTimeMillis(),
                )
                current.copy(running = (current.running + timer).toImmutableList())
            }

            is GameTimersEvent.TimerCancelled -> _state.update { current ->
                current.copy(
                    running = current.running.filterNot { it.id == event.timerId }.toImmutableList(),
                )
            }

            is GameTimersEvent.SpellSlotClicked -> onSlotClicked(event.target)
            GameTimersEvent.SpellPickerDismissed -> _state.update { it.copy(pickingTarget = null) }
            is GameTimersEvent.SpellPicked -> onSpellPicked(event.spell)
            GameTimersEvent.SpellCancelConfirmed -> onCancelConfirmed()
            GameTimersEvent.SpellCancelDismissed -> _state.update { it.copy(pendingCancelTarget = null) }
        }
    }

    private fun onSlotClicked(target: SpellSlotTarget) {
        val occupied = _state.value.laneSlots[target.lane]?.getOrNull(target.slotIndex) != null
        _state.update {
            if (occupied) {
                it.copy(pendingCancelTarget = target)
            } else {
                it.copy(pickingTarget = target)
            }
        }
    }

    private fun onSpellPicked(spell: SummonerSpell) {
        val target = _state.value.pickingTarget ?: return
        val timer = SpellTimer(
            lane = target.lane,
            slotIndex = target.slotIndex,
            spellId = spell.id,
            startedAtEpochMillis = System.currentTimeMillis(),
            durationSeconds = spell.cooldownSeconds.toInt(),
        )
        _state.update { current ->
            current.copy(
                pickingTarget = null,
                laneSlots = current.laneSlots.withSlot(target, timer),
            )
        }
    }

    private fun onCancelConfirmed() {
        val target = _state.value.pendingCancelTarget ?: return
        _state.update { current ->
            current.copy(
                pendingCancelTarget = null,
                laneSlots = current.laneSlots.withSlot(target, null),
            )
        }
    }

    private fun ImmutableMap<EnemyLane, ImmutableList<SpellTimer?>>.withSlot(
        target: SpellSlotTarget,
        timer: SpellTimer?,
    ): ImmutableMap<EnemyLane, ImmutableList<SpellTimer?>> {
        val updatedLane = (this[target.lane] ?: persistentListOf(null, null))
            .toMutableList()
            .apply { this[target.slotIndex] = timer }
            .toImmutableList()
        return toMutableMap().apply { this[target.lane] = updatedLane }.toImmutableMap()
    }

    private companion object {
        const val TICK_MILLIS = 500L
    }
}
