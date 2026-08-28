package com.venom7t.lolguide.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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
)

internal fun appTypography(fontFamily: FontFamily = FontFamily.Default) = AppTypography(
    displayLarge = TextStyle(
        fontFamily = fontFamily,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
    label = TextStyle(
        fontFamily = fontFamily,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium,
    ),
    caption = TextStyle(
        fontFamily = fontFamily,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
    ),
    // Tabular figures matter here: stat columns must not jitter as digits
    // change width between champions.
    statValue = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
)
