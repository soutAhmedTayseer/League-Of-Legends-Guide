package com.venom7t.lolguide.presentation.followed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.components.EmptyContent
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
        if (state.followed.isEmpty()) {
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
                        onClick = { onEvent(FollowedSummonersEvent.SummonerClicked(summoner)) },
                        onUnfollow = { onEvent(FollowedSummonersEvent.UnfollowClicked(summoner.puuid)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowedSummonerRow(
    summoner: FollowedSummoner,
    onClick: () -> Unit,
    onUnfollow: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        shape = AppTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${summoner.riotIdName}#${summoner.riotIdTagline}",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
            )
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
