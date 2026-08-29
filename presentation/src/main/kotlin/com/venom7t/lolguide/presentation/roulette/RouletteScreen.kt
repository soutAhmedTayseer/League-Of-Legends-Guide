package com.venom7t.lolguide.presentation.roulette

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
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
import kotlinx.coroutines.delay

@Composable
fun RouletteScreenRoot(
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RouletteViewModel = koinViewModel(),
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
 * The result shows the splash art whole, framed like every other piece of
 * art in the app -- a full-bleed crop was cutting off most of the
 * illustration on a portrait screen, and "premium" here means showing the
 * artist's full composition in a border, not spilling it edge to edge.
 *
 * A roll spins through [RouletteState.spinSequence] like a slot machine --
 * each frame swap slows down towards the end, so the wheel visibly settles
 * on the landing champion instead of just cutting to it.
 */
@Composable
private fun RouletteResult(
    state: RouletteState,
    onEvent: (RouletteEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var displayed by remember { mutableStateOf(state.result) }
    var isSpinning by remember { mutableStateOf(false) }

    LaunchedEffect(state.spinSequence) {
        val frames = state.spinSequence
        if (frames.isEmpty()) return@LaunchedEffect
        isSpinning = true
        frames.forEachIndexed { index, champion ->
            displayed = champion
            // Eases out: fast flicker at the start, slowing to a stop on the
            // last frame -- a constant rate reads as a list scrolling, not a
            // wheel landing.
            val progress = index.toFloat() / (frames.size - 1).coerceAtLeast(1)
            val delayMillis = (SPIN_MIN_FRAME_MILLIS + progress * progress * SPIN_MAX_EXTRA_MILLIS).toLong()
            delay(delayMillis)
        }
        isSpinning = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppTheme.dimens.spaceLg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = displayed,
                transitionSpec = {
                    fadeIn(tween(ROLL_TRANSITION_MILLIS)) togetherWith fadeOut(tween(ROLL_TRANSITION_MILLIS))
                },
                label = "roulette_result",
            ) { result ->
                if (result != null) {
                    RouletteSplash(result = result)
                } else {
                    Text(
                        text = stringResource(R.string.roulette_prompt),
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.spaceLg),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            Button(
                onClick = { onEvent(RouletteEvent.Rolled) },
                enabled = !isSpinning,
                modifier = Modifier.weight(1f),
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
                    enabled = !isSpinning,
                    modifier = Modifier.weight(1f),
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
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Data Dragon splash art is a fixed 1215x717 canvas -- fitting
                // to that ratio (rather than cropping to the screen's own)
                // is what keeps the whole illustration inside the frame.
                .aspectRatio(SPLASH_ASPECT_RATIO)
                .clip(AppTheme.shapes.large)
                .background(AppTheme.colors.surfaceElevated)
                .border(
                    width = AppTheme.dimens.borderWidth,
                    color = AppTheme.colors.primary,
                    shape = AppTheme.shapes.large,
                ),
        ) {
            AsyncImage(
                model = DataDragonUrls.championSplash(result.id),
                contentDescription = stringResource(R.string.champion_detail_portrait, result.name),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = result.name,
                style = AppTheme.typography.displayLarge,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = result.title,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val SPLASH_ASPECT_RATIO = 1215f / 717f

private const val ROLL_TRANSITION_MILLIS = 220
private const val SPIN_MIN_FRAME_MILLIS = 40
private const val SPIN_MAX_EXTRA_MILLIS = 260
