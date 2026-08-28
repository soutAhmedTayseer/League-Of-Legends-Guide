package com.venom7t.lolguide.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.components.PatchBadge
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun HomeScreenRoot(
    onNavigateToWhatsNew: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTimers: () -> Unit,
    onNavigateToLadder: () -> Unit,
    onNavigateToFollowedSummoners: () -> Unit,
    onNavigateToGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(HomeEvent.ScreenOpened) }

    HomeScreen(
        state = state,
        onNavigateToWhatsNew = onNavigateToWhatsNew,
        onNavigateToSimulator = onNavigateToSimulator,
        onNavigateToRoulette = onNavigateToRoulette,
        onNavigateToQuiz = onNavigateToQuiz,
        onNavigateToTimers = onNavigateToTimers,
        onNavigateToLadder = onNavigateToLadder,
        onNavigateToFollowedSummoners = onNavigateToFollowedSummoners,
        onNavigateToGame = onNavigateToGame,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onNavigateToWhatsNew: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTimers: () -> Unit,
    onNavigateToLadder: () -> Unit,
    onNavigateToFollowedSummoners: () -> Unit,
    onNavigateToGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                    ) {
                        Text(
                            text = stringResource(R.string.home_title),
                            style = AppTheme.typography.titleLarge,
                            color = AppTheme.colors.textPrimary,
                        )
                        state.patchVersion?.let { PatchBadge(version = it, isStale = state.isPatchStale) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.surface),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            item {
                WhatsNewTeaser(hasChanges = state.hasWhatsNew, onClick = onNavigateToWhatsNew)
            }

            item {
                RotationCard(
                    championIds = state.freeRotationChampionIds,
                    patchVersion = state.patchVersion,
                )
            }

            item {
                Text(
                    text = stringResource(R.string.home_quick_links_title),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.primary,
                )
            }

            item {
                QuickLinkRow(
                    icon = Icons.Default.Calculate,
                    label = stringResource(R.string.simulator_open),
                    onClick = onNavigateToSimulator,
                )
            }
            item {
                QuickLinkRow(
                    icon = Icons.Default.Casino,
                    label = stringResource(R.string.roulette_open),
                    onClick = onNavigateToRoulette,
                )
            }
            item {
                QuickLinkRow(
                    icon = Icons.Default.Quiz,
                    label = stringResource(R.string.quiz_open),
                    onClick = onNavigateToQuiz,
                )
            }
            item {
                QuickLinkRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(R.string.timers_open),
                    onClick = onNavigateToTimers,
                )
            }
            item {
                QuickLinkRow(
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.ladder_open),
                    onClick = onNavigateToLadder,
                )
            }
            item {
                QuickLinkRow(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.followed_summoners_open),
                    onClick = onNavigateToFollowedSummoners,
                )
            }
            item {
                QuickLinkRow(
                    icon = Icons.Default.Quiz,
                    label = stringResource(R.string.game_hub_title),
                    onClick = onNavigateToGame,
                )
            }
        }
    }
}

@Composable
private fun WhatsNewTeaser(
    hasChanges: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.large)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.primary, AppTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(AppTheme.dimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_whats_new_teaser),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(
                    if (hasChanges) R.string.home_whats_new_open else R.string.home_whats_new_unavailable
                ),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = AppTheme.colors.accent,
        )
    }
}

/**
 * Free champion rotation needs CHAMPION-V3, a keyed Riot endpoint (Phase 4).
 * When [championIds] is null -- no key configured, or the lookup failed --
 * this states plainly what it is waiting on rather than showing nothing,
 * which would read as a bug (AGENTS.md §8.2: keyed features degrade to a
 * clear "not configured" state, not silence).
 */
@Composable
private fun RotationCard(
    championIds: List<String>?,
    patchVersion: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.large)
            .padding(AppTheme.dimens.spaceMd),
    ) {
        Text(
            text = stringResource(R.string.home_rotation_placeholder_title),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.textSecondary,
        )
        if (championIds != null && patchVersion != null) {
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.padding(top = AppTheme.dimens.spaceSm),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                items(championIds) { championId ->
                    coil3.compose.AsyncImage(
                        model = com.venom7t.lolguide.presentation.common.DataDragonUrls
                            .championIconById(patchVersion, championId),
                        contentDescription = championId,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .background(AppTheme.colors.surface, AppTheme.shapes.small),
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.home_rotation_placeholder_body),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textDisabled,
            )
        }
    }
}

@Composable
private fun QuickLinkRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.medium)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(AppTheme.dimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AppTheme.colors.accent)
        Text(text = label, style = AppTheme.typography.bodyLarge, color = AppTheme.colors.textPrimary)
    }
}
