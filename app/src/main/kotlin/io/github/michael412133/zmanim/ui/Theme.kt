package io.github.michael412133.zmanim.ui

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Black on white, the same values as MMD's own e-ink scheme.
 *
 * It starts from lightColorScheme() rather than MMD's scheme because MMD leaves a few of the
 * newer surface colors unset, and an unset color draws as fully transparent. Starting here
 * fills every role, and each one is then set to black or white.
 */
val Monochrome = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color.Black,
    onPrimaryContainer = Color.White,
    inversePrimary = Color.White,
    secondary = Color.White,
    onSecondary = Color.Black,
    secondaryContainer = Color.Black,
    onSecondaryContainer = Color.White,
    tertiary = Color.White,
    onTertiary = Color.Black,
    tertiaryContainer = Color.Black,
    onTertiaryContainer = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color.White,
    onSurfaceVariant = Color.Black,
    surfaceTint = Color.White,
    inverseSurface = Color.White,
    inverseOnSurface = Color.Black,
    error = Color.Black,
    onError = Color.White,
    errorContainer = Color.White,
    onErrorContainer = Color.Black,
    outline = Color.Black,
    outlineVariant = Color.Black,
    scrim = Color.Black,
    surfaceBright = Color.White,
    surfaceDim = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerLowest = Color.White,
)
