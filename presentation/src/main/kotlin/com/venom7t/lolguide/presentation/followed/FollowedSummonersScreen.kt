package com.venom7t.lolguide.presentation.followed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.components.FollowedSummonerSkeleton
import com.venom7t.lolguide.presentation.common.components.HextechFrame
import com.venom7t.lolguide.presentation.common.components.rememberMinimumVisibleLoading
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun FollowedSummonersScreenRoot(
    onNavigateToProfile: (name: String, tagline: String, region: Region) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FollowedSummonersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(FollowedSummonersEvent.ScreenOpened) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FollowedSummonersEffect.NavigateToProfile ->
                    onNavigateToProfile(effect.riotIdName, effect.riotIdTagline, effect.region)
                FollowedSummonersEffect.NavigateBack -> onBack()
            }
        }
    }

    FollowedSummonersScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowedSummonersScreen(
    state: FollowedSummonersState,
    onEvent: (FollowedSummonersEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.followed_summoners_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(FollowedSummonersEvent.BackClicked) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background,
                    titleContentColor = AppTheme.colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        if (rememberMinimumVisibleLoading(state.isLoading)) {
            FollowedSummonerSkeleton(modifier = Modifier.padding(padding))
        } else if (state.followed.isEmpty()) {
            EmptyContent(
                message = uiText(R.string.followed_summoners_empty),
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                items(state.followed) { summoner ->
                    FollowedSummonerRow(
                        summoner = summoner,
                        resolved = state.resolvedProfiles[summoner.puuid],
                        patchVersion = state.patchVersion,
                        onClick = { onEvent(FollowedSummonersEvent.SummonerClicked(summoner)) },
                        onUnfollow = { onEvent(FollowedSummonersEvent.UnfollowClicked(summoner.puuid)) },
                    )
                }
            }
        }
    }
}

/**
 * Same shape as the summoner profile header card -- a portrait mount, name
 * stacked over a secondary line, trailing action -- so a followed summoner
 * reads as the same kind of object everywhere it appears. [resolved] is
 * fetched lazily per row once the screen opens (the followed list itself is
 * small, unlike the ladder); until it arrives, or if the lookup fails, this
 * falls back to a generic portrait and the region instead of the level
 * (AGENTS.md §8.2 -- a degraded state, not a broken one).
 */
@Composable
private fun FollowedSummonerRow(
    summoner: FollowedSummoner,
    resolved: Summoner?,
    patchVersion: String?,
    onClick: () -> Unit,
    onUnfollow: () -> Unit,
) {
    CutSurface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
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
                    modifier = Modifier.size(AppTheme.dimens.championThumb),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(AppTheme.dimens.championThumb)
                        .clip(AppTheme.shapes.small)
                        .background(AppTheme.colors.surfaceElevated)
                        .border(AppTheme.dimens.borderWidth, AppTheme.colors.primary, AppTheme.shapes.small),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = AppTheme.colors.primary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${summoner.riotIdName}#${summoner.riotIdTagline}",
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = resolved?.let {
                        stringResource(R.string.summoner_profile_level, it.summonerLevel)
                    } ?: summoner.region.name,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
            IconButton(onClick = onUnfollow) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.summoner_profile_unfollow),
                    tint = AppTheme.colors.textDisabled,
                )
            }
        }
    }
}
