package com.venom7t.lolguide.presentation.mastery

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.mastery.model.ChampionMastery
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class MasteryState(
    val isLoading: Boolean = true,
    val patchVersion: String? = null,
    val masteries: List<ChampionMastery> = emptyList(),
    val error: UiText? = null,
)

sealed interface MasteryEvent {
    data object ScreenOpened : MasteryEvent
    data object Retry : MasteryEvent
    data object BackClicked : MasteryEvent
}

sealed interface MasteryEffect {
    data object NavigateBack : MasteryEffect
}
