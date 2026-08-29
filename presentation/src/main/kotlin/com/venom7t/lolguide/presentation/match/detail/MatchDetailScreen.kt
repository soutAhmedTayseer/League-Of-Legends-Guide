package com.venom7t.lolguide.presentation.match.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.match.model.MatchDetail
import com.venom7t.lolguide.domain.match.model.MatchParticipant
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.DetailHeaderSkeleton
import com.venom7t.lolguide.presentation.common.components.HextechFrame
import com.venom7t.lolguide.presentation.common.components.rememberMinimumVisibleLoading
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun MatchDetailScreenRoot(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(MatchDetailEvent.ScreenOpened) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                MatchDetailEffect.NavigateBack -> onBack()
            }
        }
    }

    MatchDetailScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    state: MatchDetailState,
    onEvent: (MatchDetailEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(MatchDetailEvent.BackClicked) }) {
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
        val showSkeleton = rememberMinimumVisibleLoading(state.isLoading)
        when {
            showSkeleton -> DetailHeaderSkeleton(modifier = Modifier.padding(padding))
            state.error != null -> ErrorContent(
                message = state.error,
                onRetry = { onEvent(MatchDetailEvent.Retry) },
                modifier = Modifier.padding(padding),
            )
            state.detail != null -> MatchDetailContent(
                detail = state.detail,
                viewingPuuid = state.viewingPuuid,
                patchVersion = state.patchVersion,
                championsByKey = state.championsByKey,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun MatchDetailContent(
    detail: MatchDetail,
    viewingPuuid: String,
    patchVersion: String?,
    championsByKey: Map<String, Champion>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        item {
            Text(
                text = stringResource(R.string.match_detail_blue_team),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
        }
        items(detail.blueTeam) { participant ->
            ParticipantRow(
                participant = participant,
                isViewingSummoner = participant.puuid == viewingPuuid,
                patchVersion = patchVersion,
                champion = championsByKey[participant.championId],
            )
        }
        item {
            Text(
                text = stringResource(R.string.match_detail_red_team),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
        }
        items(detail.redTeam) { participant ->
            ParticipantRow(
                participant = participant,
                isViewingSummoner = participant.puuid == viewingPuuid,
                patchVersion = patchVersion,
                champion = championsByKey[participant.championId],
            )
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: MatchParticipant,
    isViewingSummoner: Boolean,
    patchVersion: String?,
    champion: Champion?,
) {
    CutSurface(highlighted = isViewingSummoner, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceSm),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            if (patchVersion != null && champion != null) {
                HextechFrame(
                    model = DataDragonUrls.championIcon(patchVersion, champion.imageFileName),
                    contentDescription = champion.name,
                    modifier = Modifier.size(40.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${participant.riotIdName}#${participant.riotIdTagline}",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                )
                Text(
                    text = champion?.name ?: participant.championId,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary,
                )
            }
            Text(
                text = "${participant.kills}/${participant.deaths}/${participant.assists}",
                style = AppTheme.typography.statValue,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = "${participant.csTotal} CS",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}
