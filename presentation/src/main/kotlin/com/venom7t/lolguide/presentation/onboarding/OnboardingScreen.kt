package com.venom7t.lolguide.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.onboarding.model.PrimaryRole
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.common.components.HextechFrame
import com.venom7t.lolguide.presentation.common.components.SectionRule
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppTheme.dimens.spaceMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StepDots(step = state.step)
                TextButton(onClick = { onEvent(OnboardingEvent.SkipClicked) }) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = AppTheme.dimens.spaceLg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceLg),
            ) {
                when (state.step) {
                    OnboardingStep.WELCOME -> WelcomeStep()
                    OnboardingStep.REGION -> RegionStep(state.selectedRegion, onEvent)
                    OnboardingStep.ROLE -> RoleStep(state.selectedRole, onEvent)
                }
            }

            OnboardingNavRow(state = state, onEvent = onEvent)
        }
    }
}

/** Small filled/hollow dots -- how far through onboarding this is, without a number. */
@Composable
private fun StepDots(step: OnboardingStep, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs)) {
        OnboardingStep.entries.forEach { entry ->
            val isCurrent = entry == step
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 20.dp else 8.dp, 8.dp)
                    .clip(AppTheme.shapes.pill)
                    .background(if (isCurrent) AppTheme.colors.primary else AppTheme.colors.border),
            )
        }
    }
}

/**
 * A champion splash sets the tone before any UI copy does -- the same
 * full-bleed-art-with-scrim language as Home's hero and Roulette's reveal,
 * so onboarding reads as this app rather than a generic wizard. The
 * champion itself is not a data claim (nothing about it is asserted), just
 * art, so a fixed pick is fine rather than needing the champion cache.
 */
@Composable
private fun WelcomeStep(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Data Dragon splash art is a fixed 1215x717 canvas -- fitting
                // to that ratio, rather than cropping to whatever space is
                // available, is what keeps the whole illustration visible.
                .aspectRatio(1215f / 717f)
                .clip(AppTheme.shapes.large)
                .background(AppTheme.colors.surfaceElevated),
        ) {
            AsyncImage(
                model = DataDragonUrls.championSplash(ONBOARDING_HERO_CHAMPION_ID),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
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
        }
    }
}

/** Purely decorative -- not a data claim about the champion, so a fixed pick needs no cache. */
private const val ONBOARDING_HERO_CHAMPION_ID = "Jinx"

@Composable
private fun RegionStep(
    selected: Region?,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        SectionRule(title = stringResource(R.string.onboarding_region_title))
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
                    iconChampionId = null,
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
        SectionRule(title = stringResource(R.string.onboarding_role_title))
        LazyColumn(
            contentPadding = PaddingValues(vertical = AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            items(PrimaryRole.entries.size) { index ->
                val role = PrimaryRole.entries[index]
                ChoiceChip(
                    label = stringResource(role.labelRes()),
                    isSelected = role == selected,
                    iconChampionId = role.exampleChampionId(),
                    onClick = { onEvent(OnboardingEvent.RolePicked(role)) },
                )
            }
        }
    }
}

/** Back / Next row, shared across all three steps so the control positions never shift underfoot. */
@Composable
private fun OnboardingNavRow(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLastStep = state.step == OnboardingStep.ROLE
    val nextEnabled = when (state.step) {
        OnboardingStep.WELCOME -> true
        OnboardingStep.REGION -> state.selectedRegion != null
        OnboardingStep.ROLE -> state.selectedRole != null
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        if (state.step != OnboardingStep.WELCOME) {
            OutlinedButton(
                onClick = { onEvent(OnboardingEvent.BackClicked) },
                modifier = Modifier.weight(1f),
                shape = AppTheme.shapes.medium,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_back),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.textSecondary,
                )
            }
        }

        Button(
            onClick = {
                onEvent(if (isLastStep) OnboardingEvent.FinishClicked else OnboardingEvent.ContinueClicked)
            },
            enabled = nextEnabled,
            modifier = Modifier.weight(1f),
            shape = AppTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primary,
                contentColor = AppTheme.colors.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(
                    if (isLastStep) R.string.onboarding_finish else R.string.onboarding_continue,
                ),
                style = AppTheme.typography.label,
            )
        }
    }
}

@Composable
private fun ChoiceChip(
    label: String,
    isSelected: Boolean,
    iconChampionId: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CutSurface(onClick = onClick, highlighted = isSelected, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconChampionId != null) {
                HextechFrame(
                    // Icon-cropped from the splash rather than the square
                    // portrait: no patch/champion cache exists yet this
                    // early in a fresh install, and splash art is the one
                    // unversioned asset that doesn't need either.
                    model = DataDragonUrls.championSplash(iconChampionId),
                    contentDescription = null,
                    isSelected = isSelected,
                    modifier = Modifier.size(56.dp),
                )
            }
            Text(
                text = label,
                style = AppTheme.typography.label,
                color = if (isSelected) AppTheme.colors.accent else AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
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

/** One recognizable example per role, purely illustrative -- not a claim that this is the only champion played there. */
private fun PrimaryRole.exampleChampionId(): String = when (this) {
    PrimaryRole.TOP -> "Garen"
    PrimaryRole.JUNGLE -> "LeeSin"
    PrimaryRole.MID -> "Ahri"
    PrimaryRole.BOTTOM -> "Jinx"
    PrimaryRole.SUPPORT -> "Thresh"
}
