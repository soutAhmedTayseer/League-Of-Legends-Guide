package com.venom7t.lolguide.presentation.account

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.auth.usecase.ObserveAccountUseCase
import com.venom7t.lolguide.domain.auth.usecase.SignInWithGoogleUseCase
import com.venom7t.lolguide.domain.auth.usecase.SignOutUseCase
import com.venom7t.lolguide.domain.onboarding.repository.OnboardingRepository
import com.venom7t.lolguide.domain.settings.repository.SettingsRepository
import com.venom7t.lolguide.domain.sync.usecase.SyncOnStartUseCase
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.common.uiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel (
    private val observeAccount: ObserveAccountUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signOut: SignOutUseCase,
    private val syncOnStart: SyncOnStartUseCase,
    private val settingsRepository: SettingsRepository,
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    private val _effects = Channel<AccountEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: AccountEvent) {
        when (event) {
            AccountEvent.ScreenOpened -> start()

            AccountEvent.SignInClicked -> viewModelScope.launch {
                _state.update { it.copy(error = null) }
                _effects.send(AccountEffect.LaunchGoogleSignIn)
            }

            is AccountEvent.GoogleIdTokenReceived -> viewModelScope.launch {
                _state.update { it.copy(isSigningIn = true, error = null) }
                signInWithGoogle(event.idToken)
                    .onSuccess {
                        // The app-start sync pull (LolGuideApplication.onCreate)
                        // already ran once, before this sign-in existed --
                        // against whatever anonymous UID was current at that
                        // instant. Signing in can switch to a *different*,
                        // pre-existing UID (the collision path in
                        // AuthRepositoryImpl.linkOrSignInWithGoogle), so the
                        // favourites/followed-summoners already sitting in
                        // Firestore under that UID would otherwise never get
                        // pulled down locally until some later cold start.
                        syncOnStart()
                        _state.update { it.copy(isSigningIn = false) }
                    }
                    .onFailure { throwable ->
                        _state.update { it.copy(isSigningIn = false, error = throwable.toUiText()) }
                    }
            }

            AccountEvent.GoogleSignInFailed -> _state.update {
                it.copy(isSigningIn = false, error = uiText(R.string.account_sign_in_error))
            }

            AccountEvent.SignOutClicked -> _state.update { it.copy(pendingSignOut = true) }
            AccountEvent.SignOutCancelled -> _state.update { it.copy(pendingSignOut = false) }
            AccountEvent.SignOutConfirmed -> viewModelScope.launch {
                _state.update { it.copy(pendingSignOut = false) }
                signOut()
            }

            is AccountEvent.ThemeModeSelected -> viewModelScope.launch {
                settingsRepository.setThemeMode(event.mode)
            }

            is AccountEvent.LanguageSelected -> {
                // Applying this recreates every Activity on its own -- no
                // effect/navigation needed, the OS does it for us.
                AppCompatDelegate.setApplicationLocales(
                    if (event.languageTag == null) {
                        LocaleListCompat.getEmptyLocaleList()
                    } else {
                        LocaleListCompat.forLanguageTags(event.languageTag)
                    },
                )
                _state.update { it.copy(languageTag = event.languageTag) }
            }

            is AccountEvent.ApiKeyInputChanged -> _state.update { it.copy(apiKeyInput = event.input) }

            AccountEvent.ApiKeySaveClicked -> viewModelScope.launch {
                val key = _state.value.apiKeyInput.trim()
                if (key.isBlank()) return@launch
                settingsRepository.setApiKeyOverride(key)
                _state.update { it.copy(apiKeyInput = "", hasApiKeyOverride = true) }
                _effects.send(AccountEffect.ShowSnackbar(uiText(R.string.settings_api_key_saved)))
            }

            AccountEvent.ApiKeyClearClicked -> viewModelScope.launch {
                settingsRepository.setApiKeyOverride(null)
                _state.update { it.copy(hasApiKeyOverride = false) }
            }

            AccountEvent.GetNewApiKeyClicked -> viewModelScope.launch {
                _effects.send(AccountEffect.NavigateToApiKeyPortal)
            }

            is AccountEvent.SummonerRegionSelected -> viewModelScope.launch {
                onboardingRepository.setRegion(event.region)
            }

            is AccountEvent.PrimaryRoleSelected -> viewModelScope.launch {
                onboardingRepository.setPrimaryRole(event.role)
            }

            AccountEvent.BackClicked -> viewModelScope.launch {
                _effects.send(AccountEffect.NavigateBack)
            }
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true
        observeAccount()
            .onEach { account -> _state.update { it.copy(account = account) } }
            .launchIn(viewModelScope)

        settingsRepository.observeThemeMode()
            .onEach { mode -> _state.update { it.copy(themeMode = mode) } }
            .launchIn(viewModelScope)

        settingsRepository.observeApiKeyOverride()
            .onEach { override -> _state.update { it.copy(hasApiKeyOverride = !override.isNullOrBlank()) } }
            .launchIn(viewModelScope)

        onboardingRepository.observePreferences()
            .onEach { prefs ->
                _state.update { it.copy(summonerRegion = prefs.region, primaryRole = prefs.primaryRole) }
            }
            .launchIn(viewModelScope)

        val currentLocales = AppCompatDelegate.getApplicationLocales()
        _state.update { it.copy(languageTag = currentLocales.toLanguageTags().takeIf { tag -> tag.isNotBlank() }) }
    }
}
