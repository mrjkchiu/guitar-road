package com.chiu.guitarroad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Wood,
    onPrimary = CardWhite,
    primaryContainer = WoodDark,
    secondary = LeafGreen,
    onSecondary = CardWhite,
    secondaryContainer = LeafGreenLight,
    error = BrickRed,
    onError = CardWhite,
    errorContainer = BrickRedLight,
    background = CreamBg,
    onBackground = InkPrimary,
    surface = CardWhite,
    onSurface = InkPrimary,
    surfaceVariant = CardVariant,
    onSurfaceVariant = InkSecondary,
    outline = LineDivider,
    outlineVariant = LineDivider,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = InkPrimary,
    primaryContainer = WoodDark,
    secondary = LeafGreen,
    onSecondary = CardWhite,
    secondaryContainer = LeafGreenLight,
    error = BrickRed,
    onError = CardWhite,
    errorContainer = BrickRedLight,
    background = DarkBg,
    onBackground = CreamBg,
    surface = DarkSurface,
    onSurface = CreamBg,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
)

@Composable
fun GuitarRoadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = GuitarRoadTypography,
        content = content,
    )
}
