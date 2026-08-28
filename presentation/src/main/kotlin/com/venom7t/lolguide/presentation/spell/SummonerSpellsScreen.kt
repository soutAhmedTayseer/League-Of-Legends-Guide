package com.venom7t.lolguide.presentation.spell

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.spell.model.SummonerSpell
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.abilityText
import com.venom7t.lolguide.presentation.common.components.ErrorContent
import com.venom7t.lolguide.presentation.common.components.LoadingContent
import com.venom7t.lolguide.presentation.common.components.PatchBadge
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun SummonerSpellsScreenRoot(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummonerSpellsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(SummonerSpellsEvent.ScreenOpened) }

    SummonerSpellsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummonerSpellsScreen(
    state: SummonerSpellsState,
    onEvent: (SummonerSpellsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.champion_detail_back),
                            tint = AppTheme.colors.textPrimary,
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                    ) {
                        Text(
                            text = stringResource(R.string.spells_title),
                            style = AppTheme.typography.titleLarge,
                            color = AppTheme.colors.textPrimary,
                        )
                        state.patchVersion?.let { PatchBadge(version = it) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.background),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> LoadingContent()

                state.error != null -> ErrorContent(
                    message = state.error,
                    onRetry = { onEvent(SummonerSpellsEvent.Retry) },
                )

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.spell_rift_only),
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textDisabled,
                        modifier = Modifier.padding(AppTheme.dimens.spaceMd),
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = AppTheme.dimens.spaceMd,
                            vertical = AppTheme.dimens.spaceSm,
                        ),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
                    ) {
                        items(items = state.spells, key = { it.id }) { spell ->
                            SpellCard(spell = spell, patchVersion = state.patchVersion.orEmpty())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellCard(
    spell: SummonerSpell,
    patchVersion: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.surface, AppTheme.shapes.medium)
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.medium)
            .padding(AppTheme.dimens.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        AsyncImage(
            model = DataDragonUrls.spellIcon(
                version = spell.patchVersion.ifBlank { patchVersion },
                imageFileName = spell.imageFileName,
            ),
            contentDescription = null,
            modifier = Modifier
                .size(AppTheme.dimens.abilityIcon)
                .background(AppTheme.colors.surfaceElevated, AppTheme.shapes.small),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = spell.name,
                style = AppTheme.typography.titleMedium,
                color = AppTheme.colors.textPrimary,
            )
            Text(
                text = abilityText(spell.description),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textSecondary,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
            ) {
                Text(
                    text = stringResource(
                        R.string.spell_cooldown,
                        spell.cooldownSeconds.formatCompact(),
                    ),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.accent,
                )
                if (spell.requiredSummonerLevel > 1) {
                    Text(
                        text = stringResource(
                            R.string.spell_required_level,
                            spell.requiredSummonerLevel,
                        ),
                        style = AppTheme.typography.caption,
                        color = AppTheme.colors.textDisabled,
                    )
                }
            }
        }
    }
}

private fun Double.formatCompact(): String =
    if (this == toLong().toDouble()) toLong().toString() else toString()
