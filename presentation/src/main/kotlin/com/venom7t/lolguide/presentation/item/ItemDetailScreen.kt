package com.venom7t.lolguide.presentation.item

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.model.ItemStats
import com.venom7t.lolguide.domain.item.usecase.GoldEfficiency
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.common.components.PatchBadge
import com.venom7t.lolguide.presentation.common.abilityText
import com.venom7t.lolguide.presentation.theme.AppTheme
import kotlin.math.roundToInt

@Composable
fun ItemDetailScreenRoot(
    onNavigateBack: () -> Unit,
    onNavigateToItem: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(ItemDetailEvent.ScreenOpened) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ItemDetailEffect.NavigateBack -> onNavigateBack()
                is ItemDetailEffect.NavigateToItem -> onNavigateToItem(effect.itemId)
            }
        }
    }

    ItemDetailScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    state: ItemDetailState,
    onEvent: (ItemDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(ItemDetailEvent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.champion_detail_back),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                    ) {
                        Text(
                            text = state.item?.name.orEmpty(),
                            style = AppTheme.typography.titleLarge,
                            color = AppTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        state.patchVersion?.let { PatchBadge(version = it) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surface,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingContent()

                state.error != null -> ErrorContent(
                    message = state.error,
                    onRetry = { onEvent(ItemDetailEvent.Retry) },
                )

                state.item != null -> DetailContent(state = state, item = state.item, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: ItemDetailState,
    item: Item,
    onEvent: (ItemDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
            ) {
                AsyncImage(
                    model = DataDragonUrls.itemIcon(item.patchVersion, item.imageFileName),
                    contentDescription = stringResource(R.string.item_icon, item.name),
                    modifier = Modifier
                        .size(AppTheme.dimens.championThumb)
                        .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.item_cost, item.gold.total),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary,
                    )
                    // Combine cost only means something for items with parts.
                    if (item.from.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.item_cost_combine, item.gold.base),
                            style = AppTheme.typography.caption,
                            color = AppTheme.colors.textSecondary,
                        )
                    }
                    Text(
                        text = stringResource(R.string.item_sell, item.gold.sell),
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textDisabled,
                    )
                }
            }
        }

        if (!item.stats.isEmpty) {
            item { SectionHeader(stringResource(R.string.item_stats)) }
            item { ItemStatsBlock(stats = item.stats) }
        }

        item { SectionHeader(stringResource(R.string.efficiency_title)) }
        item { EfficiencyBlock(efficiency = state.efficiency) }

        if (item.description.isNotBlank()) {
            item {
                // Reuses the Data Dragon markup parser built for ability text:
                // item descriptions carry the same custom tag vocabulary.
                Text(
                    text = abilityText(item.description),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
        }

        item { SectionHeader(stringResource(R.string.item_build_path)) }

        if (!state.hasBuildPath) {
            item {
                Text(
                    text = stringResource(R.string.item_no_build_path),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textDisabled,
                )
            }
        }

        if (state.components.isNotEmpty()) {
            item { SubHeader(stringResource(R.string.item_components)) }
            item {
                RelatedItemRow(
                    items = state.components,
                    onClick = { onEvent(ItemDetailEvent.RelatedItemClicked(it)) },
                )
            }
        }

        if (state.buildsInto.isNotEmpty()) {
            item { SubHeader(stringResource(R.string.item_builds_into)) }
            item {
                RelatedItemRow(
                    items = state.buildsInto,
                    onClick = { onEvent(ItemDetailEvent.RelatedItemClicked(it)) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AppTheme.typography.titleMedium,
        color = AppTheme.colors.primary,
        modifier = modifier.padding(top = AppTheme.dimens.spaceSm),
    )
}

@Composable
private fun SubHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AppTheme.typography.label,
        color = AppTheme.colors.textSecondary,
        modifier = modifier,
    )
}

@Composable
private fun ItemStatsBlock(stats: ItemStats, modifier: Modifier = Modifier) {
    val rows = buildList {
        fun flat(labelRes: Int, value: Double) {
            if (value != 0.0) add(labelRes to value.formatCompact())
        }

        fun percent(labelRes: Int, value: Double) {
            if (value != 0.0) add(labelRes to value.formatPercent())
        }

        flat(R.string.stat_attack_damage, stats.attackDamage)
        flat(R.string.stat_ability_power, stats.abilityPower)
        flat(R.string.stat_health, stats.health)
        flat(R.string.stat_mana, stats.mana)
        flat(R.string.stat_armor, stats.armor)
        flat(R.string.stat_magic_resist, stats.magicResist)
        percent(R.string.stat_attack_speed, stats.attackSpeedPercent)
        percent(R.string.stat_crit_chance, stats.critChancePercent)
        flat(R.string.stat_health_regen, stats.healthRegen)
        flat(R.string.stat_move_speed, stats.moveSpeedFlat)
        percent(R.string.stat_life_steal, stats.lifeStealPercent)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
    ) {
        rows.forEach { (labelRes, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.surface, AppTheme.shapes.small)
                    .padding(AppTheme.dimens.spaceSm),
            ) {
                Text(
                    text = stringResource(labelRes),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = value,
                    style = AppTheme.typography.statValue,
                    color = AppTheme.colors.textPrimary,
                )
            }
        }
    }
}

/**
 * The efficiency verdict.
 *
 * Three genuinely different outcomes, rendered differently: a real percentage,
 * a partial figure that understates the item, and "Riot published no stats so
 * this cannot be computed". Collapsing the last into "0%" would libel half the
 * finished items in the game.
 */
@Composable
private fun EfficiencyBlock(efficiency: GoldEfficiency?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.medium)
            .border(
                width = AppTheme.dimens.borderWidth,
                color = AppTheme.colors.border,
                shape = AppTheme.shapes.medium,
            )
            .padding(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        val percent = efficiency?.percent

        if (efficiency == null || efficiency.hasUnpublishedStats || percent == null) {
            Text(
                text = stringResource(R.string.efficiency_unavailable),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textDisabled,
            )
            return@Column
        }

        Text(
            text = stringResource(R.string.efficiency_value, percent.roundToInt()),
            style = AppTheme.typography.displayLarge,
            color = if (percent >= 100.0) AppTheme.colors.success else AppTheme.colors.warning,
        )
        Text(
            text = stringResource(
                R.string.efficiency_stat_value,
                efficiency.statValueGold.roundToInt(),
            ),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
        )
        if (efficiency.isPartial) {
            Text(
                text = stringResource(R.string.efficiency_partial),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.warning,
            )
        }
        Text(
            text = stringResource(R.string.efficiency_note),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textDisabled,
        )
    }
}

@Composable
private fun RelatedItemRow(
    items: List<Item>,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        items(items = items, key = { it.id }) { related ->
            Column(
                modifier = Modifier
                    .width(AppTheme.dimens.championThumb)
                    .clickable { onClick(related.id) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
            ) {
                AsyncImage(
                    model = DataDragonUrls.itemIcon(related.patchVersion, related.imageFileName),
                    contentDescription = stringResource(R.string.item_icon, related.name),
                    modifier = Modifier
                        .size(AppTheme.dimens.abilityIcon)
                        .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
                )
                Text(
                    text = related.name,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = related.gold.total.toString(),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.primary,
                )
            }
        }
    }
}

private fun Double.formatCompact(): String =
    if (this == toLong().toDouble()) toLong().toString() else toString()

private fun Double.formatPercent(): String = "${formatCompact()}%"
