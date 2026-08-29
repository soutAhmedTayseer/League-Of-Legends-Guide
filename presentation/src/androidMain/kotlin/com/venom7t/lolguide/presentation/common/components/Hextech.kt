package com.venom7t.lolguide.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.theme.AppTheme

/**
 * The app's image treatment: a gold hairline, a dark inset, then the art,
 * every edge chamfered to match [AppTheme.shapes].
 *
 * Champion and item art is the richest material this app has, and a bare
 * `AsyncImage` on a flat background wastes it. Framing is what makes the
 * same asset read as premium rather than as a thumbnail, so this replaces
 * ad-hoc `AsyncImage` calls anywhere art is presented as content rather
 * than as an icon.
 */
@Composable
fun HextechFrame(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    onClick: (() -> Unit)? = null,
    onState: ((coil3.compose.AsyncImagePainter.State) -> Unit)? = null,
) {
    val shape = AppTheme.shapes.medium
    Box(
        modifier = modifier
            .clip(shape)
            .background(AppTheme.colors.surfaceElevated)
            .border(
                width = if (isSelected) 2.dp else AppTheme.dimens.borderWidth,
                color = if (isSelected) AppTheme.colors.accent else AppTheme.colors.primary,
                shape = shape,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            // The inset is the frame: it lets the surface colour show as a
            // mount between the hairline and the art.
            .padding(3.dp),
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            alignment = alignment,
            onState = onState,
            modifier = Modifier
                .fillMaxSize()
                .clip(AppTheme.shapes.small),
        )
    }
}

/**
 * A section label: small wide Cinzel caps, then a gold rule that fades out
 * across the remaining width.
 *
 * The rule is the structure here -- it separates bands of content without
 * the boxed-in feel of a card header, and the fade keeps it from reading as
 * a hard table border.
 */
@Composable
fun SectionRule(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.eyebrow,
            color = AppTheme.colors.primary,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(AppTheme.dimens.borderWidth)
                .background(
                    Brush.horizontalGradient(
                        listOf(AppTheme.colors.border, Color.Transparent),
                    ),
                ),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

/**
 * A chamfered panel. The single card primitive: anything that would have
 * been a `Card` or a bordered `Column` uses this so corner geometry and trim
 * stay identical everywhere.
 */
@Composable
fun CutSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlighted: Boolean = false,
    content: @Composable () -> Unit,
) {
    val shape = AppTheme.shapes.medium
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (highlighted) {
                    Brush.linearGradient(
                        listOf(
                            AppTheme.colors.accent.copy(alpha = 0.16f),
                            AppTheme.colors.surface,
                        ),
                    )
                } else {
                    Brush.linearGradient(
                        listOf(AppTheme.colors.surface, AppTheme.colors.surface),
                    )
                },
            )
            .border(
                width = AppTheme.dimens.borderWidth,
                color = if (highlighted) AppTheme.colors.accent else AppTheme.colors.border,
                shape = shape,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        content()
    }
}

/**
 * The one confirm/destructive dialog shape for the whole app (AGENTS.md
 * §13): dark surface, gold hairline via the default [AlertDialog] shape
 * override, a themed title/body, and a confirm action coloured by
 * [destructive] so "leave a game" and "delete something" read differently
 * from "cancel" without a second dialog variant to maintain.
 */
@Composable
fun HextechConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = null,
    destructive: Boolean = true,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surface,
        shape = AppTheme.shapes.medium,
        title = {
            Text(
                text = title,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
        },
        text = {
            Text(
                text = body,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    style = AppTheme.typography.label,
                    color = if (destructive) AppTheme.colors.error else AppTheme.colors.accent,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissLabel ?: stringResource(R.string.action_cancel),
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.textSecondary,
                )
            }
        },
    )
}
