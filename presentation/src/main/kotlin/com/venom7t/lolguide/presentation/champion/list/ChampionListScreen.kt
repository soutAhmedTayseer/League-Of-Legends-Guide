package com.venom7t.lolguide.presentation.champion.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.components.ChampionRow
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.ChampionListSkeleton
import com.venom7t.lolguide.presentation.common.components.rememberMinimumVisibleLoading
import com.venom7t.lolguide.presentation.common.components.PatchBadge
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun ChampionListScreenRoot(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChampionListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChampionListEvent.ScreenOpened)
    }

    // Effects are consumed once. Collecting them here rather than holding them
    // in state is what stops a navigation or snackbar replaying on rotation.
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ChampionListEffect.NavigateToDetail -> onNavigateToDetail(effect.championId)
                is ChampionListEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    ChampionListScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToCompare = onNavigateToCompare,
        onNavigateToRoulette = onNavigateToRoulette,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionListScreen(
    state: ChampionListState,
    onEvent: (ChampionListEvent) -> Unit,
    onNavigateToCompare: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                    ) {
                        Text(
                            text = stringResource(R.string.champion_list_title),
                            style = AppTheme.typography.titleLarge,
                            color = AppTheme.colors.textPrimary,
                        )
                        state.patchVersion?.let { version ->
                            PatchBadge(version = version, isStale = state.isPatchStale)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCompare) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = stringResource(R.string.compare_open),
                            tint = AppTheme.colors.textSecondary,
                        )
                    }
                    IconButton(onClick = onNavigateToRoulette) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = stringResource(R.string.roulette_open),
                            tint = AppTheme.colors.textSecondary,
                        )
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
            SearchField(
                query = state.query,
                onQueryChange = { onEvent(ChampionListEvent.QueryChanged(it)) },
                onClear = { onEvent(ChampionListEvent.QueryCleared) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AppTheme.dimens.spaceMd,
                        end = AppTheme.dimens.spaceMd,
                        top = AppTheme.dimens.spaceMd,
                    ),
            )

            ActiveFilterBar(
                filter = state.filter,
                onOpenFilters = { onEvent(ChampionListEvent.FilterSheetOpened) },
                onClearFilters = { onEvent(ChampionListEvent.FiltersCleared) },
                modifier = Modifier.padding(
                    horizontal = AppTheme.dimens.spaceMd,
                    vertical = AppTheme.dimens.spaceSm,
                ),
            )

            val showSkeleton = rememberMinimumVisibleLoading(state.isLoading)
            when {
                showSkeleton -> ChampionListSkeleton()

                state.error != null && state.champions.isEmpty() -> ErrorContent(
                    message = state.error,
                    onRetry = { onEvent(ChampionListEvent.Retry) },
                )

                // A search or filter matching nothing is a different situation
                // from having no data at all, and says so (AGENTS.md §13).
                state.hasNoResults -> EmptyContent(
                    message = if (state.query.isNotBlank()) {
                        uiText(R.string.champion_list_no_results, state.query)
                    } else {
                        uiText(R.string.roulette_empty_pool)
                    },
                )

                state.isEmpty -> EmptyContent(message = uiText(R.string.champion_list_empty))

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AppTheme.dimens.spaceMd,
                        vertical = AppTheme.dimens.spaceSm,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                ) {
                    items(items = state.champions, key = { it.id }) { champion ->
                        ChampionRow(
                            champion = champion,
                            isFavourite = champion.id in state.favouriteIds,
                            onClick = {
                                onEvent(ChampionListEvent.ChampionClicked(champion.id))
                            },
                            onFavouriteClick = {
                                onEvent(ChampionListEvent.FavouriteToggled(champion.id))
                            },
                        )
                    }
                }
            }
        }

        if (state.isFilterSheetOpen) {
            ChampionFilterSheet(
                filter = state.filter,
                availableResources = state.availableResources,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        shape = AppTheme.shapes.medium,
        textStyle = AppTheme.typography.bodyMedium,
        placeholder = {
            Text(
                text = stringResource(R.string.champion_list_search_hint),
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
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.champion_list_clear_search),
                        tint = AppTheme.colors.textSecondary,
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppTheme.colors.textPrimary,
            unfocusedTextColor = AppTheme.colors.textPrimary,
            focusedBorderColor = AppTheme.colors.primary,
            unfocusedBorderColor = AppTheme.colors.border,
            cursorColor = AppTheme.colors.accent,
        ),
    )
}
