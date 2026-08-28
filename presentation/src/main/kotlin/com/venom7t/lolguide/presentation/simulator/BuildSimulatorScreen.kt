package com.venom7t.lolguide.presentation.simulator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.usecase.BuildResult
import com.venom7t.lolguide.domain.item.usecase.DpsAssumption
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun BuildSimulatorScreenRoot(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BuildSimulatorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.onEvent(BuildSimulatorEvent.ScreenOpened) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BuildSimulatorEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    BuildSimulatorScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildSimulatorScreen(
    state: BuildSimulatorState,
    onEvent: (BuildSimulatorEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        text = stringResource(R.string.simulator_title),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
                },
                actions = {
                    if (state.champion != null) {
                        IconButton(onClick = { onEvent(BuildSimulatorEvent.SaveBuildClicked) }) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(R.string.simulator_save_build),
                                tint = AppTheme.colors.textPrimary,
                            )
                        }
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
            item {
                ChampionSlot(
                    champion = state.champion,
                    onClick = { onEvent(BuildSimulatorEvent.ChampionPickerOpened) },
                )
            }

            item {
                ItemGrid(
                    items = state.items,
                    onSlotClick = { onEvent(BuildSimulatorEvent.ItemSlotClicked(it)) },
                    onSlotClear = { onEvent(BuildSimulatorEvent.ItemSlotCleared(it)) },
                )
            }

            if (state.champion == null) {
                item { EmptyContent(message = uiText(R.string.simulator_empty)) }
            } else {
                item {
                    LevelRow(
                        level = state.level,
                        onLevelChange = { onEvent(BuildSimulatorEvent.LevelChanged(it)) },
                    )
                }

                state.result?.let { result ->
                    item { ResultsBlock(result = result) }
                }
            }
        }
    }

    when (state.picking) {
        is SimulatorPicker.Champion -> ChampionPickerSheet(
            query = state.pickerQuery,
            results = state.championResults,
            onQueryChange = { onEvent(BuildSimulatorEvent.PickerQueryChanged(it)) },
            onPick = { onEvent(BuildSimulatorEvent.ChampionPicked(it)) },
            onDismiss = { onEvent(BuildSimulatorEvent.PickerDismissed) },
        )

        is SimulatorPicker.ItemSlot -> ItemPickerSheet(
            query = state.pickerQuery,
            results = state.itemResults,
            onQueryChange = { onEvent(BuildSimulatorEvent.PickerQueryChanged(it)) },
            onPick = { onEvent(BuildSimulatorEvent.ItemPicked(it)) },
            onDismiss = { onEvent(BuildSimulatorEvent.PickerDismissed) },
        )

        null -> Unit
    }
}

@Composable
private fun ChampionSlot(
    champion: Champion?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.medium)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(AppTheme.dimens.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        if (champion != null) {
            AsyncImage(
                model = DataDragonUrls.championIcon(champion.patchVersion, champion.imageFileName),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(AppTheme.dimens.championThumb)
                    .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = champion.name,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.simulator_change_champion),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.accent,
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = AppTheme.colors.accent,
            )
            Text(
                text = stringResource(R.string.simulator_pick_champion),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.accent,
            )
        }
    }
}

private const val ITEM_GRID_COLUMNS = 3

@Composable
private fun ItemGrid(
    items: List<Item?>,
    onSlotClick: (Int) -> Unit,
    onSlotClear: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // A plain Row/Column grid rather than LazyVerticalGrid: six fixed,
    // never-scrolling slots don't need laziness, and a LazyVerticalGrid
    // nested inside the outer LazyColumn's item {} gets measured with an
    // infinite height constraint regardless of userScrollEnabled, which
    // Compose throws on (IllegalStateException: "Vertically scrollable
    // component was measured with an infinity maximum height constraints").
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        items.chunked(ITEM_GRID_COLUMNS).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                rowItems.forEachIndexed { columnIndex, item ->
                    val index = rowIndex * ITEM_GRID_COLUMNS + columnIndex
                    ItemSlotCell(
                        item = item,
                        onClick = { onSlotClick(index) },
                        onClear = { onSlotClear(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemSlotCell(
    item: Item?,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(AppTheme.colors.surface, AppTheme.shapes.small)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (item != null) {
            AsyncImage(
                model = DataDragonUrls.itemIcon(item.patchVersion, item.imageFileName),
                contentDescription = stringResource(R.string.item_icon, item.name),
                modifier = Modifier.fillMaxSize(),
            )
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(
                        R.string.simulator_remove_item,
                        item.name,
                    ),
                    tint = AppTheme.colors.onPrimary,
                    modifier = Modifier
                        .background(AppTheme.colors.error, AppTheme.shapes.pill),
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.simulator_add_item),
                tint = AppTheme.colors.textDisabled,
            )
        }
    }
}

@Composable
private fun LevelRow(
    level: Int,
    onLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.level_label, level),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.textPrimary,
        )
        Slider(
            value = level.toFloat(),
            onValueChange = { onLevelChange(it.roundToInt()) },
            valueRange = 1f..18f,
            steps = 16,
            colors = SliderDefaults.colors(
                thumbColor = AppTheme.colors.primary,
                activeTrackColor = AppTheme.colors.primary,
                inactiveTrackColor = AppTheme.colors.surfaceElevated,
            ),
        )
    }
}

@Composable
private fun ResultsBlock(result: BuildResult, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.medium)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.medium)
            .padding(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        Text(
            text = stringResource(R.string.simulator_totals, result.level),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.primary,
        )
        Text(
            text = stringResource(R.string.simulator_total_gold, result.totalGold),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
        )

        StatLine(stringResource(R.string.stat_health), result.health)
        StatLine(stringResource(R.string.stat_attack_damage), result.attackDamage)
        StatLine(stringResource(R.string.stat_ability_power), result.abilityPower)
        StatLine(stringResource(R.string.stat_armor), result.armor)
        StatLine(stringResource(R.string.stat_magic_resist), result.magicResist)
        StatLine(
            stringResource(R.string.simulator_ehp_physical),
            result.effectiveHealthPhysical,
        )
        StatLine(stringResource(R.string.simulator_ehp_magic), result.effectiveHealthMagic)
        StatLine(stringResource(R.string.simulator_dps), result.estimatedAutoAttackDps)

        Text(
            text = stringResource(R.string.simulator_assumptions_title),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textDisabled,
        )
        DpsAssumption.ALL.forEach { assumption ->
            Text(
                text = "• " + stringResource(assumption.labelRes()),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textDisabled,
            )
        }

        if (result.hasUnpublishedItemStats) {
            Text(
                text = stringResource(R.string.simulator_unpublished_stats),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.warning,
            )
        }
    }
}

private fun DpsAssumption.labelRes(): Int = when (this) {
    DpsAssumption.TARGET_HAS_NO_ARMOR -> R.string.assumption_no_armor
    DpsAssumption.AUTO_ATTACKS_ONLY -> R.string.assumption_autos_only
    DpsAssumption.NO_ITEM_PASSIVES -> R.string.assumption_no_passives
    DpsAssumption.DEFAULT_CRIT_MULTIPLIER -> R.string.assumption_default_crit
}

@Composable
private fun StatLine(label: String, value: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
        )
        Text(
            text = value.roundToInt().toString(),
            style = AppTheme.typography.statValue,
            color = AppTheme.colors.textPrimary,
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
                                champion.patchVersion,
                                champion.imageFileName,
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(AppTheme.dimens.abilityIcon)
                                .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemPickerSheet(
    query: String,
    results: List<Item>,
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
                        text = stringResource(R.string.items_search_hint),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textDisabled,
                    )
                },
            )
            LazyColumn(
                contentPadding = PaddingValues(bottom = AppTheme.dimens.spaceXl),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
            ) {
                items(items = results, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(item.id) }
                            .padding(AppTheme.dimens.spaceSm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                    ) {
                        AsyncImage(
                            model = DataDragonUrls.itemIcon(item.patchVersion, item.imageFileName),
                            contentDescription = null,
                            modifier = Modifier
                                .size(AppTheme.dimens.abilityIcon)
                                .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = AppTheme.typography.bodyLarge,
                                color = AppTheme.colors.textPrimary,
                            )
                            Text(
                                text = stringResource(R.string.item_cost, item.gold.total),
                                style = AppTheme.typography.caption,
                                color = AppTheme.colors.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
