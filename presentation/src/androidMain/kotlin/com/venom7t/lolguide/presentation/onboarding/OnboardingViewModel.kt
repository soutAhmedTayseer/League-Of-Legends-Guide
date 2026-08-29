package com.venom7t.lolguide.presentation.onboarding

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.onboarding.model.PrimaryRole
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.onboarding.repository.OnboardingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep { WELCOME, REGION, ROLE }

@Immutable
data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val selectedRegion: Region? = null,
    val selectedRole: PrimaryRole? = null,
)

sealed interface OnboardingEvent {
    data object ContinueClicked : OnboardingEvent
    data object BackClicked : OnboardingEvent
    data object SkipClicked : OnboardingEvent
    data class RegionPicked(val region: Region) : OnboardingEvent
    data class RolePicked(val role: PrimaryRole) : OnboardingEvent
    data object FinishClicked : OnboardingEvent
}

sealed interface OnboardingEffect {
    data object NavigateToHome : OnboardingEffect
}

/**
 * Neither choice here gates anything (AGENTS.md domain notice on
 * OnboardingPreferences): skipping at any step is always available and
 * leaves the rest of the app fully functional with neutral defaults.
 */
class OnboardingViewModel (
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.ContinueClicked -> advance()

            OnboardingEvent.BackClicked -> goBack()

            OnboardingEvent.SkipClicked -> finish()

            is OnboardingEvent.RegionPicked -> {
                _state.update { it.copy(selectedRegion = event.region) }
                viewModelScope.launch { onboardingRepository.setRegion(event.region) }
            }

            is OnboardingEvent.RolePicked -> {
                _state.update { it.copy(selectedRole = event.role) }
                viewModelScope.launch { onboardingRepository.setPrimaryRole(event.role) }
            }

            OnboardingEvent.FinishClicked -> finish()
        }
    }

    private fun advance() {
        val next = when (_state.value.step) {
            OnboardingStep.WELCOME -> OnboardingStep.REGION
            OnboardingStep.REGION -> OnboardingStep.ROLE
            OnboardingStep.ROLE -> null
        }
        if (next == null) {
            finish()
        } else {
            _state.update { it.copy(step = next) }
        }
    }

    private fun goBack() {
        val previous = when (_state.value.step) {
            OnboardingStep.WELCOME -> null
            OnboardingStep.REGION -> OnboardingStep.WELCOME
            OnboardingStep.ROLE -> OnboardingStep.REGION
        }
        if (previous != null) {
            _state.update { it.copy(step = previous) }
        }
    }

    private fun finish() {
        viewModelScope.launch {
            onboardingRepository.markOnboardingComplete()
            _effects.send(OnboardingEffect.NavigateToHome)
        }
    }
}
