package com.venom7t.lolguide.presentation.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.champion.list.ChipOption
import com.venom7t.lolguide.presentation.champion.list.FilterSection
import com.venom7t.lolguide.presentation.theme.AppTheme

/**
 * Same sheet chrome as the champion list's filter sheet ([FilterSection] /
 * [ChipOption], shared from that package) -- items only have one filterable
 * dimension (tags), but the panel should still read as "the app's filter
 * sheet", not a one-off row of chips under the search field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFilterSheet(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagToggled: (String) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                if (selectedTags.isNotEmpty()) {
                    TextButton(onClick = onClearFilters) {
                        Text(
                            text = stringResource(R.string.filter_clear),
                            style = AppTheme.typography.label,
                            color = AppTheme.colors.accent,
                        )
                    }
                }
            }

            FilterSection(title = stringResource(R.string.items_filter_section_tags)) {
                availableTags.forEach { tag ->
                    ChipOption(
                        label = tag,
                        selected = tag in selectedTags,
                        onClick = { onTagToggled(tag) },
                    )
                }
            }
        }
    }
}

/** Horizontal summary chip shown under the search field, mirroring the champion list's. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemActiveFilterBar(
    selectedTags: Set<String>,
    onOpenFilters: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChipOption(
            label = if (selectedTags.isNotEmpty()) {
                stringResource(R.string.filter_active_count, selectedTags.size)
            } else {
                stringResource(R.string.filter_title)
            },
            selected = selectedTags.isNotEmpty(),
            onClick = onOpenFilters,
        )
        if (selectedTags.isNotEmpty()) {
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
