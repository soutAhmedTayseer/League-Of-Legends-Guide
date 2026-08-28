package com.venom7t.lolguide.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.auth.usecase.ObserveAccountUseCase
import com.venom7t.lolguide.domain.auth.usecase.SignInWithGoogleUseCase
import com.venom7t.lolguide.domain.auth.usecase.SignOutUseCase
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.common.uiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val observeAccount: ObserveAccountUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val signOut: SignOutUseCase,
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
                    .onSuccess { _state.update { it.copy(isSigningIn = false) } }
                    .onFailure { throwable ->
                        _state.update { it.copy(isSigningIn = false, error = throwable.toUiText()) }
                    }
            }

            AccountEvent.GoogleSignInFailed -> _state.update {
                it.copy(isSigningIn = false, error = uiText(R.string.account_sign_in_error))
            }

            AccountEvent.SignOutClicked -> viewModelScope.launch { signOut() }

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
    }
}
