package com.venom7t.lolguide.presentation.timer

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.timer.model.GameTimer
import com.venom7t.lolguide.domain.timer.model.GameTimerPreset
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun GameTimersScreenRoot(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameTimersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GameTimersScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTimersScreen(
    state: GameTimersState,
    onEvent: (GameTimersEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.champion_detail_back),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.timers_title),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
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
                Text(
                    text = stringResource(R.string.timers_presets_title),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.primary,
                )
            }

            item {
                PresetGrid(onPresetClick = { onEvent(GameTimersEvent.PresetStarted(it)) })
            }

            item {
                Text(
                    text = stringResource(R.string.timers_running_title),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.primary,
                )
            }

            if (state.running.isEmpty()) {
                item { EmptyContent(message = uiText(R.string.timers_none_running)) }
            } else {
                items(items = state.running, key = { it.id }) { timer ->
                    RunningTimerRow(
                        timer = timer,
                        nowEpochMillis = state.nowEpochMillis,
                        onCancel = { onEvent(GameTimersEvent.TimerCancelled(timer.id)) },
                    )
                }
            }
        }
    }
}

private const val PRESET_GRID_COLUMNS = 2

@Composable
private fun PresetGrid(
    onPresetClick: (GameTimerPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    // A plain Row/Column grid rather than LazyVerticalGrid: four fixed,
    // never-scrolling presets don't need laziness, and a LazyVerticalGrid
    // nested inside the outer LazyColumn's item {} gets measured with an
    // infinite height constraint regardless of userScrollEnabled, which
    // Compose throws on (IllegalStateException: "Vertically scrollable
    // component was measured with an infinity maximum height constraints").
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        GameTimerPreset.entries.chunked(PRESET_GRID_COLUMNS).forEach { rowPresets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                rowPresets.forEach { preset ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(AppTheme.colors.surface, AppTheme.shapes.medium)
                            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.medium)
                            .clickable { onPresetClick(preset) }
                            .padding(AppTheme.dimens.spaceMd),
                    ) {
                        Text(
                            text = stringResource(preset.labelRes()),
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.textPrimary,
                        )
                        Text(
                            text = preset.durationSeconds.formatDuration(),
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RunningTimerRow(
    timer: GameTimer,
    nowEpochMillis: Long,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = timer.remainingSeconds(nowEpochMillis)
    val progress = if (timer.durationSeconds == 0) {
        0f
    } else {
        remaining.toFloat() / timer.durationSeconds.toFloat()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.medium)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.medium)
            .padding(AppTheme.dimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(timer.preset.labelRes()),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = remaining.formatDuration(),
                style = AppTheme.typography.statValue,
                color = if (remaining <= 10) AppTheme.colors.warning else AppTheme.colors.accent,
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = AppTheme.colors.primary,
                trackColor = AppTheme.colors.surfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppTheme.dimens.spaceXs),
            )
        }
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.timers_cancel),
                tint = AppTheme.colors.textSecondary,
            )
        }
    }
}

private fun GameTimerPreset.labelRes(): Int = when (this) {
    GameTimerPreset.BARON -> R.string.timer_baron
    GameTimerPreset.DRAGON -> R.string.timer_dragon
    GameTimerPreset.HERALD -> R.string.timer_herald
    GameTimerPreset.WARD -> R.string.timer_ward
}

private fun Int.formatDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%d:%02d".format(minutes, seconds)
}
