package com.venom7t.lolguide.presentation.common.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.venom7t.lolguide.presentation.common.FirstRunGate
import com.venom7t.lolguide.presentation.theme.AppTheme
import kotlinx.coroutines.delay

/**
 * Keeps a loading skeleton visible for at least [minMillis] even if the real
 * load finishes sooner, so a fast cache hit doesn't flash the skeleton for
 * one frame -- a shimmer that shows and immediately vanishes reads as a
 * glitch, not as "this loaded fast". Once [isLoading] goes false the timer
 * commits; it does not restart on a later reload unless this call site's
 * key changes, since re-arming it on every recomposition would keep the
 * skeleton on screen forever during a fast-finishing load.
 *
 * The minimum only applies on the app's first-ever run ([FirstRunGate]).
 * After that, everything is cache-backed and usually loads fast; padding
 * every single visit to every screen out to two seconds forever would turn
 * a one-time polish into permanent friction, so later visits show the
 * skeleton for exactly as long as [isLoading] actually is.
 */
@Composable
fun rememberMinimumVisibleLoading(isLoading: Boolean, minMillis: Long = 2000L): Boolean {
    if (!FirstRunGate.isFirstRun) return isLoading

    var minDurationElapsed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(minMillis)
        minDurationElapsed = true
    }
    return isLoading || !minDurationElapsed
}

/**
 * A moving highlight band across [AppTheme.colors.surfaceElevated], the same
 * loading language on every screen so a skeleton always reads as "loading",
 * never as "this screen is just empty and grey".
 */
@Composable
fun Modifier.shimmer(shape: androidx.compose.ui.graphics.Shape = AppTheme.shapes.small): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )
    val base = AppTheme.colors.surfaceElevated
    val highlight = AppTheme.colors.border.copy(alpha = 0.35f)
    return this
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(base, highlight, base),
                start = Offset(translate, 0f),
                end = Offset(translate + 400f, 400f),
            ),
        )
}

/** One filled bar -- the atom every skeleton below is built from. */
@Composable
fun SkeletonBone(modifier: Modifier = Modifier, height: Dp = 14.dp) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .height(height)
            .shimmer(AppTheme.shapes.small),
    )
}

/** Mirrors [com.venom7t.lolguide.presentation.summoner.profile.SummonerHeader] plus a few match rows. */
@Composable
fun SummonerProfileSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        CutSurface(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.dimens.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .width(AppTheme.dimens.championThumb)
                        .height(AppTheme.dimens.championThumb)
                        .shimmer(androidx.compose.foundation.shape.CircleShape),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
                ) {
                    SkeletonBone(modifier = Modifier.fillMaxWidth(0.6f), height = 18.dp)
                    SkeletonBone(modifier = Modifier.fillMaxWidth(0.3f))
                }
            }
        }
        repeat(6) {
            CutSurface(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimens.spaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .width(AppTheme.dimens.championThumb)
                            .height(AppTheme.dimens.championThumb)
                            .shimmer(AppTheme.shapes.small),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
                    ) {
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.4f))
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.25f))
                    }
                }
            }
        }
    }
}

/** Mirrors a ladder row: rank, name, LP. */
@Composable
fun LadderSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        repeat(10) {
            CutSurface(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimens.spaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    SkeletonBone(modifier = Modifier.width(32.dp))
                    SkeletonBone(modifier = Modifier.weight(1f))
                    SkeletonBone(modifier = Modifier.width(64.dp))
                }
            }
        }
    }
}

/**
 * Mirrors [ChampionRow] -- shared by the Champion list, Favourites, and the
 * compare picker, so one skeleton covers all three.
 */
@Composable
fun ChampionListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        repeat(8) {
            CutSurface(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimens.spaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .width(AppTheme.dimens.championThumb)
                            .height(AppTheme.dimens.championThumb)
                            .shimmer(AppTheme.shapes.small),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
                    ) {
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.5f), height = 16.dp)
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.35f))
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.25f))
                    }
                }
            }
        }
    }
}

/** Mirrors an item row: a smaller icon than [ChampionRow]'s, name plus a couple lines of text. */
@Composable
fun ItemListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        repeat(8) {
            CutSurface(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimens.spaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .width(AppTheme.dimens.abilityIcon)
                            .height(AppTheme.dimens.abilityIcon)
                            .shimmer(AppTheme.shapes.small),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
                    ) {
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.5f), height = 16.dp)
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.3f))
                    }
                }
            }
        }
    }
}

/** Mirrors the champion/item detail header: a wide hero image plus a few stat bars. */
@Composable
fun DetailHeaderSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shimmer(AppTheme.shapes.large),
        )
        SkeletonBone(modifier = Modifier.fillMaxWidth(0.45f), height = 22.dp)
        SkeletonBone(modifier = Modifier.fillMaxWidth(0.7f))
        SkeletonBone(modifier = Modifier.fillMaxWidth(0.6f))
        SkeletonBone(modifier = Modifier.fillMaxWidth(0.8f))
    }
}

/** Mirrors a followed-summoner card: portrait, name, level. */
@Composable
fun FollowedSummonerSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppTheme.dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSm),
    ) {
        repeat(5) {
            CutSurface(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.dimens.spaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceMd),
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .width(AppTheme.dimens.championThumb)
                            .height(AppTheme.dimens.championThumb)
                            .shimmer(androidx.compose.foundation.shape.CircleShape),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceXs),
                    ) {
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.5f), height = 16.dp)
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.25f))
                    }
                }
            }
        }
    }
}
