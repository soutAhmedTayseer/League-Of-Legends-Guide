package com.venom7t.lolguide.presentation.summoner.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.common.components.SectionRule
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun SummonerSearchScreenRoot(
    onNavigateToProfile: (name: String, tagline: String, region: Region) -> Unit,
    onNavigateToAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummonerSearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.onEvent(SummonerSearchEvent.ScreenOpened) }
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
        onNavigateToAccount = onNavigateToAccount,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummonerSearchScreen(
    state: SummonerSearchState,
    onEvent: (SummonerSearchEvent) -> Unit,
    onNavigateToAccount: () -> Unit,
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
                    Text(
                        text = stringResource(R.string.summoner_search_title),
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToAccount) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.account_title),
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
                .padding(padding)
                .padding(AppTheme.dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            CutSurface(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        shape = AppTheme.shapes.medium,
                        colors = summonerFieldColors(),
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
                            shape = AppTheme.shapes.medium,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionMenuExpanded)
                            },
                            colors = summonerFieldColors(),
                        )
                        DropdownMenu(
                            expanded = regionMenuExpanded,
                            onDismissRequest = { regionMenuExpanded = false },
                            containerColor = AppTheme.colors.surface,
                        ) {
                            Region.entries.forEach { region ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = region.name,
                                            style = AppTheme.typography.bodyMedium,
                                            color = AppTheme.colors.textPrimary,
                                        )
                                    },
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
                            Text(
                                text = stringResource(R.string.summoner_search_action),
                                style = AppTheme.typography.label,
                            )
                        }
                    }

                    state.error?.let {
                        Text(
                            text = it.asString(),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.error,
                        )
                    }
                }
            }

            if (state.recentSearches.isNotEmpty()) {
                SectionRule(title = stringResource(R.string.summoner_search_recent))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                ) {
                    items(state.recentSearches) { recent ->
                        CutSurface(onClick = { onEvent(SummonerSearchEvent.RecentSearchClicked(recent)) }) {
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

@Composable
private fun summonerFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppTheme.colors.textPrimary,
    unfocusedTextColor = AppTheme.colors.textPrimary,
    focusedBorderColor = AppTheme.colors.primary,
    unfocusedBorderColor = AppTheme.colors.border,
    focusedLabelColor = AppTheme.colors.primary,
    unfocusedLabelColor = AppTheme.colors.textSecondary,
    cursorColor = AppTheme.colors.primary,
    focusedPlaceholderColor = AppTheme.colors.textDisabled,
    unfocusedPlaceholderColor = AppTheme.colors.textDisabled,
    focusedTrailingIconColor = AppTheme.colors.textSecondary,
    unfocusedTrailingIconColor = AppTheme.colors.textSecondary,
)
