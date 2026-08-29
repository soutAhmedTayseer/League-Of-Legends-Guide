package com.venom7t.lolguide.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.theme.AppTheme
import kotlinx.coroutines.launch

/**
 * The mandatory sign-in gate a fresh install lands on, before onboarding or
 * anything else. Owner decision, after an anonymous-only install lost its
 * synced favourites to an uninstall: an anonymous session is exactly the
 * failure mode that caused that, so a fresh install is routed here every
 * time rather than letting sign-in be postponed and skipped entirely.
 *
 * Deliberately has no back button and no "skip" -- [onSignedIn] is the only
 * way out, fired the moment [AccountViewModel]'s observed account stops
 * being anonymous. Reuses [AccountViewModel] and [launchGoogleSignIn] as-is;
 * only the surrounding UI differs from the optional Account screen (no
 * sign-out option makes no sense here since arriving here means there is
 * nothing to sign out of yet).
 */
@Composable
fun SignInGateScreenRoot(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.onEvent(AccountEvent.ScreenOpened) }

    LaunchedEffect(state.account) {
        val account = state.account
        if (account != null && !account.isAnonymous) onSignedIn()
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AccountEffect.LaunchGoogleSignIn -> coroutineScope.launch {
                    launchGoogleSignIn(context, viewModel)
                }
                AccountEffect.NavigateBack -> Unit // No back destination from the gate.
                AccountEffect.NavigateToApiKeyPortal -> Unit // Not offered from the gate.
                is AccountEffect.ShowSnackbar -> Unit // Not offered from the gate.
            }
        }
    }

    SignInGateScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@Composable
fun SignInGateScreen(
    state: AccountState,
    onEvent: (AccountEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppTheme.dimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceLg, Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(R.string.home_wordmark),
                style = AppTheme.typography.tileLabel,
                color = AppTheme.colors.textSecondary,
            )

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(AppTheme.shapes.medium)
                    .background(AppTheme.colors.surfaceElevated)
                    .border(AppTheme.dimens.borderWidth, AppTheme.colors.primary, AppTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(32.dp),
                )
            }

            CutSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimens.spaceLg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    Text(
                        text = stringResource(R.string.sign_in_gate_title),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = stringResource(R.string.sign_in_gate_body),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { onEvent(AccountEvent.SignInClicked) },
                        enabled = !state.isSigningIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = AppTheme.dimens.spaceSm),
                        shape = AppTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.primary,
                            contentColor = AppTheme.colors.onPrimary,
                        ),
                    ) {
                        if (state.isSigningIn) {
                            CircularProgressIndicator(
                                color = AppTheme.colors.onPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.account_sign_in_with_google),
                                style = AppTheme.typography.label,
                            )
                        }
                    }
                    state.error?.let {
                        Text(
                            text = it.asString(),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
