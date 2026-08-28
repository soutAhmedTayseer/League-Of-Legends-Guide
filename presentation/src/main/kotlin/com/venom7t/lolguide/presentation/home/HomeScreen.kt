package com.venom7t.lolguide.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.common.DataDragonUrls
import com.venom7t.lolguide.presentation.common.components.CutSurface
import com.venom7t.lolguide.presentation.common.components.HextechFrame
import com.venom7t.lolguide.presentation.common.components.SectionRule
import com.venom7t.lolguide.presentation.theme.AppTheme

@Composable
fun HomeScreenRoot(
    onNavigateToWhatsNew: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTimers: () -> Unit,
    onNavigateToLadder: () -> Unit,
    onNavigateToFollowedSummoners: () -> Unit,
    onNavigateToGame: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.onEvent(HomeEvent.ScreenOpened) }

    HomeScreen(
        state = state,
        onNavigateToWhatsNew = onNavigateToWhatsNew,
        onNavigateToSimulator = onNavigateToSimulator,
        onNavigateToRoulette = onNavigateToRoulette,
        onNavigateToQuiz = onNavigateToQuiz,
        onNavigateToTimers = onNavigateToTimers,
        onNavigateToLadder = onNavigateToLadder,
        onNavigateToFollowedSummoners = onNavigateToFollowedSummoners,
        onNavigateToGame = onNavigateToGame,
        modifier = modifier,
    )
}

/**
 * No `Scaffold`/`TopAppBar`: the hero owns the top of the screen and the
 * page background runs edge to edge behind it. A separate app bar in a
 * different colour is what made this screen read as a settings list rather
 * than as the app's front door.
 */
@Composable
fun HomeScreen(
    state: HomeState,
    onNavigateToWhatsNew: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToTimers: () -> Unit,
    onNavigateToLadder: () -> Unit,
    onNavigateToFollowedSummoners: () -> Unit,
    onNavigateToGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(bottom = AppTheme.dimens.spaceLg),
    ) {
        item {
            HomeHero(
                featured = state.featured,
                eyebrowRes = if (state.isFeaturedFreeRotation) {
                    R.string.home_rotation_title
                } else {
                    R.string.home_daily_champion
                },
                patchVersion = state.patchVersion,
                isPatchStale = state.isPatchStale,
            )
        }

        if (state.hasWhatsNew) {
            item {
                WhatsNewBanner(
                    onClick = onNavigateToWhatsNew,
                    modifier = Modifier.padding(
                        start = AppTheme.dimens.spaceMd,
                        end = AppTheme.dimens.spaceMd,
                        top = AppTheme.dimens.spaceMd,
                    ),
                )
            }
        }

        val rotation = state.freeRotation
        if (!rotation.isNullOrEmpty()) {
            item {
                SectionRule(
                    title = stringResource(R.string.home_rotation_title),
                    trailing = stringResource(R.string.home_rotation_count, rotation.size),
                    modifier = Modifier.padding(
                        start = AppTheme.dimens.spaceMd,
                        end = AppTheme.dimens.spaceMd,
                        top = AppTheme.dimens.spaceLg,
                        bottom = AppTheme.dimens.spaceSm,
                    ),
                )
            }
            item { RotationStrip(champions = rotation) }
        }

        item {
            SectionRule(
                title = stringResource(R.string.home_quick_links_title),
                modifier = Modifier.padding(
                    start = AppTheme.dimens.spaceMd,
                    end = AppTheme.dimens.spaceMd,
                    top = AppTheme.dimens.spaceLg,
                    bottom = AppTheme.dimens.spaceSm,
                ),
            )
        }

        item {
            ToolGrid(
                onNavigateToGame = onNavigateToGame,
                onNavigateToSimulator = onNavigateToSimulator,
                onNavigateToTimers = onNavigateToTimers,
                onNavigateToRoulette = onNavigateToRoulette,
                onNavigateToQuiz = onNavigateToQuiz,
                onNavigateToLadder = onNavigateToLadder,
                modifier = Modifier.padding(horizontal = AppTheme.dimens.spaceMd),
            )
        }

        item {
            SectionRule(
                title = stringResource(R.string.home_summoners_title),
                modifier = Modifier.padding(
                    start = AppTheme.dimens.spaceMd,
                    end = AppTheme.dimens.spaceMd,
                    top = AppTheme.dimens.spaceLg,
                    bottom = AppTheme.dimens.spaceSm,
                ),
            )
        }

        item {
            CutSurface(
                onClick = onNavigateToFollowedSummoners,
                modifier = Modifier
                    .padding(horizontal = AppTheme.dimens.spaceMd)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(AppTheme.dimens.spaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = AppTheme.colors.primary,
                    )
                    Text(
                        text = stringResource(R.string.followed_summoners_open),
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = AppTheme.colors.accent,
                    )
                }
            }
        }
    }
}

/**
 * Full-bleed splash of the first free-rotation champion, with the wordmark
 * and account control floating over it and the page background dissolving
 * up into the art.
 *
 * Falls back to a flat gradient when the rotation is unavailable (no Riot
 * key, or offline) rather than showing a broken image or an empty band.
 */
@Composable
private fun HomeHero(
    featured: Champion?,
    eyebrowRes: Int,
    patchVersion: String?,
    isPatchStale: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Splash art is 1215x717. A band close to that ratio keeps the
            // champion's face in frame under a centre crop; a taller box
            // crops the art down to a strip of torso.
            .aspectRatio(1.32f),
    ) {
        if (featured != null) {
            AsyncImage(
                model = DataDragonUrls.championSplash(featured.id),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AppTheme.colors.surfaceElevated,
                                AppTheme.colors.background,
                            ),
                        ),
                    ),
            )
        }

        // Two scrims, both resolving to the page background so this works in
        // either theme. The lower one dissolves the art into the page so the
        // hero has no hard bottom edge, and carries the title block on a
        // near-solid base -- splash art is bright and busy, and text laid
        // straight onto it is unreadable at any weight. The upper one does
        // the same job for the wordmark.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to AppTheme.colors.background.copy(alpha = 0.72f),
                        0.22f to Color.Transparent,
                        0.42f to Color.Transparent,
                        0.74f to AppTheme.colors.background.copy(alpha = 0.94f),
                        1f to AppTheme.colors.background,
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                // The splash art behind this runs full-bleed under the status
                // bar; only the wordmark row needs to clear it.
                .statusBarsPadding()
                .padding(AppTheme.dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_wordmark),
                style = AppTheme.typography.tileLabel,
                color = AppTheme.colors.textPrimary,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = AppTheme.dimens.spaceMd,
                    end = AppTheme.dimens.spaceMd,
                    bottom = AppTheme.dimens.spaceSm,
                ),
        ) {
            if (featured != null) {
                Text(
                    text = stringResource(eyebrowRes),
                    style = AppTheme.typography.eyebrow,
                    color = AppTheme.colors.primary,
                )
                Spacer(Modifier.height(AppTheme.dimens.spaceXs))
                Text(
                    text = featured.name,
                    style = AppTheme.typography.displayLarge,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = featured.title,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(AppTheme.dimens.spaceSm))
            }
            if (patchVersion != null) {
                PatchChip(version = patchVersion, isStale = isPatchStale)
            }
        }
    }
}


@Composable
private fun PatchChip(version: String, isStale: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.pill)
            .background(AppTheme.colors.surface.copy(alpha = 0.82f))
            .border(AppTheme.dimens.borderWidth, AppTheme.colors.border, AppTheme.shapes.pill)
            .padding(horizontal = AppTheme.dimens.spaceSm, vertical = AppTheme.dimens.spaceXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(AppTheme.shapes.pill)
                .background(if (isStale) AppTheme.colors.warning else AppTheme.colors.accent),
        )
        Text(
            text = stringResource(R.string.patch_label, version),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun RotationStrip(champions: List<Champion>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = AppTheme.dimens.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        items(items = champions, key = { it.id }) { champion ->
            Column(
                modifier = Modifier.width(AppTheme.dimens.championThumb),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HextechFrame(
                    model = DataDragonUrls.championIcon(
                        champion.patchVersion,
                        champion.imageFileName,
                    ),
                    contentDescription = champion.name,
                    modifier = Modifier.size(AppTheme.dimens.championThumb),
                )
                Spacer(Modifier.height(AppTheme.dimens.spaceXs))
                Text(
                    text = champion.name,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * The six tools, two to a row.
 *
 * Every tile here is a real destination -- there are no filler cards, so the
 * grid never shows an empty slot waiting for content that does not exist.
 */
@Composable
private fun ToolGrid(
    onNavigateToGame: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToTimers: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToLadder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tools = listOf(
        Tool(Icons.Default.Extension, R.string.game_hub_title, R.string.home_tool_riddle, onNavigateToGame, true),
        Tool(Icons.Default.Calculate, R.string.simulator_open, R.string.home_tool_simulator, onNavigateToSimulator, false),
        Tool(Icons.Default.Timer, R.string.timers_open, R.string.home_tool_timers, onNavigateToTimers, false),
        Tool(Icons.Default.Casino, R.string.roulette_open, R.string.home_tool_roulette, onNavigateToRoulette, false),
        Tool(Icons.Default.Quiz, R.string.quiz_open, R.string.home_tool_quiz, onNavigateToQuiz, false),
        Tool(Icons.Default.Leaderboard, R.string.ladder_open, R.string.home_tool_ladder, onNavigateToLadder, false),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        tools.chunked(2).forEach { row ->
            Row(
                // Intrinsic min height ties the two tiles in a row together,
                // so a label that wraps to two lines grows its neighbour to
                // match instead of leaving a ragged step between them.
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
            ) {
                row.forEach { tool ->
                    ToolTile(
                        tool = tool,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                // Keeps a lone trailing tile at column width instead of
                // stretching it across the grid.
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private data class Tool(
    val icon: ImageVector,
    val labelRes: Int,
    val descriptionRes: Int,
    val onClick: () -> Unit,
    val highlighted: Boolean,
)

@Composable
private fun ToolTile(tool: Tool, modifier: Modifier = Modifier) {
    CutSurface(
        onClick = tool.onClick,
        highlighted = tool.highlighted,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(AppTheme.dimens.spaceMd)) {
            Icon(
                imageVector = tool.icon,
                contentDescription = null,
                tint = if (tool.highlighted) AppTheme.colors.accent else AppTheme.colors.primary,
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.height(AppTheme.dimens.spaceSm))
            Text(
                text = stringResource(tool.labelRes),
                style = AppTheme.typography.tileLabel,
                color = AppTheme.colors.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(tool.descriptionRes),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun WhatsNewBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CutSurface(onClick = onClick, highlighted = true, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(AppTheme.dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_whats_new_teaser),
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.home_whats_new_open),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AppTheme.colors.accent,
            )
        }
    }
}
