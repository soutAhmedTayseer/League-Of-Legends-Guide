package com.venom7t.lolguide.presentation.common

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import com.venom7t.lolguide.presentation.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Derives an accent colour from a champion's splash art.
 *
 * Returns the theme accent until extraction succeeds, so the UI never waits on
 * a network image to paint, and never flashes a colour that fails contrast.
 *
 * The extracted colour is only ever used for accents -- a border, a header
 * wash -- never for text or a background. Splash art is arbitrary and its
 * dominant colour cannot be trusted to contrast with anything, so tinting
 * body text with it would be an accessibility regression.
 */
@Composable
fun rememberSplashAccent(championId: String, skinNum: Int = 0): Color {
    val context = LocalContext.current
    val fallback = AppTheme.colors.accent
    val isLightTheme = AppTheme.colors.isLight

    var accent by remember(championId, skinNum) { mutableStateOf(fallback) }

    LaunchedEffect(championId, skinNum, isLightTheme) {
        val extracted = extractAccent(
            context = context,
            url = DataDragonUrls.championSplash(championId, skinNum),
            isLightTheme = isLightTheme,
        )
        if (extracted != null) accent = extracted
    }

    return accent
}

private suspend fun extractAccent(
    context: android.content.Context,
    url: String,
    isLightTheme: Boolean,
): Color? = withContext(Dispatchers.IO) {
    runCatching {
        val request = ImageRequest.Builder(context)
            .data(url)
            // Palette reads pixels back, which a hardware bitmap does not
            // allow. Without this the extraction throws on most devices.
            .allowHardware(false)
            .build()

        val result = ImageLoader(context).execute(request)
        val bitmap = (result as? SuccessResult)
            ?.image
            ?.let { it as? BitmapImage }
            ?.bitmap
            ?: return@runCatching null

        paletteAccent(bitmap, isLightTheme)
    }.onFailure { throwable ->
        // A missing splash or a decode failure is not worth surfacing: the
        // screen is perfectly usable with the theme accent.
        Timber.d(throwable, "Splash palette extraction failed for %s", url)
    }.getOrNull()
}

private fun paletteAccent(bitmap: Bitmap, isLightTheme: Boolean): Color? {
    val palette = Palette.from(bitmap)
        // Splash art is large; sampling smaller is enough for a dominant hue
        // and keeps this off the "slow" list.
        .resizeBitmapArea(SAMPLE_AREA)
        .generate()

    val swatch = if (isLightTheme) {
        palette.darkVibrantSwatch ?: palette.darkMutedSwatch ?: palette.dominantSwatch
    } else {
        palette.lightVibrantSwatch ?: palette.vibrantSwatch ?: palette.dominantSwatch
    } ?: return null

    val color = Color(swatch.rgb)

    // Reject anything that would disappear against the surface it accents.
    // A near-black accent on the dark theme is worse than no tint at all.
    val luminance = color.luminance()
    return when {
        !isLightTheme && luminance < MIN_DARK_THEME_LUMINANCE -> null
        isLightTheme && luminance > MAX_LIGHT_THEME_LUMINANCE -> null
        else -> color
    }
}

private const val SAMPLE_AREA = 160 * 160
private const val MIN_DARK_THEME_LUMINANCE = 0.15f
private const val MAX_LIGHT_THEME_LUMINANCE = 0.6f
