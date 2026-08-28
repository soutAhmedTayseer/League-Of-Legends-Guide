package com.example.lolguide.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * The raw palette. Nothing outside this file may name a colour literal
 * (AGENTS.md §9) -- Composables read semantic roles off [AppColors] instead,
 * which is what keeps light and dark from drifting apart.
 *
 * The hues are League's own client palette: hextech gold on deep blue-black,
 * with teal as the interactive accent.
 */
internal object Palette {
    // Blues -- the client's background family, darkest first.
    val Abyss = Color(0xFF010A13)
    val MidnightNavy = Color(0xFF0A1428)
    val DeepNavy = Color(0xFF091428)
    val SlateNavy = Color(0xFF1E2328)
    val Steel = Color(0xFF3C3C41)
    val Fog = Color(0xFF5B5A56)

    // Golds -- primary accent, used for emphasis and selection.
    val GoldBright = Color(0xFFF0E6D2)
    val Gold = Color(0xFFC8AA6E)
    val GoldDeep = Color(0xFFC89B3C)
    val GoldShadow = Color(0xFF785A28)

    // Teals -- hextech, used for interactive affordances.
    val HextechBright = Color(0xFF0AC8B9)
    val Hextech = Color(0xFF0397AB)
    val HextechDeep = Color(0xFF005A82)

    // Neutrals for the light theme.
    val Parchment = Color(0xFFF7F4EC)
    val ParchmentDim = Color(0xFFEBE5D6)
    val Ink = Color(0xFF1B1B1B)
    val InkMuted = Color(0xFF55524B)

    val White = Color(0xFFFFFFFF)

    // Status.
    val Ruby = Color(0xFFBE1E37)
    val Amber = Color(0xFFE0A030)
    val Jade = Color(0xFF1E9E5A)

    // Champion attribute bars on the detail screen.
    val AttackRed = Color(0xFFC0392B)
    val DefenseGreen = Color(0xFF27AE60)
    val MagicBlue = Color(0xFF2980B9)
    val DifficultyPurple = Color(0xFF8E44AD)
}

/**
 * Semantic colour roles. Composables use these names, never [Palette].
 */
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val primary: Color,
    val onPrimary: Color,
    val accent: Color,
    val onAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val error: Color,
    val warning: Color,
    val success: Color,
    val attack: Color,
    val defense: Color,
    val magic: Color,
    val difficulty: Color,
    val isLight: Boolean,
)

internal val DarkAppColors = AppColors(
    background = Palette.Abyss,
    surface = Palette.MidnightNavy,
    surfaceElevated = Palette.SlateNavy,
    border = Palette.GoldShadow,
    primary = Palette.GoldDeep,
    onPrimary = Palette.Abyss,
    accent = Palette.HextechBright,
    onAccent = Palette.Abyss,
    textPrimary = Palette.GoldBright,
    textSecondary = Palette.Gold,
    textDisabled = Palette.Fog,
    error = Palette.Ruby,
    warning = Palette.Amber,
    success = Palette.Jade,
    attack = Palette.AttackRed,
    defense = Palette.DefenseGreen,
    magic = Palette.MagicBlue,
    difficulty = Palette.DifficultyPurple,
    isLight = false,
)

internal val LightAppColors = AppColors(
    background = Palette.Parchment,
    surface = Palette.White,
    surfaceElevated = Palette.ParchmentDim,
    border = Palette.Gold,
    primary = Palette.GoldShadow,
    onPrimary = Palette.White,
    accent = Palette.HextechDeep,
    onAccent = Palette.White,
    textPrimary = Palette.Ink,
    textSecondary = Palette.InkMuted,
    textDisabled = Palette.Fog,
    error = Palette.Ruby,
    warning = Palette.Amber,
    success = Palette.Jade,
    attack = Palette.AttackRed,
    defense = Palette.DefenseGreen,
    magic = Palette.MagicBlue,
    difficulty = Palette.DifficultyPurple,
    isLight = true,
)
