package com.venom7t.lolguide.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.auth.usecase.EnsureSignedInUseCase
import com.venom7t.lolguide.domain.auth.usecase.ObserveAccountUseCase
import com.venom7t.lolguide.domain.onboarding.repository.OnboardingRepository
import com.venom7t.lolguide.domain.settings.repository.SettingsRepository
import com.venom7t.lolguide.presentation.common.FirstRunGate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Decides the app's start destination once, before the NavHost is created.
 *
 * The NavHost needs a single, immediate `startDestination` -- it cannot be
 * changed after first composition -- so onboarding completion (and, since
 * the Google sign-in addendum, whether the session is still anonymous) has
 * to be known before the NavHost exists at all, not discovered mid-navigation.
 *
 * Google sign-in is mandatory (owner decision): an anonymous session is
 * exactly what caused synced data to become unreachable after an uninstall,
 * so a fresh install is routed to the sign-in gate before anything else,
 * every time, rather than letting the user postpone it and lose data again.
 */
class AppStartViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val ensureSignedIn: EnsureSignedInUseCase,
    private val observeAccount: ObserveAccountUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AppStartState>(AppStartState.Loading)
    val state: StateFlow<AppStartState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // A baseline anonymous session must exist before observeAccount()
            // has anything to report -- this is the same bootstrap
            // SyncOnStartUseCase performs, just needed a little earlier here
            // since the sign-in gate itself depends on knowing isAnonymous.
            ensureSignedIn()

            val account = observeAccount().first()
            val hasCompleted = onboardingRepository.observePreferences().first().hasCompletedOnboarding

            // Decided once, here, before any real screen mounts -- see
            // FirstRunGate's doc comment for why this lives outside DI.
            val hasCompletedFirstRun = settingsRepository.observeHasCompletedFirstRun().first()
            FirstRunGate.setIsFirstRun(!hasCompletedFirstRun)
            if (!hasCompletedFirstRun) {
                settingsRepository.markFirstRunCompleted()
            }

            _state.update {
                AppStartState.Ready(
                    needsGoogleSignIn = account == null || account.isAnonymous,
                    hasCompletedOnboarding = hasCompleted,
                )
            }
        }
    }
}

sealed interface AppStartState {
    data object Loading : AppStartState
    data class Ready(val needsGoogleSignIn: Boolean, val hasCompletedOnboarding: Boolean) : AppStartState
}
