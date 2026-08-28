package com.venom7t.lolguide.presentation.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.venom7t.lolguide.presentation.R

/**
 * Cinzel -- an inscriptional Roman serif, the closest free relative of the
 * League client's own Beaufort. Used for titles, eyebrows and tile labels
 * only: it has the personality but not the legibility for running text.
 *
 * Both faces here are **variable** fonts, so one file covers every weight.
 * [FontVariation] needs API 26; on 24-25 the file still loads and renders at
 * its default instance, which is a weight difference rather than a failure.
 */
@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

internal val DisplayFontFamily = FontFamily(
    variableFont(R.font.cinzel_variable, FontWeight.Medium),
    variableFont(R.font.cinzel_variable, FontWeight.SemiBold),
    variableFont(R.font.cinzel_variable, FontWeight.Bold),
)

/** Inter for everything the user actually reads. */
internal val BodyFontFamily = FontFamily(
    variableFont(R.font.inter_variable, FontWeight.Normal),
    variableFont(R.font.inter_variable, FontWeight.Medium),
    variableFont(R.font.inter_variable, FontWeight.SemiBold),
    variableFont(R.font.inter_variable, FontWeight.Bold),
)

/**
 * The type scale. Composables read these instead of building ad-hoc
 * `TextStyle`s with raw `sp` values (AGENTS.md §9).
 *
 * Sizes are in `sp` so they scale with the user's font-size setting; nothing
 * here may be converted to `dp` to "keep the layout tidy".
 */
data class AppTypography(
    val displayLarge: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val statValue: TextStyle,
    /** Small, wide, uppercase Cinzel. Sits above a section as its rule label. */
    val eyebrow: TextStyle,
    /** Uppercase Cinzel for tile and button labels. */
    val tileLabel: TextStyle,
)

/**
 * @param isRtl drops the wide tracking from the display styles. Latin small
 *   caps want the extra air, but Arabic is cursive -- letter-spacing pulls
 *   the joins apart and renders it as disconnected letterforms. Neither face
 *   above ships Arabic glyphs either, so Arabic falls through to the system
 *   face by glyph fallback and only the spacing needs correcting here.
 */
internal fun appTypography(isRtl: Boolean = false) = AppTypography(
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = if (isRtl) 0.em else 0.01.em,
    ),
    // Body face, not display: this labels champion rows and cards, where
    // Cinzel's inscriptional caps would fight the content rather than frame it.
    titleMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontSize = 15.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontSize = 13.5.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    label = TextStyle(
        fontFamily = BodyFontFamily,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    ),
    caption = TextStyle(
        fontFamily = BodyFontFamily,
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
    ),
    // Tabular figures matter here: stat columns, LP and countdowns must not
    // jitter as digits change width.
    statValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    eyebrow = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = if (isRtl) 0.em else 0.3.em,
    ),
    tileLabel = TextStyle(
        fontFamily = DisplayFontFamily,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = if (isRtl) 0.em else 0.09.em,
    ),
)
