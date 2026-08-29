package com.venom7t.lolguide.presentation.voiceline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.theme.AppTheme

/**
 * Embedded in the champion detail screen (Phase 3 plan §Voice lines).
 *
 * Coverage is best-effort: [VoiceLinePlayerViewModel] confirms every line
 * with a real request before offering it, so this either shows real, tested
 * play buttons or an honest "not available" message -- never a button that
 * might 404 when tapped.
 */
@Composable
fun VoiceLinePanel(
    championId: String,
    modifier: Modifier = Modifier,
    viewModel: VoiceLinePlayerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(championId) {
        viewModel.onEvent(VoiceLinePlayerEvent.Requested(championId))
    }

    // Stop playback if the panel leaves composition (e.g. the user navigates
    // away) rather than letting audio keep playing behind another screen.
    DisposableEffect(Unit) {
        onDispose { viewModel.onEvent(VoiceLinePlayerEvent.StopClicked) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.voice_lines_title),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.primary,
        )

        when {
            state.isLoading -> Unit // Voice lines are a below-the-fold extra; no spinner needed.

            !state.isAvailable -> Text(
                text = stringResource(R.string.voice_lines_unavailable),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textDisabled,
            )

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                ) {
                    items(items = state.lines, key = { it.audioUrl }) { line ->
                        val index = state.lines.indexOf(line)
                        val isPlaying = state.playingLineIndex == index

                        Row(
                            modifier = Modifier
                                .background(AppTheme.colors.surface, AppTheme.shapes.pill)
                                .padding(horizontal = AppTheme.dimens.spaceSm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(
                                        if (isPlaying) {
                                            VoiceLinePlayerEvent.StopClicked
                                        } else {
                                            VoiceLinePlayerEvent.LineClicked(index)
                                        }
                                    )
                                },
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = stringResource(
                                        if (isPlaying) R.string.voice_line_stop else R.string.voice_line_play,
                                        line.label,
                                    ),
                                    tint = AppTheme.colors.accent,
                                )
                            }
                            Text(
                                text = line.label,
                                style = AppTheme.typography.caption,
                                color = AppTheme.colors.textSecondary,
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.voice_lines_best_effort_note),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textDisabled,
                )
            }
        }
    }
}
