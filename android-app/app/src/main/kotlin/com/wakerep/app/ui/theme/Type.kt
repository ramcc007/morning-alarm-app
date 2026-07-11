package com.wakerep.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Wakerep's "three voices" type system (design-system reference doc, section 03):
 * a shout (display), a speaker (body), and a readout (mono).
 *
 * NOTE ON FONTS: the reference spec calls for Bricolage Grotesque (display),
 * Hanken Grotesk (body), and Space Mono (readout) from Google Fonts. This
 * build uses the closest built-in system font family as a stand-in for each
 * role - [FontFamily.Default] at heavy weight for the shout,
 * [FontFamily.SansSerif] for the speaker, [FontFamily.Monospace] for the
 * readout - so the app compiles and renders correctly with zero network
 * dependency. To bring in the real typefaces: download the three family
 * .ttf/.otf files, place them under `res/font/`, and swap the FontFamily
 * values below for `FontFamily(Font(R.font.bricolage_grotesque_bold), ...)`
 * (or wire up the Google Fonts downloadable-font provider, which needs a
 * verified certificate array this environment couldn't safely generate
 * offline - see PR notes).
 */
object WakerepFonts {
    val Display = FontFamily.Default
    val Body = FontFamily.SansSerif
    val Mono = FontFamily.Monospace
}

object WakerepType {
    /** display-xl · 88/700 · time, rep counts, headlines. Tight tracking at large sizes. */
    val DisplayXl = TextStyle(
        fontFamily = WakerepFonts.Display,
        fontWeight = FontWeight.Bold,
        fontSize = 88.sp,
        lineHeight = 92.sp,
        letterSpacing = (-0.03).em,
    )

    /** h1 · 34/700 */
    val H1 = TextStyle(
        fontFamily = WakerepFonts.Display,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.02).em,
    )

    /** h2 · 26/600 */
    val H2 = TextStyle(
        fontFamily = WakerepFonts.Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.01).em,
    )

    /** body-lg · 17/400 */
    val BodyLg = TextStyle(
        fontFamily = WakerepFonts.Body,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    )

    /** body · 15/400 - all UI copy, labels, list rows. */
    val Body = TextStyle(
        fontFamily = WakerepFonts.Body,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    )

    /** body medium/semibold weights for emphasis without switching family. */
    val BodyEmphasis = Body.copy(fontWeight = FontWeight.SemiBold)

    /** label · 13/600 · uppercase, tracked. Section kickers, chip text. */
    val Label = TextStyle(
        fontFamily = WakerepFonts.Body,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em,
    )

    /** mono · 11/700 · uppercase, tracked. Instrument labels only: "REC", timestamps, tallies. */
    val Mono = TextStyle(
        fontFamily = WakerepFonts.Mono,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.14.em,
    )

    /** The 104px hero numeral used only on the ringing screen's rep counter. */
    val RepNumeral: TextUnit = 104.sp
}
