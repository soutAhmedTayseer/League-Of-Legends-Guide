package com.venom7t.lolguide.presentation.roulette

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun RouletteScreenRoot(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouletteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(RouletteEvent.ScreenOpened)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RouletteEffect.NavigateToDetail -> onNavigateToDetail(effect.championId)
                RouletteEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    RouletteScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouletteScreen(
    state: RouletteState,
    onEvent: (RouletteEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(RouletteEvent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.champion_detail_back),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.roulette_title),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingContent()

                state.poolSize == 0 -> Box(
                    modifier = Modifier.fillMaxSize().padding(AppTheme.dimens.spaceMd),
                ) {
                    CutSurface(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(AppTheme.dimens.spaceLg)) {
                            EmptyContent(message = uiText(R.string.roulette_empty_pool))
                        }
                    }
                }

                else -> RouletteResult(state = state, onEvent = onEvent)
            }
        }
    }
}

/**
 * The result is a full-bleed splash, same language as Home's hero: the art
 * fills the frame, a scrim resolving to the page background carries the
 * name so it stays legible over any splash, and rolling again cross-fades
 * rather than hard-cutting so consecutive rolls read as one continuous
 * reveal instead of a flicker.
 */
@Composable
private fun RouletteResult(
    state: RouletteState,
    onEvent: (RouletteEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = state.result,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                fadeIn(tween(ROLL_TRANSITION_MILLIS)) togetherWith fadeOut(tween(ROLL_TRANSITION_MILLIS))
            },
            label = "roulette_result",
        ) { result ->
            if (result != null) {
                RouletteSplash(result = result)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.background),
                )
            }
        }

        if (state.result == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.dimens.spaceLg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.roulette_prompt),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            Button(
                onClick = { onEvent(RouletteEvent.Rolled) },
                shape = AppTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primary,
                    contentColor = AppTheme.colors.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    modifier = Modifier.height(18.dp),
                )
                Text(
                    text = stringResource(
                        if (state.result == null) R.string.roulette_roll else R.string.roulette_reroll,
                    ),
                    style = AppTheme.typography.label,
                    modifier = Modifier.padding(start = AppTheme.dimens.spaceXs),
                )
            }

            if (state.result != null) {
                OutlinedButton(
                    onClick = { onEvent(RouletteEvent.ViewChampionClicked) },
                    shape = AppTheme.shapes.medium,
                ) {
                    Text(
                        text = stringResource(R.string.roulette_view_champion),
                        style = AppTheme.typography.label,
                        color = AppTheme.colors.accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun RouletteSplash(result: Champion, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = DataDragonUrls.championSplash(result.id),
            contentDescription = stringResource(R.string.champion_detail_portrait, result.name),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.4f to Color.Transparent,
                        0.72f to AppTheme.colors.background.copy(alpha = 0.94f),
                        1f to AppTheme.colors.background,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start = AppTheme.dimens.spaceLg,
                    end = AppTheme.dimens.spaceLg,
                    // Clears the two-button stack RouletteResult overlays at
                    // the bottom of the same Box -- title text sitting under
                    // "Roll again" was the bug here, not a stylistic margin.
                    bottom = 176.dp,
                ),
        ) {
            Text(
                text = result.name,
                style = AppTheme.typography.displayLarge,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = result.title,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

private const val ROLL_TRANSITION_MILLIS = 220
