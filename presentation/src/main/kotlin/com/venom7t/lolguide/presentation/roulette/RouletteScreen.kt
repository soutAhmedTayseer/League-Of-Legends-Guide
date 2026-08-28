package com.venom7t.lolguide.presentation.roulette

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
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
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> LoadingContent()

                state.poolSize == 0 -> EmptyContent(
                    message = uiText(R.string.roulette_empty_pool),
                )

                else -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimens.spaceLg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    val result = state.result
                    if (result == null) {
                        Text(
                            text = stringResource(R.string.roulette_prompt),
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        AsyncImage(
                            model = DataDragonUrls.championSplash(result.id),
                            contentDescription = stringResource(
                                R.string.champion_detail_portrait,
                                result.name,
                            ),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                        )
                        Text(
                            text = result.name,
                            style = AppTheme.typography.displayLarge,
                            color = AppTheme.colors.textPrimary,
                        )
                        Text(
                            text = result.title,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }

                    Button(
                        onClick = { onEvent(RouletteEvent.Rolled) },
                        shape = AppTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.primary,
                            contentColor = AppTheme.colors.onPrimary,
                        ),
                    ) {
                        Text(
                            text = stringResource(
                                if (result == null) {
                                    R.string.roulette_roll
                                } else {
                                    R.string.roulette_reroll
                                }
                            ),
                            style = AppTheme.typography.label,
                        )
                    }

                    if (result != null) {
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
    }
}
