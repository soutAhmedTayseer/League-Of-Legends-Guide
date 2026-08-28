package com.venom7t.lolguide.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.onboarding.model.PrimaryRole
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun OnboardingScreenRoot(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                OnboardingEffect.NavigateToHome -> onFinished()
            }
        }
    }

    OnboardingScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        bottomBar = {
            TextButton(
                onClick = { onEvent(OnboardingEvent.SkipClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.dimens.spaceMd),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.textSecondary,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceLg),
        ) {
            when (state.step) {
                OnboardingStep.WELCOME -> WelcomeStep(onEvent)
                OnboardingStep.REGION -> RegionStep(state.selectedRegion, onEvent)
                OnboardingStep.ROLE -> RoleStep(state.selectedRole, onEvent)
            }
        }
    }
}

@Composable
private fun WelcomeStep(onEvent: (OnboardingEvent) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceLg),
        ) {
            Text(
                text = stringResource(R.string.onboarding_welcome_title),
                style = AppTheme.typography.displayLarge,
                color = AppTheme.colors.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.onboarding_welcome_body),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = { onEvent(OnboardingEvent.ContinueClicked) },
                shape = AppTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primary,
                    contentColor = AppTheme.colors.onPrimary,
                ),
            ) {
                Text(text = stringResource(R.string.onboarding_continue))
            }
        }
    }
}

@Composable
private fun RegionStep(
    selected: Region?,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.onboarding_region_title),
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(vertical = AppTheme.dimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            items(Region.entries.size) { index ->
                val region = Region.entries[index]
                ChoiceChip(
                    label = region.name,
                    isSelected = region == selected,
                    onClick = { onEvent(OnboardingEvent.RegionPicked(region)) },
                )
            }
        }
    }
}

@Composable
private fun RoleStep(
    selected: PrimaryRole?,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.onboarding_role_title),
            style = AppTheme.typography.titleLarge,
            color = AppTheme.colors.textPrimary,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = AppTheme.dimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            items(PrimaryRole.entries.size) { index ->
                val role = PrimaryRole.entries[index]
                ChoiceChip(
                    label = stringResource(role.labelRes()),
                    isSelected = role == selected,
                    onClick = { onEvent(OnboardingEvent.RolePicked(role)) },
                )
            }
        }

        Button(
            onClick = { onEvent(OnboardingEvent.FinishClicked) },
            enabled = selected != null,
            shape = AppTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primary,
                contentColor = AppTheme.colors.onPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.onboarding_finish))
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isSelected) AppTheme.colors.primary else AppTheme.colors.surface,
                AppTheme.shapes.medium,
            )
            .border(
                width = AppTheme.dimens.borderWidth,
                color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.border,
                shape = AppTheme.shapes.medium,
            )
            .clickable(onClick = onClick)
            .padding(AppTheme.dimens.spaceMd),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = AppTheme.typography.label,
            color = if (isSelected) AppTheme.colors.onPrimary else AppTheme.colors.textPrimary,
        )
    }
}

private fun PrimaryRole.labelRes(): Int = when (this) {
    PrimaryRole.TOP -> R.string.role_top
    PrimaryRole.JUNGLE -> R.string.role_jungle
    PrimaryRole.MID -> R.string.role_mid
    PrimaryRole.BOTTOM -> R.string.role_bottom
    PrimaryRole.SUPPORT -> R.string.role_support
}
