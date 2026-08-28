package com.example.lolguide.presentation.champion.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.lolguide.domain.champion.model.Champion
import com.example.lolguide.presentation.R
import com.example.lolguide.presentation.common.DataDragonUrls
import com.example.lolguide.presentation.common.UiText
import com.example.lolguide.presentation.common.components.EmptyContent
import com.example.lolguide.presentation.common.components.ErrorContent
import com.example.lolguide.presentation.common.components.LoadingContent
import com.example.lolguide.presentation.common.components.PatchBadge
import com.example.lolguide.presentation.common.uiText
import com.example.lolguide.presentation.theme.AppTheme

@Composable
fun ChampionListScreenRoot(
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChampionListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChampionListEvent.ScreenOpened)
    }

    // Effects are consumed once. Collecting them here rather than in state is
    // what stops a navigation or a snackbar from replaying on rotation.
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
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionListScreen(
    state: ChampionListState,
    onEvent: (ChampionListEvent) -> Unit,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surface,
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
                    .padding(AppTheme.dimens.spaceMd),
            )

            when {
                state.isLoading -> LoadingContent()

                state.error != null && state.champions.isEmpty() -> ErrorContent(
                    message = state.error,
                    onRetry = { onEvent(ChampionListEvent.Retry) },
                )

                state.hasNoSearchResults -> EmptyContent(
                    message = uiText(R.string.champion_list_no_results, state.query),
                )

                state.isEmpty -> EmptyContent(
                    message = uiText(R.string.champion_list_empty),
                )

                else -> ChampionList(
                    champions = state.champions,
                    patchVersion = state.patchVersion,
                    onChampionClick = { onEvent(ChampionListEvent.ChampionClicked(it)) },
                )
            }
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

@Composable
private fun ChampionList(
    champions: List<Champion>,
    patchVersion: String?,
    onChampionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = AppTheme.dimens.spaceMd,
            vertical = AppTheme.dimens.spaceSm,
        ),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        items(items = champions, key = { it.id }) { champion ->
            ChampionRow(
                champion = champion,
                patchVersion = patchVersion,
                onClick = { onChampionClick(champion.id) },
            )
        }
    }
}

@Composable
private fun ChampionRow(
    champion: Champion,
    patchVersion: String?,
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
        // The icon is versioned art, so it is fetched on the champion's own
        // patch rather than whatever the screen happens to be showing.
        AsyncImage(
            model = DataDragonUrls.championIcon(
                version = champion.patchVersion.ifBlank { patchVersion.orEmpty() },
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
                Text(
                    text = champion.tags.joinToString(" · ") { it.raw },
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
