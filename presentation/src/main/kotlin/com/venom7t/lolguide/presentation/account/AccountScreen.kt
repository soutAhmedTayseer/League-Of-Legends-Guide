package com.venom7t.lolguide.presentation.account

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun AccountScreenRoot(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.onEvent(AccountEvent.ScreenOpened) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AccountEffect.LaunchGoogleSignIn -> coroutineScope.launch {
                    launchGoogleSignIn(context, viewModel)
                }
                AccountEffect.NavigateBack -> onBack()
            }
        }
    }

    AccountScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
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
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(AccountEvent.BackClicked) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
                .padding(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            val account = state.account
            if (account != null && !account.isAnonymous) {
                Text(
                    text = stringResource(
                        R.string.account_signed_in_as,
                        account.displayName ?: account.email.orEmpty(),
                    ),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                )
                OutlinedButton(
                    onClick = { onEvent(AccountEvent.SignOutClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.account_sign_out))
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
