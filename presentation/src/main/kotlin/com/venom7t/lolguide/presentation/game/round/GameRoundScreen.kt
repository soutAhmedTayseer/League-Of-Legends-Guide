package com.venom7t.lolguide.presentation.game.round

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.South
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.venom7t.lolguide.domain.game.model.Clue
import com.venom7t.lolguide.domain.game.model.ClueAttribute
import com.venom7t.lolguide.domain.game.model.ClueState
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GuessResult
import com.venom7t.lolguide.domain.game.model.RoundOutcome
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.HextechFrame
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.common.components.SectionRule
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
                title = {
                    Text(
                        text = stringResource(modeTitleResource(state.mode)),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(GameRoundEvent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                actions = {
                    if (!state.isFinished && !state.isLoading) {
                        TextButton(onClick = { onEvent(GameRoundEvent.GiveUpClicked) }) {
                            Text(
                                text = stringResource(R.string.game_round_give_up),
                                style = AppTheme.typography.label,
                                color = AppTheme.colors.error,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
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
                item { RoundPrompt(state) }

                item {
                    Text(
                        text = stringResource(R.string.game_round_guess_count, state.guesses.size),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.textSecondary,
                    )
                }

                if (state.isFinished) {
                    item { OutcomeCard(state) }
                } else {
                    item { GuessInput(state, onEvent) }
                }

                if (state.guesses.isNotEmpty()) {
                    item { ClueLegend() }
                    item { ClueGrid(guesses = state.guesses) }
                }
            }
        }
    }

    if (state.pendingGiveUp) {
        AlertDialog(
            onDismissRequest = { onEvent(GameRoundEvent.GiveUpCancelled) },
            containerColor = AppTheme.colors.surface,
            title = {
                Text(
                    text = stringResource(R.string.game_round_give_up_confirm_title),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.game_round_give_up_confirm_body),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { onEvent(GameRoundEvent.GiveUpConfirmed) }) {
                    Text(
                        text = stringResource(R.string.game_round_give_up),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(GameRoundEvent.GiveUpCancelled) }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.textSecondary,
                    )
                }
            },
        )
    }
}

@Composable
private fun RoundPrompt(state: GameRoundState) {
    when (state.mode) {
        GameMode.CLASSIC -> Unit

        GameMode.ABILITY -> {
            if (state.patchVersion != null && state.abilityIconFileName != null) {
                HextechFrame(
                    model = DataDragonUrls.spellIcon(state.patchVersion, state.abilityIconFileName),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                )
            }
        }

        GameMode.SPLASH -> {
            state.answerChampionId?.let { championId ->
                val splashUrl = DataDragonUrls.championSplash(championId)
                // Wider each wrong guess: cropping less of the image is what
                // "zooms out" -- more context, not less, on a miss. Clamped by
                // splashZoomStep so it stops widening once fully revealed,
                // since there is no longer a guess limit to naturally cap it.
                val ratio = if (state.isFinished) {
                    16f / 9f
                } else {
                    (3f - state.splashZoomStep * 0.4f).coerceAtLeast(1f)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio)
                        .clip(AppTheme.shapes.large),
                ) {
                    AsyncImage(
                        model = splashUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppTheme.colors.surfaceElevated),
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
            shape = AppTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppTheme.colors.textPrimary,
                unfocusedTextColor = AppTheme.colors.textPrimary,
                focusedBorderColor = AppTheme.colors.primary,
                unfocusedBorderColor = AppTheme.colors.border,
            ),
        )
        if (state.suggestions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = AppTheme.dimens.spaceSm),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                items(state.suggestions) { champion ->
                    CutSurface(onClick = { onEvent(GameRoundEvent.SuggestionSelected(champion)) }) {
                        Text(
                            text = champion.name,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.padding(
                                horizontal = AppTheme.dimens.spaceMd,
                                vertical = AppTheme.dimens.spaceSm,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutcomeCard(state: GameRoundState) {
    val isWin = state.outcome == RoundOutcome.WON
    CutSurface(highlighted = isWin, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppTheme.dimens.spaceMd)) {
            Text(
                text = stringResource(
                    when (state.outcome) {
                        RoundOutcome.WON -> R.string.game_round_won
                        else -> R.string.game_round_gave_up
                    },
                ),
                style = AppTheme.typography.titleMedium,
                color = if (isWin) AppTheme.colors.accent else AppTheme.colors.error,
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

/**
 * The clue grid: one column per [ClueAttribute] plus the champion, one row
 * per guess (newest first). Header and rows share a single horizontal
 * scroll state so the columns cannot drift out of alignment with each
 * other -- giving each row its own scroll state was the bug to avoid here.
 */
@Composable
private fun ClueGrid(guesses: List<GuessResult>, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
        ) {
            GridHeaderCell(stringResource(R.string.game_round_column_champion))
            GridHeaderCell(stringResource(R.string.game_round_column_role))
            GridHeaderCell(stringResource(R.string.game_round_column_resource))
            GridHeaderCell(stringResource(R.string.game_round_column_damage))
            GridHeaderCell(stringResource(R.string.game_round_column_difficulty))
            GridHeaderCell(stringResource(R.string.game_round_column_range))
        }
        Spacer(Modifier.height(AppTheme.dimens.spaceXs))
        guesses.asReversed().forEach { guess ->
            Row(
                modifier = Modifier
                    .padding(bottom = AppTheme.dimens.spaceXs)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
            ) {
                GridCell {
                    HextechFrame(
                        model = DataDragonUrls.championIcon(guess.patchVersion, guess.imageFileName),
                        contentDescription = guess.championName,
                        modifier = Modifier.size(GRID_CELL_SIZE),
                        isSelected = guess.isCorrect,
                    )
                }
                guess.clues.forEach { clue -> ClueCell(clue) }
            }
        }
    }
}

private val GRID_CELL_SIZE = 60.dp

@Composable
private fun GridHeaderCell(text: String) {
    Box(
        modifier = Modifier.width(GRID_CELL_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = AppTheme.typography.eyebrow,
            color = AppTheme.colors.primary,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun GridCell(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.width(GRID_CELL_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun ClueCell(clue: Clue) {
    val (background, icon) = when (clue.state) {
        ClueState.MATCH -> AppTheme.colors.success to null
        ClueState.PARTIAL -> AppTheme.colors.warning to null
        ClueState.MISS -> AppTheme.colors.error to null
        ClueState.HIGHER -> AppTheme.colors.error to Icons.Default.North
        ClueState.LOWER -> AppTheme.colors.error to Icons.Default.South
    }
    GridCell {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(AppTheme.shapes.small)
                .background(background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.onPrimary,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = clue.displayValues.firstOrNull().orEmpty(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.onPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun ClueLegend() {
    CutSurface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
        ) {
            LegendRow(AppTheme.colors.success, stringResource(R.string.game_round_legend_match))
            LegendRow(AppTheme.colors.warning, stringResource(R.string.game_round_legend_partial))
            LegendRow(AppTheme.colors.error, stringResource(R.string.game_round_legend_miss))
            LegendRow(AppTheme.colors.error, stringResource(R.string.game_round_legend_higher), Icons.Default.North)
            LegendRow(AppTheme.colors.error, stringResource(R.string.game_round_legend_lower), Icons.Default.South)
        }
    }
}

@Composable
private fun LegendRow(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm)) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(AppTheme.shapes.small)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = AppTheme.colors.onPrimary, modifier = Modifier.size(12.dp))
            }
        }
        Text(text = label, style = AppTheme.typography.caption, color = AppTheme.colors.textSecondary)
    }
}

private fun modeTitleResource(mode: GameMode): Int = when (mode) {
    GameMode.CLASSIC -> R.string.game_mode_classic
    GameMode.ABILITY -> R.string.game_mode_ability
    GameMode.SPLASH -> R.string.game_mode_splash
}
