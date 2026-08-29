package com.venom7t.lolguide.presentation.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.spell.model.SummonerSpell
import com.venom7t.lolguide.domain.timer.model.EnemyLane
import com.venom7t.lolguide.domain.timer.model.GameTimer
import com.venom7t.lolguide.domain.timer.model.GameTimerPreset
import com.venom7t.lolguide.domain.timer.model.SpellTimer
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.components.HextechConfirmDialog
import com.venom7t.lolguide.presentation.common.components.HextechFrame
import com.venom7t.lolguide.presentation.common.components.SectionRule
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
                actions = {
                    TextButton(onClick = { onEvent(GameTimersEvent.ResetAllClicked) }) {
                        Text(
                            text = stringResource(R.string.timers_reset_all),
                            style = AppTheme.typography.label,
                            color = AppTheme.colors.error,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.background),
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
            item { SectionRule(title = stringResource(R.string.timers_presets_title)) }

            item {
                PresetGrid(onPresetClick = { onEvent(GameTimersEvent.PresetStarted(it)) })
            }

            item { SectionRule(title = stringResource(R.string.timers_running_title)) }

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

            item {
                SectionRule(
                    title = stringResource(R.string.timers_enemy_team_title),
                    modifier = Modifier.padding(top = AppTheme.dimens.spaceSm),
                )
            }

            items(EnemyLane.entries) { lane ->
                EnemyLaneRow(
                    lane = lane,
                    slots = state.laneSlots[lane],
                    spells = state.spells,
                    patchVersion = state.patchVersion,
                    nowEpochMillis = state.nowEpochMillis,
                    onSlotClick = { slotIndex ->
                        onEvent(GameTimersEvent.SpellSlotClicked(SpellSlotTarget(lane, slotIndex)))
                    },
                )
            }
        }
    }

    val pickingTarget = state.pickingTarget
    if (pickingTarget != null) {
        SpellPickerSheet(
            spells = state.spells,
            patchVersion = state.patchVersion,
            onPick = { onEvent(GameTimersEvent.SpellPicked(it)) },
            onDismiss = { onEvent(GameTimersEvent.SpellPickerDismissed) },
        )
    }

    if (state.pendingCancelTarget != null) {
        HextechConfirmDialog(
            title = stringResource(R.string.timers_clear_spell_title),
            body = stringResource(R.string.timers_clear_spell_body),
            confirmLabel = stringResource(R.string.action_remove),
            onConfirm = { onEvent(GameTimersEvent.SpellCancelConfirmed) },
            onDismiss = { onEvent(GameTimersEvent.SpellCancelDismissed) },
        )
    }

    if (state.pendingResetAll) {
        HextechConfirmDialog(
            title = stringResource(R.string.timers_reset_all_confirm_title),
            body = stringResource(R.string.timers_reset_all_confirm_body),
            confirmLabel = stringResource(R.string.timers_reset_all),
            onConfirm = { onEvent(GameTimersEvent.ResetAllConfirmed) },
            onDismiss = { onEvent(GameTimersEvent.ResetAllCancelled) },
        )
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
                            .clip(AppTheme.shapes.medium)
                            .background(AppTheme.colors.surface)
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
            .clip(AppTheme.shapes.medium)
            .background(AppTheme.colors.surface)
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

@Composable
private fun EnemyLaneRow(
    lane: EnemyLane,
    slots: List<SpellTimer?>?,
    spells: List<SummonerSpell>,
    patchVersion: String?,
    nowEpochMillis: Long,
    onSlotClick: (slotIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.medium)
            .background(AppTheme.colors.surface)
            .padding(AppTheme.dimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        Text(
            text = stringResource(lane.labelRes()),
            style = AppTheme.typography.tileLabel,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        (slots ?: listOf(null, null)).forEachIndexed { index, slot ->
            SpellSlot(
                slot = slot,
                spell = spells.firstOrNull { it.id == slot?.spellId },
                patchVersion = patchVersion,
                nowEpochMillis = nowEpochMillis,
                onClick = { onSlotClick(index) },
            )
        }
    }
}

@Composable
private fun SpellSlot(
    slot: SpellTimer?,
    spell: SummonerSpell?,
    patchVersion: String?,
    nowEpochMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (slot != null && spell != null && patchVersion != null) {
            HextechFrame(
                model = DataDragonUrls.spellIcon(patchVersion, spell.imageFileName),
                contentDescription = spell.name,
                modifier = modifier
                    .size(44.dp)
                    .clickable(onClick = onClick),
            )
            Text(
                text = slot.remainingSeconds(nowEpochMillis).formatDuration(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )
        } else {
            Box(
                modifier = modifier
                    .size(44.dp)
                    .clip(AppTheme.shapes.small)
                    .background(AppTheme.colors.surfaceElevated)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.timers_assign_spell),
                    tint = AppTheme.colors.textDisabled,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpellPickerSheet(
    spells: List<SummonerSpell>,
    patchVersion: String?,
    onPick: (SummonerSpell) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.surface,
    ) {
        Text(
            text = stringResource(R.string.timers_pick_spell),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.padding(horizontal = AppTheme.dimens.spaceMd),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            // Sheet content, not an outer LazyColumn item -- unlike PresetGrid,
            // this is the sheet's only scrollable content, so nesting is safe.
            modifier = Modifier.padding(bottom = AppTheme.dimens.spaceLg),
        ) {
            items(spells, key = { it.id }) { spell ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (patchVersion != null) {
                        HextechFrame(
                            model = DataDragonUrls.spellIcon(patchVersion, spell.imageFileName),
                            contentDescription = spell.name,
                            modifier = Modifier
                                .size(52.dp)
                                .clickable { onPick(spell) },
                        )
                    }
                    Text(
                        text = spell.name,
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private fun EnemyLane.labelRes(): Int = when (this) {
    EnemyLane.TOP -> R.string.lane_top
    EnemyLane.JUNGLE -> R.string.lane_jungle
    EnemyLane.MID -> R.string.lane_mid
    EnemyLane.BOTTOM -> R.string.lane_bottom
    EnemyLane.SUPPORT -> R.string.lane_support
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
