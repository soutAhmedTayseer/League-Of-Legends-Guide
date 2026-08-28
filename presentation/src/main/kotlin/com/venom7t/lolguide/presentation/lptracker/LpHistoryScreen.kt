package com.venom7t.lolguide.presentation.lptracker

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.venom7t.lolguide.domain.lptracker.model.LpSnapshot
import com.venom7t.lolguide.domain.summoner.model.RankedQueue
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.components.EmptyContent
import com.venom7t.lolguide.presentation.common.uiText
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun LpHistoryScreenRoot(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LpHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(LpHistoryEvent.ScreenOpened) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LpHistoryEffect.NavigateBack -> onBack()
            }
        }
    }

    LpHistoryScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LpHistoryScreen(
    state: LpHistoryState,
    onEvent: (LpHistoryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lp_history_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(LpHistoryEvent.BackClicked) }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.surface,
                    titleContentColor = AppTheme.colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.dimens.spaceMd),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                RankedQueue.entries.forEach { queue ->
                    FilterChip(
                        selected = state.queueType == queue,
                        onClick = { onEvent(LpHistoryEvent.QueueSelected(queue)) },
                        label = { Text(queue.name) },
                    )
                }
            }

            if (!state.hasHistory) {
                EmptyContent(
                    message = uiText(R.string.lp_history_empty),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                ) {
                    items(state.snapshots) { snapshot -> LpSnapshotRow(snapshot) }
                }
            }
        }
    }
}

@Composable
private fun LpSnapshotRow(snapshot: LpSnapshot) {
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
                text = "${snapshot.tier} ${snapshot.rank}",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = "${snapshot.leaguePoints} LP",
                style = AppTheme.typography.statValue,
                color = AppTheme.colors.accent,
            )
        }
    }
}
