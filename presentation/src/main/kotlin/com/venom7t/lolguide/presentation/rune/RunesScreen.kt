package com.venom7t.lolguide.presentation.rune

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.rune.model.Rune
import com.venom7t.lolguide.domain.rune.model.RuneTree
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.common.components.PatchBadge
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun RunesScreenRoot(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RunesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(RunesEvent.ScreenOpened) }

    RunesScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunesScreen(
    state: RunesState,
    onEvent: (RunesEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                    ) {
                        Text(
                            text = stringResource(R.string.runes_title),
                            style = AppTheme.typography.titleLarge,
                            color = AppTheme.colors.textPrimary,
                        )
                        state.patchVersion?.let { PatchBadge(version = it) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.surface),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingContent()

                state.error != null -> ErrorContent(
                    message = state.error,
                    onRetry = { onEvent(RunesEvent.Retry) },
                )

                else -> RunesContent(state = state, onEvent = onEvent)
            }
        }
    }
}

@Composable
private fun RunesContent(
    state: RunesState,
    onEvent: (RunesEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TreeTabs(
            trees = state.trees,
            selectedTreeId = state.selectedTree?.id,
            onTreeSelected = { onEvent(RunesEvent.TreeSelected(it)) },
        )

        val tree = state.selectedTree
        if (tree != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.rune_keystones),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.primary,
                    )
                }
                item { RuneRow(runes = tree.keystones, tree = tree, large = true) }

                items(items = tree.minorSlots, key = { tree.slots.indexOf(it) }) { slot ->
                    RuneRow(runes = slot.runes, tree = tree, large = false)
                }
            }
        }
    }
}

@Composable
private fun TreeTabs(
    trees: List<RuneTree>,
    selectedTreeId: Int?,
    onTreeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        items(items = trees, key = { it.id }) { tree ->
            val selected = tree.id == selectedTreeId
            Column(
                modifier = Modifier
                    .clickable { onTreeSelected(tree.id) }
                    .background(
                        if (selected) AppTheme.colors.surfaceElevated else AppTheme.colors.surface,
                        AppTheme.shapes.medium,
                    )
                    .border(
                        width = AppTheme.dimens.borderWidth,
                        color = if (selected) AppTheme.colors.primary else AppTheme.colors.border,
                        shape = AppTheme.shapes.medium,
                    )
                    .padding(AppTheme.dimens.spaceSm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = DataDragonUrls.runeIcon(tree.iconPath),
                    contentDescription = stringResource(R.string.rune_icon, tree.name),
                    modifier = Modifier.size(AppTheme.dimens.abilityIcon),
                )
                Text(
                    text = tree.name,
                    style = AppTheme.typography.caption,
                    color = if (selected) AppTheme.colors.primary else AppTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun RuneRow(
    runes: List<Rune>,
    tree: RuneTree,
    large: Boolean,
    modifier: Modifier = Modifier,
) {
    val iconSize = if (large) AppTheme.dimens.championThumb else AppTheme.dimens.abilityIcon

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        runes.forEach { rune ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = DataDragonUrls.runeIcon(rune.iconPath),
                    contentDescription = stringResource(R.string.rune_icon, rune.name),
                    modifier = Modifier
                        .size(iconSize)
                        .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.pill),
                )
                Text(
                    text = rune.name,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 2,
                )
            }
        }
    }
}
