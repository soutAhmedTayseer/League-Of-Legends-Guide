package com.venom7t.lolguide.presentation.champion.detail

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionDetail
import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.champion.model.ScaledStats
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class ChampionDetailState(
    val isLoading: Boolean = true,
    val champion: Champion? = null,
    val detail: ChampionDetail? = null,
    val isFavourite: Boolean = false,
    /** Which skin the splash carousel is showing. Index into [ChampionDetail.skins]. */
    val selectedSkinIndex: Int = 0,
    val level: Int = ChampionStatCalculator.MIN_LEVEL,
    /**
     * Stats at [level]. Null until the champion loads.
     *
     * Everything in here is **computed**, not shipped by Riot, and the UI is
     * required to label it as such (AGENTS.md §1).
     */
    val scaledStats: ScaledStats? = null,
    /** Gates the confirmation dialog for un-favouriting (AGENTS.md §13). */
    val pendingFavouriteRemoval: Boolean = false,
    val error: UiText? = null,
) {
    val patchVersion: String? get() = champion?.patchVersion

    val hasPatchMismatch: Boolean
        get() = champion != null && detail != null &&
            champion.patchVersion != detail.patchVersion

    /** True when the slider is off its default, so the derived-value notice shows. */
    val isShowingDerivedStats: Boolean
        get() = level > ChampionStatCalculator.MIN_LEVEL
}

sealed interface ChampionDetailEvent {
    data object ScreenOpened : ChampionDetailEvent
    data object Retry : ChampionDetailEvent
    data object BackClicked : ChampionDetailEvent
    data class LevelChanged(val level: Int) : ChampionDetailEvent
    data class SkinSelected(val index: Int) : ChampionDetailEvent
    data object FavouriteClicked : ChampionDetailEvent
    data object FavouriteRemovalConfirmed : ChampionDetailEvent
    data object FavouriteRemovalCancelled : ChampionDetailEvent
}

sealed interface ChampionDetailEffect {
    data object NavigateBack : ChampionDetailEffect
    data class ShowSnackbar(val message: UiText) : ChampionDetailEffect
}
