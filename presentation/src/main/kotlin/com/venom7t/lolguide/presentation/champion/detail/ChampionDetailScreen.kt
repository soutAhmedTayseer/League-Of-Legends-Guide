package com.venom7t.lolguide.presentation.champion.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionDetail
import com.venom7t.lolguide.domain.champion.model.Spell
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.abilityText
import com.venom7t.lolguide.presentation.common.rememberSplashAccent
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.common.components.PatchBadge
import com.venom7t.lolguide.presentation.common.skinDisplayName
import com.venom7t.lolguide.presentation.theme.AppTheme
import com.venom7t.lolguide.presentation.voiceline.VoiceLinePanel

@Composable
fun ChampionDetailScreenRoot(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChampionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChampionDetailEvent.ScreenOpened)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ChampionDetailEffect.NavigateBack -> onNavigateBack()
                is ChampionDetailEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    ChampionDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionDetailScreen(
    state: ChampionDetailState,
    onEvent: (ChampionDetailEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(ChampionDetailEvent.BackClicked) }) {
                        Icon(
                            // Auto-mirrored so the arrow points the correct way
                            // in Arabic (AGENTS.md §10).
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
                            text = state.champion?.name.orEmpty(),
                            style = AppTheme.typography.titleLarge,
                            color = AppTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        state.patchVersion?.let { PatchBadge(version = it) }
                    }
                },
                actions = {
                    state.champion?.let { champion ->
                        IconButton(onClick = { onEvent(ChampionDetailEvent.FavouriteClicked) }) {
                            Icon(
                                imageVector = if (state.isFavourite) {
                                    Icons.Filled.Star
                                } else {
                                    Icons.Outlined.StarBorder
                                },
                                contentDescription = stringResource(
                                    if (state.isFavourite) {
                                        R.string.favourite_remove
                                    } else {
                                        R.string.favourite_add
                                    },
                                    champion.name,
                                ),
                                tint = if (state.isFavourite) {
                                    AppTheme.colors.primary
                                } else {
                                    AppTheme.colors.textDisabled
                                },
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
            when {
                state.isLoading -> LoadingContent()

                state.error != null -> ErrorContent(
                    message = state.error,
                    onRetry = { onEvent(ChampionDetailEvent.Retry) },
                )

                state.champion != null && state.detail != null -> DetailContent(
                    state = state,
                    champion = state.champion,
                    detail = state.detail,
                    onEvent = onEvent,
                )
            }
        }

        if (state.pendingFavouriteRemoval && state.champion != null) {
            RemoveFavouriteDialog(
                championName = state.champion.name,
                onConfirm = { onEvent(ChampionDetailEvent.FavouriteRemovalConfirmed) },
                onDismiss = { onEvent(ChampionDetailEvent.FavouriteRemovalCancelled) },
            )
        }
    }
}

@Composable
private fun DetailContent(
    state: ChampionDetailState,
    champion: Champion,
    detail: ChampionDetail,
    onEvent: (ChampionDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Accent pulled from the splash art of the skin currently on screen, so
    // the page picks up the champion's own colouring. Falls back to the theme
    // accent, and is used only for accents -- never for text on a background.
    val accent = rememberSplashAccent(
        championId = champion.id,
        skinNum = detail.skins.getOrNull(state.selectedSkinIndex)?.num ?: 0,
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        item {
            if (detail.skins.isNotEmpty()) {
                SkinsSection(
                    championId = champion.id,
                    championName = champion.name,
                    skins = detail.skins,
                    selectedIndex = state.selectedSkinIndex,
                    onSkinSelected = { onEvent(ChampionDetailEvent.SkinSelected(it)) },
                )
            } else {
                AsyncImage(
                    model = DataDragonUrls.championSplash(champion.id),
                    contentDescription = stringResource(
                        R.string.champion_detail_portrait,
                        champion.name,
                    ),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.large),
                )
            }
        }

        item {
            Text(
                text = champion.title,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textSecondary,
            )
        }

        item {
            AttributeBars(champion = champion)
        }

        if (detail.lore.isNotBlank()) {
            item {
                SectionHeader(text = stringResource(R.string.champion_detail_lore), accent = accent)
            }
            item {
                Text(
                    text = detail.lore,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
        }

        item {
            SectionHeader(text = stringResource(R.string.champion_detail_abilities), accent = accent)
        }

        item {
            AbilityCard(
                iconUrl = DataDragonUrls.passiveIcon(
                    version = detail.patchVersion,
                    imageFileName = detail.passive.imageFileName,
                ),
                slotLabel = stringResource(R.string.champion_detail_passive),
                name = detail.passive.name,
                description = detail.passive.description,
                spell = null,
            )
        }

        items(items = detail.spells, key = { it.id }) { spell ->
            AbilityCard(
                iconUrl = DataDragonUrls.spellIcon(
                    version = detail.patchVersion,
                    imageFileName = spell.imageFileName,
                ),
                slotLabel = spell.slot.name,
                name = spell.name,
                description = spell.description,
                spell = spell,
            )
        }

        state.scaledStats?.let { scaled ->
            item {
                LevelStatsSection(
                    level = state.level,
                    stats = scaled,
                    resourceName = champion.partype,
                    onLevelChanged = { onEvent(ChampionDetailEvent.LevelChanged(it)) },
                )
            }
        }

        item {
            SectionHeader(text = stringResource(R.string.champion_detail_base_stats), accent = accent)
        }

        item {
            StatsGrid(champion = champion)
        }

        item {
            VoiceLinePanel(championId = champion.id)
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = AppTheme.colors.primary,
) {
    Text(
        text = text,
        style = AppTheme.typography.titleMedium,
        color = accent,
        modifier = modifier.padding(top = AppTheme.dimens.spaceSm),
    )
}

@Composable
private fun AttributeBars(champion: Champion, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        AttributeBar(
            label = stringResource(R.string.attribute_attack),
            value = champion.info.attack,
            color = AppTheme.colors.attack,
        )
        AttributeBar(
            label = stringResource(R.string.attribute_defense),
            value = champion.info.defense,
            color = AppTheme.colors.defense,
        )
        AttributeBar(
            label = stringResource(R.string.attribute_magic),
            value = champion.info.magic,
            color = AppTheme.colors.magic,
        )
        AttributeBar(
            label = stringResource(R.string.attribute_difficulty),
            value = champion.info.difficulty,
            color = AppTheme.colors.difficulty,
        )
    }
}

@Composable
private fun AttributeBar(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.label,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.weight(0.35f),
        )
        LinearProgressIndicator(
            // Riot's info bars are on a 0-10 scale.
            progress = { (value.coerceIn(0, 10)) / 10f },
            color = color,
            trackColor = AppTheme.colors.surfaceElevated,
            modifier = Modifier.weight(0.5f),
        )
        Text(
            text = value.toString(),
            style = AppTheme.typography.statValue,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(0.15f),
        )
    }
}

@Composable
private fun AbilityCard(
    iconUrl: String,
    slotLabel: String,
    name: String,
    description: String,
    spell: Spell?,
    modifier: Modifier = Modifier,
) {
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            AsyncImage(
                model = iconUrl,
                contentDescription = stringResource(
                    R.string.champion_detail_ability_icon,
                    name,
                ),
                modifier = Modifier
                    .size(AppTheme.dimens.abilityIcon)
                    .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slotLabel,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.accent,
                )
                Text(
                    text = name,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                )
            }
        }

        Text(
            // Data Dragon ships pseudo-HTML here; the parser maps damage-type
            // tags to colours and drops the structural ones.
            text = abilityText(description),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
        )

        if (spell != null) {
            SpellRankTable(spell = spell)
        }
    }
}

/**
 * Per-rank cooldown and cost.
 *
 * This is what the per-rank arrays in the domain model buy us: the previous
 * implementation could only print Riot's "14/12/10/8/6" burn string as text.
 */
@Composable
private fun SpellRankTable(spell: Spell, modifier: Modifier = Modifier) {
    val unavailable = stringResource(R.string.ability_value_unavailable)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
    ) {
        SpellRankRow(
            label = stringResource(R.string.ability_cooldown),
            values = spell.cooldownPerRank,
            maxRank = spell.maxRank,
            unavailable = unavailable,
        )
        SpellRankRow(
            label = if (spell.costType.isNotBlank()) {
                spell.costType
            } else {
                stringResource(R.string.ability_cost)
            },
            values = spell.costPerRank,
            maxRank = spell.maxRank,
            unavailable = unavailable,
        )
    }
}

@Composable
private fun SpellRankRow(
    label: String,
    values: List<Double>,
    maxRank: Int,
    unavailable: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.weight(0.35f),
        )
        Row(
            modifier = Modifier.weight(0.65f),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
        ) {
            repeat(maxRank.coerceAtLeast(1)) { rank ->
                Text(
                    // A missing rank shows an em dash rather than a guessed
                    // value -- game numbers are never invented (AGENTS.md §1).
                    text = values.getOrNull(rank)?.formatCompact() ?: unavailable,
                    style = AppTheme.typography.statValue,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(champion: Champion, modifier: Modifier = Modifier) {
    val stats = champion.stats
    val rows = listOf(
        stringResource(R.string.stat_health) to (stats.hp to stats.hpPerLevel),
        stringResource(R.string.stat_attack_damage) to (stats.attackDamage to stats.attackDamagePerLevel),
        stringResource(R.string.stat_armor) to (stats.armor to stats.armorPerLevel),
        stringResource(R.string.stat_magic_resist) to (stats.spellBlock to stats.spellBlockPerLevel),
        stringResource(R.string.stat_attack_speed) to (stats.attackSpeed to stats.attackSpeedPerLevel),
        stringResource(R.string.stat_health_regen) to (stats.hpRegen to stats.hpRegenPerLevel),
        stringResource(R.string.stat_move_speed) to (stats.moveSpeed to 0.0),
        stringResource(R.string.stat_attack_range) to (stats.attackRange to 0.0),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        rows.forEach { (label, values) ->
            val (base, perLevel) = values
            StatRow(label = label, base = base, perLevel = perLevel)
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    base: Double,
    perLevel: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.small)
            .padding(AppTheme.dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = base.formatCompact(),
            style = AppTheme.typography.statValue,
            color = AppTheme.colors.textPrimary,
        )
        if (perLevel != 0.0) {
            Text(
                text = " " + stringResource(R.string.stat_per_level, perLevel.formatCompact()),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.accent,
            )
        }
    }
}

/** Drops a trailing `.0` so whole numbers read as "600", not "600.0". */
private fun Double.formatCompact(): String =
    if (this == toLong().toDouble()) toLong().toString() else toString()
