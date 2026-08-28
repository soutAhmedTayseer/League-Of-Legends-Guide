package com.venom7t.lolguide.presentation.match.detail

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.match.model.MatchDetail
import com.venom7t.lolguide.domain.match.model.MatchParticipant
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
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
        when {
            state.isLoading -> LoadingContent(modifier = Modifier.padding(padding))
            state.error != null -> ErrorContent(
                message = state.error,
                onRetry = { onEvent(MatchDetailEvent.Retry) },
                modifier = Modifier.padding(padding),
            )
            state.detail != null -> MatchDetailContent(
                detail = state.detail,
                viewingPuuid = state.viewingPuuid,
                patchVersion = state.patchVersion,
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
            ParticipantRow(participant, participant.puuid == viewingPuuid, patchVersion)
        }
        item {
            Text(
                text = stringResource(R.string.match_detail_red_team),
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
        }
        items(detail.redTeam) { participant ->
            ParticipantRow(participant, participant.puuid == viewingPuuid, patchVersion)
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: MatchParticipant,
    isViewingSummoner: Boolean,
    patchVersion: String?,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isViewingSummoner) AppTheme.colors.surfaceElevated else AppTheme.colors.surface,
        ),
        shape = AppTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceSm),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
        ) {
            if (patchVersion != null) {
                AsyncImage(
                    model = DataDragonUrls.championIconById(patchVersion, participant.championId),
                    contentDescription = participant.championId,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
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
                    text = participant.championId,
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
