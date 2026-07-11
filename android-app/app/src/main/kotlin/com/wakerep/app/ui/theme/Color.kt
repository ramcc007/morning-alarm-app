package com.wakerep.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Wakerep Design System v1.0 color tokens. A dark-native palette - warm-tinted
 * near-blacks, never true gray - with the sunrise gradient rationed as the
 * single saturated accent. See design-system reference doc, section 02.
 *
 * No green anywhere: "verified/complete" is signalled by the horizon arc
 * filling to amber-gold plus the frame brightening, a shape-and-brightness
 * signal rather than a hue swap (also makes it colorblind-safe).
 */
object WakerepColors {
    /** ink/900 - canvas, the base background almost every screen sits on. */
    val InkCanvas = Color(0xFF08080B)

    /** ink/800 - sunk, for recessed wells (camera frame background, input sinks). */
    val InkSunk = Color(0xFF0E0F14)

    /** surface - card, the default elevated container. */
    val Surface = Color(0xFF16171F)

    /** surface/hi - raised, a step brighter than Surface for nested/hover-active cards. */
    val SurfaceHigh = Color(0xFF1E2029)

    /** coral - primary. The main accent for CTAs, focus, and the warm end of the gradient. */
    val Coral = Color(0xFFFF6B4F)

    /** amber - verified. "Done/earned" state color; also the hot end of the gradient. */
    val Amber = Color(0xFFFFB037)

    /** pink - streak/dusk. Cool end of the gradient, used for streak accents. */
    val Pink = Color(0xFFFF5C8A)

    /** alert - destructive only. Never used for anything else. */
    val Alert = Color(0xFFFF5C5C)

    /** text/hi - warm white, near-black-on-white contrast inverted for dark surfaces. */
    val TextHigh = Color(0xFFF7F4EE)

    /** text/mid - ~60% opacity warm white, for secondary copy. */
    val TextMid = TextHigh.copy(alpha = 0.60f)

    /** text/low - ~38% opacity warm white, for tertiary/disabled copy. */
    val TextLow = TextHigh.copy(alpha = 0.38f)

    /** hairline - subtle 8% white borders/dividers; on black this reads as depth, not a shadow. */
    val Hairline = Color(0xFFFFFFFF).copy(alpha = 0.08f)

    /** Focus ring color - amber, full opacity. */
    val Focus = Amber

    /**
     * gradient/dawn - the signature. Linear 105°, pink -> coral -> amber.
     * The "earn light" fill: used on the arc, hero glow, and primary CTA.
     * Never a flat fill behind text.
     */
    val DawnGradient = Brush.linearGradient(
        colors = listOf(Pink, Coral, Amber),
        start = Offset(0f, 0f),
        end = Offset(1000f, 260f), // approximates a 105° angle; recompute per-size if precision matters
    )

    /** Soft radial glow used to pool warmth at a card's base or behind a hero element. */
    fun dawnGlow(radius: Float = 600f) = Brush.radialGradient(
        colors = listOf(Coral.copy(alpha = 0.22f), Color.Transparent),
        radius = radius,
    )
}
