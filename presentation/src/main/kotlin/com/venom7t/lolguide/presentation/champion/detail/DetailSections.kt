package com.venom7t.lolguide.presentation.champion.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.builds.model.SavedBuild
import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.champion.model.ScaledStats
import com.venom7t.lolguide.domain.champion.model.Skin
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.skinDisplayName
import com.venom7t.lolguide.presentation.theme.AppTheme
import kotlinx.collections.immutable.ImmutableList

/**
 * The splash art of the currently selected skin, plus a thumbnail strip.
 *
 * Splash paths are keyed on [Skin.num], never [Skin.id] -- using the id gives
 * a URL that 404s silently.
 */
@Composable
fun SkinsSection(
    championId: String,
    championName: String,
    skins: List<Skin>,
    selectedIndex: Int,
    onSkinSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (skins.isEmpty()) return

    val safeIndex = selectedIndex.coerceIn(0, skins.lastIndex)
    val selected = skins[safeIndex]

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        AsyncImage(
            model = DataDragonUrls.championSplash(championId, selected.num),
            contentDescription = stringResource(
                R.string.skin_splash,
                skinDisplayName(selected.name, championName),
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.large),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = skinDisplayName(selected.name, championName),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (selected.hasChromas) {
                // Data Dragon ships a boolean, and hosts no chroma images, so
                // this states existence rather than inventing a count.
                Text(
                    text = stringResource(R.string.skin_has_chromas),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.accent,
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            contentPadding = PaddingValues(vertical = AppTheme.dimens.spaceXs),
        ) {
            items(items = skins, key = { it.id }) { skin ->
                val index = skins.indexOf(skin)
                val isSelected = index == safeIndex
                AsyncImage(
                    model = DataDragonUrls.championLoading(championId, skin.num),
                    contentDescription = skinDisplayName(skin.name, championName),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(AppTheme.dimens.abilityIcon)
                        .height(AppTheme.dimens.championThumb)
                        .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small)
                        .border(
                            width = AppTheme.dimens.borderWidth,
                            color = if (isSelected) {
                                AppTheme.colors.primary
                            } else {
                                AppTheme.colors.border
                            },
                            shape = AppTheme.shapes.small,
                        )
                        .clickable { onSkinSelected(index) },
                )
            }
        }
    }
}

/**
 * Level slider and the stats it produces.
 *
 * The derived-values notice is not decoration: Riot publishes only level-1
 * values, so everything above level 1 is this app's arithmetic and must be
 * presented that way (AGENTS.md §1).
 */
@Composable
fun LevelStatsSection(
    level: Int,
    stats: ScaledStats,
    resourceName: String,
    onLevelChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.level_stats_title),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.primary,
            )
            Text(
                text = stringResource(R.string.level_label, level),
                style = AppTheme.typography.statValue,
                color = AppTheme.colors.textPrimary,
            )
        }

        Slider(
            value = level.toFloat(),
            onValueChange = { onLevelChanged(it.toInt()) },
            valueRange = ChampionStatCalculator.MIN_LEVEL.toFloat()..
                ChampionStatCalculator.MAX_LEVEL.toFloat(),
            // 18 levels means 17 gaps between them.
            steps = ChampionStatCalculator.MAX_LEVEL - ChampionStatCalculator.MIN_LEVEL - 1,
            colors = SliderDefaults.colors(
                thumbColor = AppTheme.colors.primary,
                activeTrackColor = AppTheme.colors.primary,
                inactiveTrackColor = AppTheme.colors.surfaceElevated,
            ),
        )

        ScaledStatRow(stringResource(R.string.stat_health), stats.hp)
        if (stats.mp > 0.0) {
            ScaledStatRow(resourceName.ifBlank { stringResource(R.string.stat_resource) }, stats.mp)
        }
        ScaledStatRow(stringResource(R.string.stat_attack_damage), stats.attackDamage)
        ScaledStatRow(stringResource(R.string.stat_attack_speed), stats.attackSpeed, decimals = 3)
        ScaledStatRow(stringResource(R.string.stat_armor), stats.armor)
        ScaledStatRow(stringResource(R.string.stat_magic_resist), stats.spellBlock)
        ScaledStatRow(stringResource(R.string.stat_health_regen), stats.hpRegen, decimals = 2)
        ScaledStatRow(stringResource(R.string.stat_move_speed), stats.moveSpeed)
        ScaledStatRow(stringResource(R.string.stat_attack_range), stats.attackRange)

        Text(
            text = stringResource(R.string.derived_values_notice),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textDisabled,
        )
    }
}

@Composable
private fun ScaledStatRow(
    label: String,
    value: Double,
    modifier: Modifier = Modifier,
    decimals: Int = 0,
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
            text = value.format(decimals),
            style = AppTheme.typography.statValue,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Composable
fun RemoveFavouriteDialog(
    championName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surface,
        title = {
            Text(
                text = stringResource(R.string.favourite_removed_confirm_title),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.favourite_removed_confirm_message, championName),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.action_remove),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.textSecondary,
                )
            }
        },
    )
}

/**
 * The user's own saved builds for this champion (Build Simulator, synced via
 * Firebase). Tapping a row reloads that build into the simulator; the "new
 * build" row opens a blank one instead.
 */
@Composable
fun SavedBuildsSection(
    builds: ImmutableList<SavedBuild>,
    onBuildClick: (String) -> Unit,
    onNewBuildClick: () -> Unit,
    onDeleteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface, AppTheme.shapes.medium)
                .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.medium)
                .clickable(onClick = onNewBuildClick)
                .padding(AppTheme.dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AppTheme.colors.accent)
            Text(
                text = stringResource(R.string.saved_build_new),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.accent,
            )
        }

        builds.forEach { build ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.surface, AppTheme.shapes.medium)
                    .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.medium)
                    .clickable { onBuildClick(build.id) }
                    .padding(AppTheme.dimens.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.saved_build_item_count, build.itemIds.size, build.level),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onDeleteClick(build.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.saved_build_delete),
                        tint = AppTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteSavedBuildDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surface,
        title = {
            Text(
                text = stringResource(R.string.saved_build_delete_confirm_title),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.saved_build_delete_confirm_body),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.action_remove),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.textSecondary,
                )
            }
        },
    )
}

/** A notice shown when computed values are on screen. */
@Composable
fun DerivedValuesNotice(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small)
            .padding(AppTheme.dimens.spaceSm),
    ) {
        Text(
            text = stringResource(R.string.derived_values_notice),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.warning,
            textAlign = TextAlign.Start,
        )
    }
}

/** Rounds for display without pulling in a locale-specific formatter. */
internal fun Double.format(decimals: Int): String =
    if (decimals <= 0) {
        Math.round(this).toString()
    } else {
        val factor = generateSequence(1.0) { it * 10 }.elementAt(decimals)
        (Math.round(this * factor) / factor).toString()
    }
