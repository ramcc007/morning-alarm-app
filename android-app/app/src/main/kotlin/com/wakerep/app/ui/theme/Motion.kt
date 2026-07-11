package com.wakerep.app.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Wakerep motion system (design-system reference doc, section 05): one
 * easing curve owns the whole app, content rises into place rather than
 * dropping in, and every duration is named rather than ad-hoc.
 */
object WakerepMotion {
    /** ease-dawn · cubic-bezier(0.2, 0.8, 0.2, 1) - slow to start, confident to finish. */
    val EaseDawn: Easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

    const val EnterRiseMs = 420
    const val TapFeedbackMs = 120
    const val RepPulseMs = 300
    const val DaybreakMs = 1200
}

/**
 * Whether the user has requested reduced motion (Android's system-wide
 * "Remove animations" accessibility setting / animator duration scale = 0).
 * Per the reference spec: rises become cross-fades and the rep-pulse is
 * disabled, but the arc still fills so meaning is never motion-only.
 */
val LocalReduceMotion = staticCompositionLocalOf { false }

fun isReduceMotionEnabled(context: Context): Boolean {
    val scale = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    return scale == 0f
}

@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return isReduceMotionEnabled(context)
}
