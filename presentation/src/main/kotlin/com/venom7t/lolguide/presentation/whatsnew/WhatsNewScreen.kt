package com.venom7t.lolguide.presentation.whatsnew

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.patch.model.ChampionChange
import com.venom7t.lolguide.domain.patch.model.ItemChange
import com.venom7t.lolguide.domain.patch.model.PatchDiff
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun WhatsNewScreenRoot(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WhatsNewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(WhatsNewEvent.ScreenOpened) }

    WhatsNewScreen(state = state, onNavigateBack = onNavigateBack, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewScreen(
    state: WhatsNewState,
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
                    Text(
                        text = stringResource(R.string.whats_new_title),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.background),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingContent()
                state.diff == null -> EmptyContent(message = uiText(R.string.whats_new_unavailable))
                state.diff.isEmpty -> EmptyContent(
                    message = uiText(
                        R.string.whats_new_empty,
                        state.diff.fromVersion,
                        state.diff.toVersion,
                    ),
                )
                else -> DiffContent(diff = state.diff)
            }
        }
    }
}

@Composable
private fun DiffContent(diff: PatchDiff, modifier: Modifier = Modifier) {
    val added = diff.championChanges.filterIsInstance<ChampionChange.Added>()
    val removed = diff.championChanges.filterIsInstance<ChampionChange.Removed>()
    val changed = diff.championChanges.filterIsInstance<ChampionChange.StatsChanged>()
    val itemsAdded = diff.itemChanges.filterIsInstance<ItemChange.Added>()
    val itemsRemoved = diff.itemChanges.filterIsInstance<ItemChange.Removed>()
    val itemsRepriced = diff.itemChanges.filterIsInstance<ItemChange.Repriced>()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        section(R.string.whats_new_champions_added, added) { NameRow(it.championName) }
        section(R.string.whats_new_champions_removed, removed) { NameRow(it.championName) }
        section(R.string.whats_new_champions_changed, changed) {
            NameRow("${it.championName} (${it.statDeltas.size})")
        }
        section(R.string.whats_new_items_added, itemsAdded) { NameRow(it.itemName) }
        section(R.string.whats_new_items_removed, itemsRemoved) { NameRow(it.itemName) }
        section(R.string.whats_new_items_repriced, itemsRepriced) {
            RepricedRow(name = it.itemName, before = it.goldBefore, after = it.goldAfter)
        }
    }
}

private fun <T> androidx.compose.foundation.lazy.LazyListScope.section(
    titleRes: Int,
    entries: List<T>,
    row: @Composable (T) -> Unit,
) {
    if (entries.isEmpty()) return
    item {
        Text(
            text = stringResource(titleRes),
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.primary,
        )
    }
    lazyItems(entries) { entry -> row(entry) }
}

@Composable
private fun NameRow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.colors.textPrimary,
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.small)
            .padding(AppTheme.dimens.spaceSm),
    )
}

@Composable
private fun RepricedRow(
    name: String,
    before: Int,
    after: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.small)
            .padding(AppTheme.dimens.spaceSm),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = name, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
        Text(
            text = stringResource(R.string.whats_new_repriced_from_to, before, after),
            style = AppTheme.typography.statValue,
            color = if (after > before) AppTheme.colors.warning else AppTheme.colors.success,
        )
    }
}
