package com.venom7t.lolguide.presentation.summoner.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.match.model.MatchSummary
import com.venom7t.lolguide.domain.summoner.model.RankedEntry
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun SummonerProfileScreenRoot(
    onNavigateToMatchDetail: (matchId: String, viewingPuuid: String) -> Unit,
    onNavigateToLiveGame: (puuid: String) -> Unit,
    onNavigateToMasteries: (puuid: String) -> Unit,
    onNavigateToLpHistory: (puuid: String, riotIdName: String, riotIdTagline: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummonerProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.onEvent(SummonerProfileEvent.ScreenOpened) }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SummonerProfileEffect.NavigateToMatchDetail ->
                    onNavigateToMatchDetail(effect.matchId, effect.viewingPuuid)
                is SummonerProfileEffect.NavigateToLiveGame -> onNavigateToLiveGame(effect.puuid)
                is SummonerProfileEffect.NavigateToMasteries -> onNavigateToMasteries(effect.puuid)
                is SummonerProfileEffect.NavigateToLpHistory ->
                    onNavigateToLpHistory(effect.puuid, effect.riotIdName, effect.riotIdTagline)
                is SummonerProfileEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    SummonerProfileScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummonerProfileScreen(
    state: SummonerProfileState,
    onEvent: (SummonerProfileEvent) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.summoner?.riotId.orEmpty()) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                    titleContentColor = AppTheme.colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingContent(modifier = Modifier.padding(padding))
            state.error != null -> ErrorContent(
                message = state.error,
                onRetry = { onEvent(SummonerProfileEvent.Retry) },
                modifier = Modifier.padding(padding),
            )
            state.summoner != null -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(AppTheme.dimens.spaceMd),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
            ) {
                item {
                    SummonerHeader(
                        summoner = state.summoner,
                        patchVersion = state.patchVersion,
                        isFollowed = state.isFollowed,
                        isInLiveGame = state.isInLiveGame,
                        onFollowClick = { onEvent(SummonerProfileEvent.FollowClicked) },
                        onLiveGameClick = { onEvent(SummonerProfileEvent.LiveGameClicked) },
                    )
                }

                if (state.rankedEntries.isNotEmpty()) {
                    items(state.rankedEntries) { entry -> RankedEntryCard(entry) }
                    item {
                        Text(
                            text = stringResource(R.string.summoner_profile_lp_history),
                            style = AppTheme.typography.label,
                            color = AppTheme.colors.accent,
                            modifier = Modifier.clickable {
                                onEvent(SummonerProfileEvent.LpHistoryClicked)
                            },
                        )
                    }
                }

                if (state.clashTeam != null) {
                    item { ClashCard(state.clashTeam) }
                }

                if (state.duoStats.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.duo_stats_title),
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.textPrimary,
                        )
                    }
                    items(state.duoStats) { duo -> DuoStatsRow(duo) }
                }

                if (state.topMasteries.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.summoner_profile_masteries),
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.clickable {
                                onEvent(SummonerProfileEvent.MasteriesClicked)
                            },
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.summoner_profile_match_history),
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.textPrimary,
                    )
                }

                items(state.matches) { match ->
                    MatchRow(
                        match = match,
                        patchVersion = state.patchVersion,
                        onClick = { onEvent(SummonerProfileEvent.MatchClicked(match.matchId)) },
                    )
                }

                if (state.matches.isNotEmpty()) {
                    item {
                        Button(
                            onClick = { onEvent(SummonerProfileEvent.LoadMoreMatches) },
                            enabled = !state.isLoadingMoreMatches,
                            modifier = Modifier.fillMaxWidth(),
                            shape = AppTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.surface,
                                contentColor = AppTheme.colors.textPrimary,
                            ),
                        ) {
                            if (state.isLoadingMoreMatches) {
                                CircularProgressIndicator(color = AppTheme.colors.accent)
                            } else {
                                Text(stringResource(R.string.summoner_profile_load_more))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummonerHeader(
    summoner: Summoner,
    patchVersion: String?,
    isFollowed: Boolean,
    isInLiveGame: Boolean,
    onFollowClick: () -> Unit,
    onLiveGameClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        shape = AppTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(AppTheme.dimens.spaceMd)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (patchVersion != null) {
                    AsyncImage(
                        model = DataDragonUrls.profileIcon(patchVersion, summoner.profileIconId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(AppTheme.dimens.championThumb)
                            .background(AppTheme.colors.surfaceElevated, CircleShape),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = AppTheme.dimens.spaceMd),
                ) {
                    Text(
                        text = summoner.riotId,
                        style = AppTheme.typography.titleLarge,
                        color = AppTheme.colors.textPrimary,
                    )
                    Text(
                        text = stringResource(
                            R.string.summoner_profile_level,
                            summoner.summonerLevel,
                        ),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textSecondary,
                    )
                }
                IconButton(onClick = onFollowClick) {
                    Icon(
                        imageVector = if (isFollowed) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(
                            if (isFollowed) {
                                R.string.summoner_profile_unfollow
                            } else {
                                R.string.summoner_profile_follow
                            },
                        ),
                        tint = if (isFollowed) AppTheme.colors.primary else AppTheme.colors.textDisabled,
                    )
                }
            }

            if (isInLiveGame) {
                Button(
                    onClick = onLiveGameClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppTheme.dimens.spaceMd),
                    shape = AppTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.accent,
                        contentColor = AppTheme.colors.onPrimary,
                    ),
                ) {
                    Text(stringResource(R.string.summoner_profile_in_live_game))
                }
            }
        }
    }
}

@Composable
private fun ClashCard(team: com.venom7t.lolguide.domain.clash.model.ClashTeam) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        shape = AppTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(AppTheme.dimens.spaceMd)) {
            Text(
                text = stringResource(R.string.clash_title),
                style = AppTheme.typography.label,
                color = AppTheme.colors.textSecondary,
            )
            Text(
                text = team.name,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.clash_tier, team.tier),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun DuoStatsRow(duo: com.venom7t.lolguide.domain.match.model.DuoStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        shape = AppTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${duo.teammateRiotIdName}#${duo.teammateRiotIdTagline}",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.duo_stats_win_rate, duo.winRatePercent, duo.sampleSize),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun RankedEntryCard(entry: RankedEntry) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        shape = AppTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = entry.queueType.name,
                    style = AppTheme.typography.label,
                    color = AppTheme.colors.textSecondary,
                )
                Text(
                    text = "${entry.tier} ${entry.rank} · ${entry.leaguePoints} LP",
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                )
            }
            Text(
                text = stringResource(
                    R.string.summoner_profile_win_loss,
                    entry.wins,
                    entry.losses,
                    entry.winRatePercent,
                ),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun MatchRow(
    match: MatchSummary,
    patchVersion: String?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (match.win) {
                AppTheme.colors.surfaceElevated
            } else {
                AppTheme.colors.surface
            },
        ),
        shape = AppTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            if (patchVersion != null) {
                AsyncImage(
                    model = DataDragonUrls.championIconById(patchVersion, match.championId),
                    contentDescription = match.championId,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(AppTheme.dimens.championThumb)
                        .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.championId,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = "${match.kills}/${match.deaths}/${match.assists}",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
            Text(
                text = stringResource(
                    if (match.win) R.string.summoner_profile_victory else R.string.summoner_profile_defeat,
                ),
                style = AppTheme.typography.label,
                color = if (match.win) AppTheme.colors.accent else AppTheme.colors.error,
            )
        }
    }
}
