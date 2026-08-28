package com.venom7t.lolguide.presentation.champion.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.venom7t.lolguide.domain.champion.model.ChampionFilter
import com.venom7t.lolguide.domain.champion.model.ChampionTag
import com.venom7t.lolguide.domain.champion.model.DamageType
import com.venom7t.lolguide.domain.champion.model.Difficulty
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.labelUiText
import com.venom7t.lolguide.presentation.theme.AppTheme

/** The roles offered as filters. Excludes ChampionTag.Unknown, which is not a
 *  category a user would ever deliberately filter by. */
private val filterableRoles = listOf(
    ChampionTag.Assassin,
    ChampionTag.Fighter,
    ChampionTag.Mage,
    ChampionTag.Marksman,
    ChampionTag.Support,
    ChampionTag.Tank,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionFilterSheet(
    filter: ChampionFilter,
    availableResources: List<String>,
    onEvent: (ChampionListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onEvent(ChampionListEvent.FilterSheetDismissed) },
        sheetState = sheetState,
        containerColor = AppTheme.colors.surface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppTheme.dimens.spaceMd,
                    end = AppTheme.dimens.spaceMd,
                    bottom = AppTheme.dimens.spaceXl,
                ),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.filter_title),
                    style = AppTheme.typography.titleLarge,
                    color = AppTheme.colors.textPrimary,
                )
                if (filter.isActive) {
                    TextButton(onClick = { onEvent(ChampionListEvent.FiltersCleared) }) {
                        Text(
                            text = stringResource(R.string.filter_clear),
                            style = AppTheme.typography.label,
                            color = AppTheme.colors.accent,
                        )
                    }
                }
            }

            FilterSection(title = stringResource(R.string.filter_section_role)) {
                filterableRoles.forEach { role ->
                    ChipOption(
                        label = role.labelUiText().asString(),
                        selected = role in filter.roles,
                        onClick = { onEvent(ChampionListEvent.RoleToggled(role)) },
                    )
                }
            }

            if (availableResources.isNotEmpty()) {
                FilterSection(title = stringResource(R.string.filter_section_resource)) {
                    availableResources.forEach { resource ->
                        ChipOption(
                            // Data Dragon already localises resource names for
                            // the requested locale, so these are shown as-is.
                            label = resource,
                            selected = resource in filter.resources,
                            onClick = { onEvent(ChampionListEvent.ResourceToggled(resource)) },
                        )
                    }
                }
            }

            FilterSection(title = stringResource(R.string.filter_section_difficulty)) {
                Difficulty.entries.forEach { difficulty ->
                    ChipOption(
                        label = difficulty.labelUiText().asString(),
                        selected = difficulty in filter.difficulties,
                        onClick = { onEvent(ChampionListEvent.DifficultyToggled(difficulty)) },
                    )
                }
            }

            FilterSection(title = stringResource(R.string.filter_section_damage)) {
                DamageType.entries.forEach { damageType ->
                    ChipOption(
                        label = damageType.labelUiText().asString(),
                        selected = damageType in filter.damageTypes,
                        onClick = { onEvent(ChampionListEvent.DamageTypeToggled(damageType)) },
                    )
                }
            }

            // Damage type is inferred from Riot's attack/magic bars, not an
            // official field, so the UI says so rather than implying authority
            // it does not have (AGENTS.md §1).
            Text(
                text = stringResource(R.string.damage_type_approximate),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textDisabled,
            )

            FilterSection(title = stringResource(R.string.favourites_title)) {
                ChipOption(
                    label = stringResource(R.string.filter_favourites_only),
                    selected = filter.favouritesOnly,
                    onClick = { onEvent(ChampionListEvent.FavouritesOnlyToggled) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.primary,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = AppTheme.shapes.pill,
        label = {
            Text(text = label, style = AppTheme.typography.label)
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = AppTheme.colors.surfaceElevated,
            labelColor = AppTheme.colors.textSecondary,
            selectedContainerColor = AppTheme.colors.primary,
            selectedLabelColor = AppTheme.colors.onPrimary,
        ),
    )
}

/** Horizontal chip strip shown under the search field on the list screen. */
@Composable
fun ActiveFilterBar(
    filter: ChampionFilter,
    onOpenFilters: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChipOption(
            label = if (filter.isActive) {
                stringResource(R.string.filter_active_count, filter.activeCount)
            } else {
                stringResource(R.string.filter_title)
            },
            selected = filter.isActive,
            onClick = onOpenFilters,
        )
        if (filter.isActive) {
            TextButton(onClick = onClearFilters) {
                Text(
                    text = stringResource(R.string.filter_clear),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.accent,
                )
            }
        }
    }
}
