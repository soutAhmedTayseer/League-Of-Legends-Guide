package com.venom7t.lolguide.presentation.account

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.auth.model.Account
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class AccountState(
    val account: Account? = null,
    val isSigningIn: Boolean = false,
    val error: UiText? = null,
)

sealed interface AccountEvent {
    data object ScreenOpened : AccountEvent
    data object SignInClicked : AccountEvent
    data class GoogleIdTokenReceived(val idToken: String) : AccountEvent
    data object GoogleSignInFailed : AccountEvent
    data object SignOutClicked : AccountEvent
    data object BackClicked : AccountEvent
}

sealed interface AccountEffect {
    /** Tells the screen to launch Credential Manager's Google sign-in flow. */
    data object LaunchGoogleSignIn : AccountEffect
    data object NavigateBack : AccountEffect
}
