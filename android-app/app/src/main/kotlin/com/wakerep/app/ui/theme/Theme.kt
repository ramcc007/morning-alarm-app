package com.wakerep.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val WakerepColorScheme = darkColorScheme(
    primary = WakerepColors.Coral,
    secondary = WakerepColors.Pink,
    tertiary = WakerepColors.Amber,
    background = WakerepColors.InkCanvas,
    surface = WakerepColors.Surface,
    surfaceVariant = WakerepColors.SurfaceHigh,
    onPrimary = Color.Black,
    onBackground = WakerepColors.TextHigh,
    onSurface = WakerepColors.TextHigh,
    error = WakerepColors.Alert,
)

private val WakerepShapes = Shapes(
    small = RoundedCornerShape(WakerepRadius.control),
    medium = RoundedCornerShape(WakerepRadius.field),
    large = RoundedCornerShape(WakerepRadius.card),
    extraLarge = RoundedCornerShape(WakerepRadius.sheet),
)

private val WakerepTypography = Typography(
    displayLarge = WakerepType.DisplayXl,
    headlineLarge = WakerepType.H1,
    headlineMedium = WakerepType.H2,
    titleMedium = WakerepType.H2,
    bodyLarge = WakerepType.BodyLg,
    bodyMedium = WakerepType.Body,
    labelLarge = WakerepType.Label,
    labelSmall = WakerepType.Mono,
)

/**
 * Root theme for the whole app - a single committed dark-native design
 * (design-system reference doc v1.0). No light-mode variant is planned:
 * "half-asleep in a dark room" is the primary use case at every hour.
 */
@Composable
fun WakerepTheme(content: @Composable () -> Unit) {
    val reduceMotion = rememberReduceMotion()
    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
        MaterialTheme(
            colorScheme = WakerepColorScheme,
            shapes = WakerepShapes,
            typography = WakerepTypography,
            content = content,
        )
    }
}
