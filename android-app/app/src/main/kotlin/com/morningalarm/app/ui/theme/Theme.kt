package com.morningalarm.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Single committed dark "energetic sunrise" theme, matching the marketing site.
val Background = Color(0xFF0A0B10)
val Surface = Color(0xFF171923)
val SurfaceElevated = Color(0xFF1E212C)
val AccentOrange = Color(0xFFFF8A42)
val AccentPink = Color(0xFFFF5C8A)
val AccentYellow = Color(0xFFFFB037)
val SuccessGreen = Color(0xFF4FD987)
val TextPrimary = Color(0xFFF5F6F8)
val TextSecondary = Color(0xB3F5F6F8)

private val MorningAlarmColorScheme = darkColorScheme(
    primary = AccentOrange,
    secondary = AccentPink,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceElevated,
    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Color(0xFFFF6B6B),
)

private val MorningAlarmShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
)

private val MorningAlarmTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
)

@Composable
fun MorningAlarmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MorningAlarmColorScheme,
        shapes = MorningAlarmShapes,
        typography = MorningAlarmTypography,
        content = content
    )
}
