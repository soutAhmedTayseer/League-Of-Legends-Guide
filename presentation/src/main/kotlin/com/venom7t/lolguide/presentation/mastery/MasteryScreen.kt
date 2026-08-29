package com.venom7t.lolguide.presentation.mastery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.mastery.model.ChampionMastery
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun MasteryScreenRoot(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MasteryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(MasteryEvent.ScreenOpened) }
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                MasteryEffect.NavigateBack -> onBack()
            }
        }
    }

    MasteryScreen(state = state, onEvent = viewModel::onEvent, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasteryScreen(
    state: MasteryState,
    onEvent: (MasteryEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mastery_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(MasteryEvent.BackClicked) }) {
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
                onRetry = { onEvent(MasteryEvent.Retry) },
                modifier = Modifier.padding(padding),
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(AppTheme.dimens.spaceMd),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                items(state.masteries) { mastery -> MasteryCell(mastery, state.patchVersion) }
            }
        }
    }
}

@Composable
private fun MasteryCell(mastery: ChampionMastery, patchVersion: String?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        shape = AppTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceSm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (patchVersion != null) {
                AsyncImage(
                    model = DataDragonUrls.championIconById(patchVersion, mastery.championId),
                    contentDescription = mastery.championId,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
                )
            }
            Text(
                text = mastery.championId,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.mastery_level, mastery.championLevel),
                style = AppTheme.typography.label,
                color = AppTheme.colors.accent,
            )
            Text(
                text = stringResource(R.string.mastery_points, mastery.championPoints),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}
