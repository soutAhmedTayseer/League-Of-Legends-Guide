package com.example.lolguide.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.lolguide.presentation.R
import com.example.lolguide.presentation.theme.AppTheme

/**
 * The patch label every data-bearing screen must show (AGENTS.md §1).
 *
 * When [isStale] the badge switches to the warning colour, because cached data
 * shown during an outage must be visibly distinguishable from data we just
 * confirmed is current -- silently relabelling it as live is the failure mode
 * this whole rule exists to prevent.
 */
@Composable
fun PatchBadge(
    version: String,
    modifier: Modifier = Modifier,
    isStale: Boolean = false,
) {
    val accent = if (isStale) AppTheme.colors.warning else AppTheme.colors.accent

    Text(
        text = stringResource(R.string.patch_label, version),
        style = AppTheme.typography.caption,
        color = accent,
        modifier = modifier
            .border(
                width = AppTheme.dimens.borderWidth,
                color = accent,
                shape = AppTheme.shapes.pill,
            )
            .background(color = AppTheme.colors.surface, shape = AppTheme.shapes.pill)
            .padding(
                horizontal = AppTheme.dimens.spaceSm,
                vertical = AppTheme.dimens.spaceXs,
            ),
    )
}
