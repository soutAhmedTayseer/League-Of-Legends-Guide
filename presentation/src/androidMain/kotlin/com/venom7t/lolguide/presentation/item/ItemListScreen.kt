package com.venom7t.lolguide.presentation.item

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.HextechFrame
import com.venom7t.lolguide.presentation.common.components.ItemListSkeleton
import com.venom7t.lolguide.presentation.common.components.rememberMinimumVisibleLoading
import com.venom7t.lolguide.presentation.common.components.PatchBadge
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun ItemListScreenRoot(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToBuildSimulator: () -> Unit,
    onNavigateToRunes: () -> Unit,
    onNavigateToSummonerSpells: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ItemListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ItemListEvent.ScreenOpened)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ItemListEffect.NavigateToDetail -> onNavigateToDetail(effect.itemId)
            }
        }
    }

    ItemListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToBuildSimulator = onNavigateToBuildSimulator,
        onNavigateToRunes = onNavigateToRunes,
        onNavigateToSummonerSpells = onNavigateToSummonerSpells,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    state: ItemListState,
    onEvent: (ItemListEvent) -> Unit,
    onNavigateToBuildSimulator: () -> Unit,
    onNavigateToRunes: () -> Unit,
    onNavigateToSummonerSpells: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                    ) {
                        Text(
                            text = stringResource(R.string.items_title),
                            style = AppTheme.typography.titleLarge,
                            color = AppTheme.colors.textPrimary,
                        )
                        state.patchVersion?.let { PatchBadge(version = it) }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToBuildSimulator) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = stringResource(R.string.simulator_open),
                            tint = AppTheme.colors.textSecondary,
                        )
                    }
                    IconButton(onClick = onNavigateToRunes) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = stringResource(R.string.runes_open),
                            tint = AppTheme.colors.textSecondary,
                        )
                    }
                    IconButton(onClick = onNavigateToSummonerSpells) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = stringResource(R.string.spells_open),
                            tint = AppTheme.colors.textSecondary,
                        )
                    }
                    if (state.selectedTags.isNotEmpty() || state.query.isNotEmpty()) {
                        IconButton(onClick = { onEvent(ItemListEvent.FiltersCleared) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.filter_clear),
                                tint = AppTheme.colors.textSecondary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(ItemListEvent.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.dimens.spaceMd),
                singleLine = true,
                shape = AppTheme.shapes.medium,
                textStyle = AppTheme.typography.bodyMedium,
                placeholder = {
                    Text(
                        text = stringResource(R.string.items_search_hint),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textDisabled,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AppTheme.colors.textSecondary,
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                    focusedBorderColor = AppTheme.colors.primary,
                    unfocusedBorderColor = AppTheme.colors.border,
                    cursorColor = AppTheme.colors.accent,
                ),
            )

            if (state.availableTags.isNotEmpty()) {
                ItemActiveFilterBar(
                    selectedTags = state.selectedTags,
                    onOpenFilters = { onEvent(ItemListEvent.FilterSheetOpened) },
                    onClearFilters = { onEvent(ItemListEvent.FiltersCleared) },
                    modifier = Modifier.padding(horizontal = AppTheme.dimens.spaceMd),
                )
                Spacer(modifier = Modifier.height(AppTheme.dimens.spaceSm))
            }

            val showSkeleton = rememberMinimumVisibleLoading(state.isLoading)
            when {
                showSkeleton -> ItemListSkeleton()

                state.error != null && state.items.isEmpty() -> ErrorContent(
                    message = state.error,
                    onRetry = { onEvent(ItemListEvent.Retry) },
                )

                state.hasNoResults -> EmptyContent(message = uiText(R.string.items_no_results))

                state.isEmpty -> EmptyContent(message = uiText(R.string.items_empty))

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                ) {
                    items(items = state.items, key = { it.id }) { item ->
                        ItemRow(
                            item = item,
                            onClick = { onEvent(ItemListEvent.ItemClicked(item.id)) },
                        )
                    }
                }
            }
        }
    }

    if (state.isFilterSheetOpen) {
        ItemFilterSheet(
            availableTags = state.availableTags,
            selectedTags = state.selectedTags,
            onTagToggled = { onEvent(ItemListEvent.TagToggled(it)) },
            onClearFilters = { onEvent(ItemListEvent.FiltersCleared) },
            onDismiss = { onEvent(ItemListEvent.FilterSheetDismissed) },
        )
    }
}

@Composable
private fun ItemRow(
    item: Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        HextechFrame(
            model = DataDragonUrls.itemIcon(item.patchVersion, item.imageFileName),
            contentDescription = stringResource(R.string.item_icon, item.name),
            modifier = Modifier.size(AppTheme.dimens.abilityIcon),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.plaintext.isNotBlank()) {
                Text(
                    text = item.plaintext,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Text(
            text = stringResource(R.string.item_cost, item.gold.total),
            style = AppTheme.typography.statValue,
            color = AppTheme.colors.primary,
        )
    }
}
