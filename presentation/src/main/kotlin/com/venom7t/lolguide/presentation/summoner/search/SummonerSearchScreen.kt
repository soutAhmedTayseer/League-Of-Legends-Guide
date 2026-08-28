package com.venom7t.lolguide.presentation.summoner.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun SummonerSearchScreenRoot(
    onNavigateToProfile: (name: String, tagline: String, region: Region) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummonerSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SummonerSearchEffect.NavigateToProfile ->
                    onNavigateToProfile(effect.riotIdName, effect.riotIdTagline, effect.region)
                is SummonerSearchEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    SummonerSearchScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummonerSearchScreen(
    state: SummonerSearchState,
    onEvent: (SummonerSearchEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.summoner_search_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surface,
                    titleContentColor = AppTheme.colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(SummonerSearchEvent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.summoner_search_hint)) },
                placeholder = { Text("Name#TAG") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.colors.textPrimary,
                    unfocusedTextColor = AppTheme.colors.textPrimary,
                ),
            )

            var regionMenuExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = regionMenuExpanded,
                onExpandedChange = { regionMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = state.region.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.summoner_search_region)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionMenuExpanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppTheme.colors.textPrimary,
                        unfocusedTextColor = AppTheme.colors.textPrimary,
                    ),
                )
                DropdownMenu(
                    expanded = regionMenuExpanded,
                    onDismissRequest = { regionMenuExpanded = false },
                ) {
                    Region.entries.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region.name) },
                            onClick = {
                                onEvent(SummonerSearchEvent.RegionSelected(region))
                                regionMenuExpanded = false
                            },
                        )
                    }
                }
            }

            Button(
                onClick = { onEvent(SummonerSearchEvent.SearchClicked) },
                enabled = state.canSearch,
                modifier = Modifier.fillMaxWidth(),
                shape = AppTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primary,
                    contentColor = AppTheme.colors.onPrimary,
                ),
            ) {
                if (state.isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppTheme.colors.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.summoner_search_action), style = AppTheme.typography.label)
                }
            }

            state.error?.let {
                Text(
                    text = it.asString(),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.error,
                )
            }

            if (state.recentSearches.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.summoner_search_recent),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                ) {
                    items(state.recentSearches) { recent ->
                        Card(
                            onClick = { onEvent(SummonerSearchEvent.RecentSearchClicked(recent)) },
                            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                            shape = AppTheme.shapes.medium,
                        ) {
                            Text(
                                text = "${recent.riotIdName}#${recent.riotIdTagline}",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.textPrimary,
                                modifier = Modifier.padding(AppTheme.dimens.spaceMd),
                            )
                        }
                    }
                }
            }
        }
    }
}
