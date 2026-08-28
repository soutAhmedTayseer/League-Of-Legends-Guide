package com.example.lolguide.presentation.champion.detail

import androidx.compose.runtime.Immutable
import com.example.lolguide.domain.champion.model.Champion
import com.example.lolguide.domain.champion.model.ChampionDetail
import com.example.lolguide.presentation.common.UiText

@Immutable
data class ChampionDetailState(
    val isLoading: Boolean = true,
    val champion: Champion? = null,
    val detail: ChampionDetail? = null,
    val error: UiText? = null,
) {
    /**
     * The patch to label the screen with.
     *
     * Read from the champion rather than passed in separately, and only
     * non-null once both halves have loaded, so the badge can never claim a
     * patch the displayed numbers did not come from (AGENTS.md §1).
     */
    val patchVersion: String? get() = champion?.patchVersion

    /**
     * Guards against the one mismatch this screen can produce: header stats
     * cached on an older patch beside abilities fetched on a newer one.
     */
    val hasPatchMismatch: Boolean
        get() = champion != null && detail != null &&
            champion.patchVersion != detail.patchVersion
}

sealed interface ChampionDetailEvent {
    data object ScreenOpened : ChampionDetailEvent
    data object Retry : ChampionDetailEvent
    data object BackClicked : ChampionDetailEvent
}

sealed interface ChampionDetailEffect {
    data object NavigateBack : ChampionDetailEffect
    data class ShowSnackbar(val message: UiText) : ChampionDetailEffect
}
