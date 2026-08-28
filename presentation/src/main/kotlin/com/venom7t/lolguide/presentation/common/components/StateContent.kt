package com.venom7t.lolguide.presentation.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.UiText
import com.venom7t.lolguide.presentation.theme.AppTheme

/**
 * The three non-content states every async screen must handle distinctly
 * (AGENTS.md §13). "Empty" and "error" are separate components on purpose:
 * collapsing them is how apps end up telling an offline user their search
 * returned nothing.
 */

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    val loadingLabel = stringResource(R.string.state_loading)
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = loadingLabel },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = AppTheme.colors.accent)
    }
}

@Composable
fun ErrorContent(
    message: UiText,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            Text(
                text = message.asString(),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onRetry,
                shape = AppTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primary,
                    contentColor = AppTheme.colors.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.action_retry),
                    style = AppTheme.typography.label,
                )
            }
        }
    }
}

@Composable
fun EmptyContent(
    message: UiText,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message.asString(),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(AppTheme.dimens.spaceLg),
        )
    }
}
