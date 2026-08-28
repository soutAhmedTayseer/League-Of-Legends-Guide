package com.venom7t.lolguide.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.onboarding.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Decides the app's start destination once, before the NavHost is created.
 *
 * The NavHost needs a single, immediate `startDestination` -- it cannot be
 * changed after first composition -- so onboarding completion has to be known
 * before the NavHost exists at all, not discovered mid-navigation. This
 * exists purely to gate that one read; nothing else in the app needs it.
 */
@HiltViewModel
class AppStartViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AppStartState>(AppStartState.Loading)
    val state: StateFlow<AppStartState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val hasCompleted = onboardingRepository.observePreferences().first().hasCompletedOnboarding
            _state.update { AppStartState.Ready(hasCompletedOnboarding = hasCompleted) }
        }
    }
}

sealed interface AppStartState {
    data object Loading : AppStartState
    data class Ready(val hasCompletedOnboarding: Boolean) : AppStartState
}
