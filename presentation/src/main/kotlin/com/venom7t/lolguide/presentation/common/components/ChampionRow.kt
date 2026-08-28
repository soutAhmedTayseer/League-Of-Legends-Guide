package com.venom7t.lolguide.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.labelUiText
import com.venom7t.lolguide.presentation.theme.AppTheme

/**
 * The champion list row, shared by the champion list and the favourites tab so
 * the two cannot drift apart.
 *
 * @param onFavouriteClick null hides the star entirely, for contexts where
 *   favouriting makes no sense (the compare picker).
 */
@Composable
fun ChampionRow(
    champion: Champion,
    isFavourite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFavouriteClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.medium)
            .border(
                width = AppTheme.dimens.borderWidth,
                color = AppTheme.colors.border,
                shape = AppTheme.shapes.medium,
            )
            .clickable(onClick = onClick)
            .padding(AppTheme.dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        // Versioned art fetched on the champion's own patch, so an icon can
        // never come from a different patch than the data beside it.
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = champion.name,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = champion.title,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (champion.tags.isNotEmpty()) {
                // joinToString's transform is not an inline lambda, so the
                // @Composable asString() cannot be called inside it. map is
                // inline, so resolving the labels first is what makes this legal.
                val tagLabels = champion.tags.map { it.labelUiText().asString() }
                Text(
                    text = tagLabels.joinToString(" · "),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (onFavouriteClick != null) {
            IconButton(onClick = onFavouriteClick) {
                Icon(
                    imageVector = if (isFavourite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    // The description states the action, not the state, so a
                    // screen reader announces what tapping will do.
                    contentDescription = stringResource(
                        if (isFavourite) R.string.favourite_remove else R.string.favourite_add,
                        champion.name,
                    ),
                    tint = if (isFavourite) AppTheme.colors.primary else AppTheme.colors.textDisabled,
                )
            }
        }
    }
}
