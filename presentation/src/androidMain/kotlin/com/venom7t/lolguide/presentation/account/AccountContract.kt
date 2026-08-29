package com.venom7t.lolguide.presentation.account

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.auth.model.Account
import com.venom7t.lolguide.domain.onboarding.model.PrimaryRole
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.settings.model.ThemeMode
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class AccountState(
    val account: Account? = null,
    val isSigningIn: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Set during onboarding; editable here too so it isn't a one-time choice. */
    val summonerRegion: Region? = null,
    val primaryRole: PrimaryRole? = null,
    /** The current app locale tag, e.g. "en" or "ar". Null means "follow system". */
    val languageTag: String? = null,
    /** Gates the confirm-before-logout dialog (AGENTS.md §13). */
    val pendingSignOut: Boolean = false,
    /** The pasted API key text field -- separate from [hasApiKeyOverride], which reflects what's saved. */
    val apiKeyInput: String = "",
    /** True once a user-pasted key is saved and actually in use instead of the app's own. */
    val hasApiKeyOverride: Boolean = false,
    val error: UiText? = null,
)

sealed interface AccountEvent {
    data object ScreenOpened : AccountEvent
    data object SignInClicked : AccountEvent
    data class GoogleIdTokenReceived(val idToken: String) : AccountEvent
    data object GoogleSignInFailed : AccountEvent
    data object SignOutClicked : AccountEvent
    data object SignOutConfirmed : AccountEvent
    data object SignOutCancelled : AccountEvent
    data class ThemeModeSelected(val mode: ThemeMode) : AccountEvent
    data class LanguageSelected(val languageTag: String?) : AccountEvent
    data class SummonerRegionSelected(val region: Region) : AccountEvent
    data class PrimaryRoleSelected(val role: PrimaryRole) : AccountEvent
    data class ApiKeyInputChanged(val input: String) : AccountEvent
    data object ApiKeySaveClicked : AccountEvent
    data object ApiKeyClearClicked : AccountEvent
    data object GetNewApiKeyClicked : AccountEvent
    data object BackClicked : AccountEvent
}

sealed interface AccountEffect {
    /** Tells the screen to launch Credential Manager's Google sign-in flow. */
    data object LaunchGoogleSignIn : AccountEffect
    data object NavigateBack : AccountEffect
    data object NavigateToApiKeyPortal : AccountEffect
    data class ShowSnackbar(val message: UiText) : AccountEffect
}
