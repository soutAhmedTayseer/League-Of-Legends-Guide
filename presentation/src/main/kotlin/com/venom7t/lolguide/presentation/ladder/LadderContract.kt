package com.venom7t.lolguide.presentation.ladder

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.ladder.model.LadderEntry
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class LadderState(
    val isLoading: Boolean = true,
    /** True only for a pull-to-refresh re-fetch, once entries are already showing. */
    val isRefreshing: Boolean = false,
    val region: Region = Region.EUNE,
    val entries: List<LadderEntry> = emptyList(),
    val patchVersion: String? = null,
    /**
     * Names/icons resolved for the top [LadderViewModel.RESOLVE_COUNT] rows
     * only, keyed by puuid -- see LadderViewModel.resolveTopEntries.
     */
    val resolvedProfiles: Map<String, Summoner> = emptyMap(),
    val error: UiText? = null,
)

sealed interface LadderEvent {
    data object ScreenOpened : LadderEvent
    data object Retry : LadderEvent
    data class RegionSelected(val region: Region) : LadderEvent
    data object BackClicked : LadderEvent
}

sealed interface LadderEffect {
    data object NavigateBack : LadderEffect
}
