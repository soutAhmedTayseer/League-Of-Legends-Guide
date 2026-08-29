package com.venom7t.lolguide.presentation.account

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.venom7t.lolguide.domain.onboarding.model.PrimaryRole
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.settings.model.ThemeMode
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.common.components.HextechConfirmDialog
import com.venom7t.lolguide.presentation.common.components.SectionRule
import com.venom7t.lolguide.presentation.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun AccountScreenRoot(
    onBack: () -> Unit,
    onNavigateToApiKeyPortal: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.onEvent(AccountEvent.ScreenOpened) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AccountEffect.LaunchGoogleSignIn -> coroutineScope.launch {
                    launchGoogleSignIn(context, viewModel)
                }
                AccountEffect.NavigateBack -> onBack()
                AccountEffect.NavigateToApiKeyPortal -> onNavigateToApiKeyPortal()
                is AccountEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    AccountScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

/**
 * Runs Credential Manager's Google sign-in bottom sheet and hands the
 * resulting ID token back to the ViewModel. Lives in the Composable rather
 * than the ViewModel because it needs an Android [android.content.Context]
 * for the system UI -- everything past the token itself stays in
 * [AccountViewModel] and :domain.
 */
internal suspend fun launchGoogleSignIn(
    context: android.content.Context,
    viewModel: AccountViewModel,
) {
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.google_sign_in_web_client_id))
        .build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

    try {
        val result = CredentialManager.create(context).getCredential(context, request)
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        viewModel.onEvent(AccountEvent.GoogleIdTokenReceived(googleIdTokenCredential.idToken))
    } catch (e: GetCredentialException) {
        Log.w("AccountScreen", "Google sign-in failed or was cancelled", e)
        viewModel.onEvent(AccountEvent.GoogleSignInFailed)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountState,
    onEvent: (AccountEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.account_title),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AccountEvent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                    titleContentColor = AppTheme.colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            SectionRule(title = stringResource(R.string.settings_appearance_title))
            CutSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimens.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                ) {
                    Text(
                        text = stringResource(R.string.settings_theme_label),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.textPrimary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            SelectableChip(
                                label = stringResource(mode.labelRes()),
                                selected = state.themeMode == mode,
                                onClick = { onEvent(AccountEvent.ThemeModeSelected(mode)) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.settings_language_label),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.textPrimary,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                    ) {
                        SelectableChip(
                            label = stringResource(R.string.settings_language_system),
                            selected = state.languageTag == null,
                            onClick = { onEvent(AccountEvent.LanguageSelected(null)) },
                            modifier = Modifier.weight(1f),
                        )
                        SelectableChip(
                            label = "English",
                            selected = state.languageTag == "en",
                            onClick = { onEvent(AccountEvent.LanguageSelected("en")) },
                            modifier = Modifier.weight(1f),
                        )
                        SelectableChip(
                            label = "العربية",
                            selected = state.languageTag == "ar",
                            onClick = { onEvent(AccountEvent.LanguageSelected("ar")) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            SectionRule(title = stringResource(R.string.settings_summoner_setup_title))
            SummonerSetupCard(state = state, onEvent = onEvent)

            SectionRule(title = stringResource(R.string.settings_api_key_title))
            ApiKeyCard(state = state, onEvent = onEvent)

            AccountCard(state = state, onEvent = onEvent)
        }
    }

    if (state.pendingSignOut) {
        HextechConfirmDialog(
            title = stringResource(R.string.account_sign_out_confirm_title),
            body = stringResource(R.string.account_sign_out_confirm_body),
            confirmLabel = stringResource(R.string.account_sign_out),
            onConfirm = { onEvent(AccountEvent.SignOutConfirmed) },
            onDismiss = { onEvent(AccountEvent.SignOutCancelled) },
        )
    }
}

/**
 * The region and primary role set during onboarding, editable here too so
 * they were never a one-time choice -- region is what Summoner Search
 * pre-fills, and both are what the app's personalisation (free-rotation
 * region, Home's tool ordering) reads from.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SummonerSetupCard(
    state: AccountState,
    onEvent: (AccountEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    CutSurface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            Text(
                text = stringResource(R.string.settings_region_label),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                Region.entries.forEach { region ->
                    SelectableChip(
                        label = region.name,
                        selected = state.summonerRegion == region,
                        onClick = { onEvent(AccountEvent.SummonerRegionSelected(region)) },
                    )
                }
            }

            Text(
                text = stringResource(R.string.settings_role_label),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier.padding(top = AppTheme.dimens.spaceSm),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                PrimaryRole.entries.forEach { role ->
                    SelectableChip(
                        label = stringResource(role.labelRes()),
                        selected = state.primaryRole == role,
                        onClick = { onEvent(AccountEvent.PrimaryRoleSelected(role)) },
                    )
                }
            }
        }
    }
}

private fun PrimaryRole.labelRes(): Int = when (this) {
    PrimaryRole.TOP -> R.string.role_top
    PrimaryRole.JUNGLE -> R.string.role_jungle
    PrimaryRole.MID -> R.string.role_mid
    PrimaryRole.BOTTOM -> R.string.role_bottom
    PrimaryRole.SUPPORT -> R.string.role_support
}

/**
 * Riot's development keys expire every 24 hours and this app cannot renew
 * one itself -- this is how a user keeps using keyed features (summoner
 * search, match history, ladder, and so on) between app updates once the
 * bundled key expires, without needing a new build.
 */
@Composable
private fun ApiKeyCard(
    state: AccountState,
    onEvent: (AccountEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    CutSurface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            Text(
                text = stringResource(R.string.settings_api_key_body),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )

            if (state.hasApiKeyOverride) {
                Text(
                    text = stringResource(R.string.settings_api_key_active),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.accent,
                )
            }

            OutlinedTextField(
                value = state.apiKeyInput,
                onValueChange = { onEvent(AccountEvent.ApiKeyInputChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppTheme.shapes.medium,
                placeholder = {
                    Text(
                        text = stringResource(R.string.settings_api_key_hint),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textDisabled,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    focusedBorderColor = AppTheme.colors.primary,
                    unfocusedBorderColor = AppTheme.colors.border,
                    cursorColor = AppTheme.colors.primary,
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                OutlinedButton(
                    onClick = { onEvent(AccountEvent.GetNewApiKeyClicked) },
                    modifier = Modifier.weight(1f),
                    shape = AppTheme.shapes.medium,
                ) {
                    Text(
                        text = stringResource(R.string.settings_api_key_get_new),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.accent,
                    )
                }
                Button(
                    onClick = { onEvent(AccountEvent.ApiKeySaveClicked) },
                    enabled = state.apiKeyInput.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = AppTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.primary,
                        contentColor = AppTheme.colors.onPrimary,
                    ),
                ) {
                    Text(stringResource(R.string.settings_api_key_save), style = AppTheme.typography.label)
                }
            }

            if (state.hasApiKeyOverride) {
                TextButton(onClick = { onEvent(AccountEvent.ApiKeyClearClicked) }) {
                    Text(
                        text = stringResource(R.string.settings_api_key_clear),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    state: AccountState,
    onEvent: (AccountEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val account = state.account
    CutSurface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            if (account != null && !account.isAnonymous) {
                Text(
                    text = stringResource(
                        R.string.account_signed_in_as,
                        account.displayName ?: account.email.orEmpty(),
                    ),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                )
                Button(
                    onClick = { onEvent(AccountEvent.SignOutClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.error,
                        contentColor = AppTheme.colors.onPrimary,
                    ),
                ) {
                    Text(stringResource(R.string.account_sign_out), style = AppTheme.typography.label)
                }
            } else {
                Text(
                    text = stringResource(R.string.account_anonymous),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
                Text(
                    text = stringResource(R.string.account_sign_in_benefit),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
                Button(
                    onClick = { onEvent(AccountEvent.SignInClicked) },
                    enabled = !state.isSigningIn,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.primary,
                        contentColor = AppTheme.colors.onPrimary,
                    ),
                ) {
                    if (state.isSigningIn) {
                        CircularProgressIndicator(color = AppTheme.colors.onPrimary)
                    } else {
                        Text(stringResource(R.string.account_sign_in_with_google))
                    }
                }
            }

            state.error?.let {
                Text(
                    text = it.asString(),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.error,
                )
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CutSurface(modifier = modifier, onClick = onClick, highlighted = selected) {
        Text(
            text = label,
            style = AppTheme.typography.label,
            color = if (selected) AppTheme.colors.accent else AppTheme.colors.textSecondary,
            maxLines = 1,
            modifier = Modifier.padding(
                horizontal = AppTheme.dimens.spaceMd,
                vertical = AppTheme.dimens.spaceSm,
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
}
