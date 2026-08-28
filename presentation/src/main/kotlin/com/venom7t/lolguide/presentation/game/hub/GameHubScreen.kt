package com.venom7t.lolguide.presentation.game.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun GameHubScreenRoot(
    onNavigateToRound: (GameMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameHubViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(GameHubEvent.ScreenOpened) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GameHubEffect.NavigateToRound -> onNavigateToRound(effect.mode)
                GameHubEffect.NavigateBack -> onBack()
            }
        }
    }

    GameHubScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHubScreen(
    state: GameHubState,
    onEvent: (GameHubEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_hub_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(GameHubEvent.BackClicked) }) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            items(GameMode.entries) { mode ->
                GameModeCard(
                    mode = mode,
                    stats = state.stats[mode],
                    onClick = { onEvent(GameHubEvent.ModeClicked(mode)) },
                )
            }
        }
    }
}

@Composable
private fun GameModeCard(mode: GameMode, stats: GameStats?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        shape = AppTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(AppTheme.dimens.spaceMd)) {
            Text(
                text = modeTitle(mode),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = modeDescription(mode),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
            if (stats != null && stats.played > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppTheme.dimens.spaceSm),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    StatChip(stringResource(R.string.game_hub_streak, stats.currentStreak))
                    StatChip(stringResource(R.string.game_hub_best_streak, stats.bestStreak))
                    StatChip(stringResource(R.string.game_hub_win_rate, stats.winRatePercent))
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.caption,
        color = AppTheme.colors.accent,
    )
}

@Composable
private fun modeTitle(mode: GameMode): String = stringResource(
    when (mode) {
        GameMode.CLASSIC -> R.string.game_mode_classic
        GameMode.ABILITY -> R.string.game_mode_ability
        GameMode.SPLASH -> R.string.game_mode_splash
    },
)

@Composable
private fun modeDescription(mode: GameMode): String = stringResource(
    when (mode) {
        GameMode.CLASSIC -> R.string.game_mode_classic_description
        GameMode.ABILITY -> R.string.game_mode_ability_description
        GameMode.SPLASH -> R.string.game_mode_splash_description
    },
)
