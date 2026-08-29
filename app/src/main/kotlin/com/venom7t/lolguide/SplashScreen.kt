package com.venom7t.lolguide

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.venom7t.lolguide.presentation.R
import com.venom7t.lolguide.presentation.theme.AppTheme

/**
 * Shown for the one deliberate blocking gap in the launch path -- resolving
 * whether this is a fresh install (needs sign-in / onboarding) before the
 * NavHost's start destination can be chosen (see LolGuideNavGraph's doc
 * comment). A bare spinner there read as a stall; this gives that same wait
 * a wordmark and a hairline reveal so it reads as the app opening, not
 * loading.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }

    val alpha by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = EaseOutCubic),
        label = "splash_alpha",
    )
    val ruleWidth by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 150, easing = EaseOutCubic),
        label = "splash_rule",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.home_wordmark),
                style = AppTheme.typography.displayLarge,
                color = AppTheme.colors.primary,
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(120.dp * ruleWidth)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, AppTheme.colors.border, Color.Transparent),
                        ),
                    ),
            )
        }
    }
}
