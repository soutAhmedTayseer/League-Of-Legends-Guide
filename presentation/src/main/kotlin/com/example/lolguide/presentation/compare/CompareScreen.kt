package com.example.lolguide.presentation.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.lolguide.domain.champion.model.Champion
import com.example.lolguide.domain.champion.model.ChampionStatCalculator
import com.example.lolguide.domain.champion.usecase.ChampionComparison
import com.example.lolguide.presentation.R
import com.example.lolguide.presentation.common.DataDragonUrls
import com.example.lolguide.presentation.common.components.EmptyContent
import com.example.lolguide.presentation.common.components.LoadingContent
import com.example.lolguide.presentation.common.uiText
import com.example.lolguide.presentation.theme.AppTheme

@Composable
fun CompareScreenRoot(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompareViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(CompareEvent.ScreenOpened) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CompareEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    CompareScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    state: CompareState,
    onEvent: (CompareEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(CompareEvent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.champion_detail_back),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.compare_title),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
                },
                actions = {
                    if (state.left != null && state.right != null) {
                        IconButton(onClick = { onEvent(CompareEvent.Swapped) }) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = stringResource(R.string.compare_swap),
                                tint = AppTheme.colors.textSecondary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surface,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                LoadingContent()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppTheme.dimens.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                    ) {
                        SlotPicker(
                            champion = state.left,
                            placeholder = stringResource(R.string.compare_pick_left),
                            onClick = { onEvent(CompareEvent.PickerOpened(CompareSlot.LEFT)) },
                            modifier = Modifier.weight(1f),
                        )
                        SlotPicker(
                            champion = state.right,
                            placeholder = stringResource(R.string.compare_pick_right),
                            onClick = { onEvent(CompareEvent.PickerOpened(CompareSlot.RIGHT)) },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    val comparison = state.comparison
                    if (comparison == null) {
                        EmptyContent(message = uiText(R.string.compare_empty))
                    } else {
                        LevelSlider(
                            level = state.level,
                            onLevelChanged = { onEvent(CompareEvent.LevelChanged(it)) },
                        )
                        ComparisonTable(comparison = comparison)
                    }
                }
            }
        }

        if (state.pickingFor != null) {
            ChampionPickerSheet(
                query = state.pickerQuery,
                results = state.pickerResults,
                onQueryChange = { onEvent(CompareEvent.PickerQueryChanged(it)) },
                onPick = { onEvent(CompareEvent.ChampionPicked(it)) },
                onDismiss = { onEvent(CompareEvent.PickerDismissed) },
            )
        }
    }
}

@Composable
private fun SlotPicker(
    champion: Champion?,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(AppTheme.colors.surface, AppTheme.shapes.medium)
            .border(
                width = AppTheme.dimens.borderWidth,
                color = AppTheme.colors.border,
                shape = AppTheme.shapes.medium,
            )
            .clickable(onClick = onClick)
            .padding(AppTheme.dimens.spaceSm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
    ) {
        if (champion == null) {
            Text(
                text = placeholder,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textDisabled,
                textAlign = TextAlign.Center,
            )
        } else {
            AsyncImage(
                model = DataDragonUrls.championIcon(
                    version = champion.patchVersion,
                    imageFileName = champion.imageFileName,
                ),
                contentDescription = stringResource(
                    R.string.champion_detail_portrait,
                    champion.name,
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(AppTheme.dimens.championThumb)
                    .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
            )
            Text(
                text = champion.name,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.compare_change),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.accent,
            )
        }
    }
}

@Composable
private fun LevelSlider(
    level: Int,
    onLevelChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.level_label, level),
            style = AppTheme.typography.statValue,
            color = AppTheme.colors.textPrimary,
        )
        Slider(
            value = level.toFloat(),
            onValueChange = { onLevelChanged(it.toInt()) },
            valueRange = ChampionStatCalculator.MIN_LEVEL.toFloat()..
                ChampionStatCalculator.MAX_LEVEL.toFloat(),
            steps = ChampionStatCalculator.MAX_LEVEL - ChampionStatCalculator.MIN_LEVEL - 1,
            colors = SliderDefaults.colors(
                thumbColor = AppTheme.colors.primary,
                activeTrackColor = AppTheme.colors.primary,
                inactiveTrackColor = AppTheme.colors.surfaceElevated,
            ),
        )
    }
}

@Composable
private fun ComparisonTable(
    comparison: ChampionComparison,
    modifier: Modifier = Modifier,
) {
    val left = comparison.leftStats
    val right = comparison.rightStats

    val rows = listOf(
        stringResource(R.string.stat_health) to (left.hp to right.hp),
        stringResource(R.string.stat_attack_damage) to (left.attackDamage to right.attackDamage),
        stringResource(R.string.stat_armor) to (left.armor to right.armor),
        stringResource(R.string.stat_magic_resist) to (left.spellBlock to right.spellBlock),
        stringResource(R.string.stat_attack_speed) to (left.attackSpeed to right.attackSpeed),
        stringResource(R.string.stat_move_speed) to (left.moveSpeed to right.moveSpeed),
        stringResource(R.string.stat_attack_range) to (left.attackRange to right.attackRange),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
    ) {
        rows.forEach { (label, values) ->
            ComparisonRow(label = label, leftValue = values.first, rightValue = values.second)
        }

        // These are computed from base stats and growth, not published by
        // Riot, and the screen says so (AGENTS.md §1).
        Text(
            text = stringResource(R.string.derived_values_notice),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textDisabled,
            modifier = Modifier.padding(top = AppTheme.dimens.spaceSm),
        )
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    leftValue: Double,
    rightValue: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.small)
            .padding(AppTheme.dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The higher value is highlighted. Higher is not always better --
        // there is no "better" without context -- so this marks the larger
        // number rather than claiming a winner.
        Text(
            text = leftValue.formatForCompare(),
            style = AppTheme.typography.statValue,
            color = if (leftValue > rightValue) {
                AppTheme.colors.primary
            } else {
                AppTheme.colors.textSecondary
            },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
        )
        Text(
            text = label,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textDisabled,
            modifier = Modifier.weight(1.4f),
            textAlign = TextAlign.Center,
        )
        Text(
            text = rightValue.formatForCompare(),
            style = AppTheme.typography.statValue,
            color = if (rightValue > leftValue) {
                AppTheme.colors.primary
            } else {
                AppTheme.colors.textSecondary
            },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChampionPickerSheet(
    query: String,
    results: List<Champion>,
    onQueryChange: (String) -> Unit,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = AppTheme.shapes.medium,
                textStyle = AppTheme.typography.bodyMedium,
                placeholder = {
                    Text(
                        text = stringResource(R.string.champion_list_search_hint),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textDisabled,
                    )
                },
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = AppTheme.dimens.spaceXl),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
            ) {
                items(items = results, key = { it.id }) { champion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(champion.id) }
                            .padding(AppTheme.dimens.spaceSm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                    ) {
                        AsyncImage(
                            model = DataDragonUrls.championIcon(
                                version = champion.patchVersion,
                                imageFileName = champion.imageFileName,
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(AppTheme.dimens.abilityIcon)
                                .background(
                                    AppTheme.colors.surfaceElevated,
                                    AppTheme.shapes.small,
                                ),
                        )
                        Text(
                            text = champion.name,
                            style = AppTheme.typography.bodyLarge,
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

private fun Double.formatForCompare(): String =
    if (this < 10.0) {
        (Math.round(this * 100) / 100.0).toString()
    } else {
        Math.round(this).toString()
    }
