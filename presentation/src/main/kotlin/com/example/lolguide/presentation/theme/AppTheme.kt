package com.example.lolguide.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppShapes(
    val small: Shape,
    val medium: Shape,
    val large: Shape,
    val pill: Shape,
)

data class AppDimens(
    val spaceXs: Dp,
    val spaceSm: Dp,
    val spaceMd: Dp,
    val spaceLg: Dp,
    val spaceXl: Dp,
    val borderWidth: Dp,
    val championThumb: Dp,
    val abilityIcon: Dp,
)

internal val defaultShapes = AppShapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp),
    pill = RoundedCornerShape(percent = 50),
)

internal val defaultDimens = AppDimens(
    spaceXs = 4.dp,
    spaceSm = 8.dp,
    spaceMd = 16.dp,
    spaceLg = 24.dp,
    spaceXl = 32.dp,
    borderWidth = 1.dp,
    championThumb = 64.dp,
    abilityIcon = 48.dp,
)

private val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("AppColors accessed outside of AppTheme. Wrap the content in AppTheme { }.")
}
private val LocalAppTypography = staticCompositionLocalOf<AppTypography> {
    error("AppTypography accessed outside of AppTheme. Wrap the content in AppTheme { }.")
}
private val LocalAppShapes = staticCompositionLocalOf { defaultShapes }
private val LocalAppDimens = staticCompositionLocalOf { defaultDimens }

/**
 * The project's design system.
 *
 * Material 3 is still installed underneath so that Material components
 * (`Scaffold`, `TopAppBar`, ripples) render sensibly, but app code reads
 * `AppTheme.colors` / `.typography` / `.shapes` / `.dimens` rather than
 * `MaterialTheme.*` (AGENTS.md §9).
 */
@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (useDarkTheme) DarkAppColors else LightAppColors
    val typography = appTypography()

    // Keep the Material scheme in step with our own roles so that any Material
    // component we do not skin still lands in the right palette.
    val materialScheme = if (useDarkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            secondary = colors.accent,
            onSecondary = colors.onAccent,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            error = colors.error,
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            secondary = colors.accent,
            onSecondary = colors.onAccent,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            error = colors.error,
        )
    }

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
        LocalAppShapes provides defaultShapes,
        LocalAppDimens provides defaultDimens,
    ) {
        MaterialTheme(colorScheme = materialScheme, content = content)
    }
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current

    val typography: AppTypography
        @Composable @ReadOnlyComposable get() = LocalAppTypography.current

    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = LocalAppShapes.current

    val dimens: AppDimens
        @Composable @ReadOnlyComposable get() = LocalAppDimens.current
}
