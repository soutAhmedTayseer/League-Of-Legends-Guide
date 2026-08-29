package com.venom7t.lolguide.presentation.ladder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.ladder.model.LadderEntry
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.HextechFrame
import com.venom7t.lolguide.presentation.common.components.LadderSkeleton
import com.venom7t.lolguide.presentation.common.components.rememberMinimumVisibleLoading
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun LadderScreenRoot(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LadderViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(LadderEvent.ScreenOpened) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LadderEffect.NavigateBack -> onBack()
            }
        }
    }

    LadderScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LadderScreen(
    state: LadderState,
    onEvent: (LadderEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ladder_title, state.region.name)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(LadderEvent.BackClicked) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    var regionMenuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { regionMenuExpanded = true }) {
                        Text(state.region.name, style = AppTheme.typography.label, color = AppTheme.colors.textPrimary)
                    }
                    DropdownMenu(
                        expanded = regionMenuExpanded,
                        onDismissRequest = { regionMenuExpanded = false },
                    ) {
                        Region.entries.forEach { region ->
                            DropdownMenuItem(
                                text = { Text(region.name) },
                                onClick = {
                                    onEvent(LadderEvent.RegionSelected(region))
                                    regionMenuExpanded = false
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                    titleContentColor = AppTheme.colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        val showSkeleton = rememberMinimumVisibleLoading(state.isLoading)
        when {
            showSkeleton -> LadderSkeleton(modifier = Modifier.padding(padding))
            state.error != null -> ErrorContent(
                message = state.error,
                onRetry = { onEvent(LadderEvent.Retry) },
                modifier = Modifier.padding(padding),
            )
            else -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onEvent(LadderEvent.Retry) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                ) {
                    items(state.entries, key = { it.puuid }) { entry ->
                        LadderRow(
                            entry = entry,
                            resolved = state.resolvedProfiles[entry.puuid],
                            patchVersion = state.patchVersion,
                        )
                    }
                }
            }
        }
    }
}

/**
 * [resolved] is only populated for the top rows (see
 * LadderViewModel.resolveTopEntries) -- a row past that cutoff, or one whose
 * lookup hasn't landed yet, falls back to rank and puuid-based placeholder
 * text with no icon rather than blocking on a fetch nobody asked for.
 */
@Composable
private fun LadderRow(
    entry: LadderEntry,
    resolved: Summoner?,
    patchVersion: String?,
) {
    CutSurface(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            if (resolved != null && patchVersion != null) {
                HextechFrame(
                    model = DataDragonUrls.profileIcon(patchVersion, resolved.profileIconId),
                    contentDescription = null,
                    modifier = Modifier.size(AppTheme.dimens.abilityIcon),
                )
            }
            Text(
                text = "#${entry.rank}  ${resolved?.riotId ?: entry.summonerName ?: "—"}",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${entry.leaguePoints} LP · ${entry.winRatePercent}%",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}
