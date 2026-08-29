package com.venom7t.lolguide.presentation.lptracker

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.lptracker.model.LpSnapshot
import com.venom7t.lolguide.domain.summoner.model.RankedQueue

@Immutable
data class LpHistoryState(
    val queueType: RankedQueue = RankedQueue.SOLO_DUO,
    /** Newest first, as returned by ObserveLpHistoryUseCase. */
    val snapshots: List<LpSnapshot> = emptyList(),
) {
    /** Empty until the tracker has polled at least twice for this queue. */
    val hasHistory: Boolean get() = snapshots.size >= 2
}

sealed interface LpHistoryEvent {
    data object ScreenOpened : LpHistoryEvent
    data class QueueSelected(val queueType: RankedQueue) : LpHistoryEvent
    data object BackClicked : LpHistoryEvent
}

sealed interface LpHistoryEffect {
    data object NavigateBack : LpHistoryEffect
}
