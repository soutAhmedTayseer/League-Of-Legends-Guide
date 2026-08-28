package com.venom7t.lolguide.presentation.game.round

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.game.model.Clue
import com.venom7t.lolguide.domain.game.model.ClueState
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GuessResult
import com.venom7t.lolguide.domain.game.model.RoundOutcome
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun GameRoundScreenRoot(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameRoundViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(GameRoundEvent.ScreenOpened) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                GameRoundEffect.NavigateBack -> onBack()
            }
        }
    }

    GameRoundScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRoundScreen(
    state: GameRoundState,
    onEvent: (GameRoundEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(modeTitleResource(state.mode).let { stringResource(it) }) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(GameRoundEvent.BackClicked) }) {
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
        when {
            state.isLoading -> LoadingContent(modifier = Modifier.padding(padding))
            state.error != null -> ErrorContent(
                message = state.error,
                onRetry = { onEvent(GameRoundEvent.ScreenOpened) },
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
            ) {
                item {
                    RoundPrompt(state)
                }

                item {
                    Text(
                        text = stringResource(R.string.game_round_guesses_remaining, state.guessesRemaining),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.textSecondary,
                    )
                }

                if (state.isFinished) {
                    item { OutcomeCard(state) }
                } else {
                    item { GuessInput(state, onEvent) }
                }

                items(state.guesses.reversed()) { guess -> GuessRow(guess) }
            }
        }
    }
}

@Composable
private fun RoundPrompt(state: GameRoundState) {
    when (state.mode) {
        GameMode.CLASSIC -> Unit
        GameMode.ABILITY -> {
            if (state.patchVersion != null && state.abilityIconFileName != null) {
                AsyncImage(
                    model = DataDragonUrls.spellIcon(state.patchVersion, state.abilityIconFileName),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.medium),
                )
            }
        }
        GameMode.SPLASH -> {
            state.answerChampionId?.let { championId ->
                val splashUrl = DataDragonUrls.championSplash(championId)
                // Wider each wrong guess: cropping less of the image is what
                // "zooms out" -- more context, not less, on a miss.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (state.isFinished) 16f / 9f else (3f - state.splashZoomStep * 0.4f).coerceAtLeast(1f)),
                ) {
                    AsyncImage(
                        model = splashUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.medium),
                    )
                }
            }
        }
    }
}

@Composable
private fun GuessInput(state: GameRoundState, onEvent: (GameRoundEvent) -> Unit) {
    Column {
        OutlinedTextField(
            value = state.query,
            onValueChange = { onEvent(GameRoundEvent.QueryChanged(it)) },
            label = { Text(stringResource(R.string.game_round_guess_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppTheme.colors.textPrimary,
                unfocusedTextColor = AppTheme.colors.textPrimary,
            ),
        )
        if (state.suggestions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = AppTheme.dimens.spaceSm),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                items(state.suggestions) { champion ->
                    Card(
                        onClick = { onEvent(GameRoundEvent.SuggestionSelected(champion)) },
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                        shape = AppTheme.shapes.medium,
                    ) {
                        Text(
                            text = champion.name,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.padding(AppTheme.dimens.spaceMd),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutcomeCard(state: GameRoundState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceElevated),
        shape = AppTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
        ) {
            Text(
                text = stringResource(
                    if (state.outcome == RoundOutcome.WON) R.string.game_round_won else R.string.game_round_lost,
                ),
                style = AppTheme.typography.titleMedium,
                color = if (state.outcome == RoundOutcome.WON) AppTheme.colors.accent else AppTheme.colors.error,
            )
            state.revealedAnswerName?.let { name ->
                Text(
                    text = stringResource(R.string.game_round_answer_was, name),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary,
                )
            }
            state.stats?.let { stats ->
                Text(
                    text = stringResource(R.string.game_hub_streak, stats.currentStreak),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun GuessRow(guess: GuessResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        shape = AppTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(AppTheme.dimens.spaceSm)) {
            Text(
                text = guess.championName,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppTheme.dimens.spaceXs),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
            ) {
                guess.clues.forEach { clue -> ClueChip(clue) }
            }
        }
    }
}

@Composable
private fun ClueChip(clue: Clue) {
    val color = when (clue.state) {
        ClueState.MATCH -> AppTheme.colors.accent
        ClueState.PARTIAL -> Color(0xFFC9A227)
        ClueState.MISS, ClueState.HIGHER, ClueState.LOWER -> AppTheme.colors.textDisabled
    }
    Box(
        modifier = Modifier
            .background(color, AppTheme.shapes.small)
            .padding(horizontal = AppTheme.dimens.spaceXs, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = clue.displayValues.firstOrNull().orEmpty(),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.onPrimary,
        )
    }
}

private fun modeTitleResource(mode: GameMode): Int = when (mode) {
    GameMode.CLASSIC -> R.string.game_mode_classic
    GameMode.ABILITY -> R.string.game_mode_ability
    GameMode.SPLASH -> R.string.game_mode_splash
}
