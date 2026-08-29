package com.venom7t.lolguide.presentation.timer

import com.venom7t.lolguide.domain.timer.model.EnemyLane
import com.venom7t.lolguide.domain.timer.model.GameTimer
import com.venom7t.lolguide.domain.timer.model.SpellTimer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Running objective and enemy-spell timers, held outside the ViewModel so
 * they survive leaving the screen and coming back -- `popBackStack()` kills
 * [GameTimersViewModel], but a jungler mid-game does not want Baron's
 * countdown to reset just because they tapped into Champions and back.
 * Cleared only by the explicit "Reset all" action, never implicitly.
 *
 * Timers are stamped with absolute epoch time, so nothing needs to keep
 * ticking while the screen is gone -- reopening simply re-derives elapsed
 * time from the wall clock.
 */
@Singleton
class GameTimersSessionStore @Inject constructor() {
    var running: ImmutableList<GameTimer> = persistentListOf()
    var laneSlots: ImmutableMap<EnemyLane, ImmutableList<SpellTimer?>> = EnemyLane.entries
        .associateWith { persistentListOf<SpellTimer?>(null, null) }
        .toImmutableMap()

    fun resetAll() {
        running = persistentListOf()
        laneSlots = EnemyLane.entries
            .associateWith { persistentListOf<SpellTimer?>(null, null) }
            .toImmutableMap()
    }
}
