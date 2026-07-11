package com.wakerep.app.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Wakerep's bespoke icon set (design-system reference doc, section 06):
 * geometric, 2px stroke, round caps, drawn on a 24px grid. The horizon-arc
 * motif recurs across the family so it reads as one voice rather than a
 * grab-bag of glyphs. Duotone (filled + stroke) only where an icon needs to
 * show a lit-vs-unlit state (e.g. achievements).
 *
 * Each icon is drawn directly via [Canvas]/[DrawScope] rather than shipped
 * as static vector assets, so the whole family lives in one reviewable file
 * and every stroke width/color stays driven by the design tokens.
 */
object WakerepIcons {

    private const val GRID = 24f

    @Composable
    fun Alarm(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color) {
        IconCanvas(modifier, size) {
            val s = scaleFactor()
            // Horizon arc "dawn" cap
            drawArc(
                color = tint,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(2f * s, 8f * s),
                size = androidx.compose.ui.geometry.Size(20f * s, 20f * s),
                style = Stroke(width = 2f * s, cap = StrokeCap.Round),
            )
            // Sunrise tick above the arc
            drawLine(
                color = tint,
                start = Offset(12f * s, 3f * s),
                end = Offset(12f * s, 1.5f * s),
                strokeWidth = 2f * s,
                cap = StrokeCap.Round,
            )
        }
    }

    @Composable
    fun Camera(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color) {
        IconCanvas(modifier, size) {
            val s = scaleFactor()
            drawRoundRect(
                color = tint,
                topLeft = Offset(2.5f * s, 6f * s),
                size = androidx.compose.ui.geometry.Size(19f * s, 14f * s),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * s, 4f * s),
                style = Stroke(width = 2f * s, cap = StrokeCap.Round),
            )
            drawCircle(
                color = tint,
                radius = 4f * s,
                center = Offset(12f * s, 13f * s),
                style = Stroke(width = 2f * s),
            )
        }
    }

    @Composable
    fun Exercise(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color) {
        IconCanvas(modifier, size) {
            val s = scaleFactor()
            drawCircle(color = tint, radius = 2.4f * s, center = Offset(12f * s, 5.5f * s), style = Stroke(2f * s))
            drawLine(tint, Offset(12f * s, 9f * s), Offset(12f * s, 16f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(12f * s, 11f * s), Offset(6f * s, 14f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(12f * s, 11f * s), Offset(18f * s, 14f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(12f * s, 16f * s), Offset(8f * s, 21.5f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(12f * s, 16f * s), Offset(16f * s, 21.5f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
        }
    }

    @Composable
    fun Streak(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color) {
        IconCanvas(modifier, size) {
            val s = scaleFactor()
            drawLine(tint, Offset(12f * s, 20f * s), Offset(12f * s, 4.5f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(12f * s, 4f * s), Offset(7f * s, 9f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(12f * s, 4f * s), Offset(17f * s, 9f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
        }
    }

    @Composable
    fun Add(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color) {
        IconCanvas(modifier, size) {
            val s = scaleFactor()
            drawLine(tint, Offset(12f * s, 5f * s), Offset(12f * s, 19f * s), strokeWidth = 2.4f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(5f * s, 12f * s), Offset(19f * s, 12f * s), strokeWidth = 2.4f * s, cap = StrokeCap.Round)
        }
    }

    @Composable
    fun Snooze(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color) {
        IconCanvas(modifier, size) {
            val s = scaleFactor()
            drawCircle(tint, radius = 8f * s, center = Offset(12f * s, 13f * s), style = Stroke(2f * s))
            drawLine(tint, Offset(12f * s, 13f * s), Offset(12f * s, 8.5f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(12f * s, 13f * s), Offset(15f * s, 14.5f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
            drawLine(tint, Offset(9f * s, 3.5f * s), Offset(15f * s, 3.5f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
        }
    }

    @Composable
    fun Settings(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color) {
        IconCanvas(modifier, size) {
            val s = scaleFactor()
            drawCircle(tint, radius = 4.5f * s, center = Offset(12f * s, 12f * s), style = Stroke(2f * s))
            for (i in 0 until 8) {
                val angle = (i * 45.0) * Math.PI / 180.0
                val innerR = 7.5f * s
                val outerR = 10f * s
                val start = Offset(
                    12f * s + (innerR * cos(angle)).toFloat(),
                    12f * s + (innerR * sin(angle)).toFloat(),
                )
                val end = Offset(
                    12f * s + (outerR * cos(angle)).toFloat(),
                    12f * s + (outerR * sin(angle)).toFloat(),
                )
                drawLine(tint, start, end, strokeWidth = 2f * s, cap = StrokeCap.Round)
            }
        }
    }

    /** Achievement medallion glyph: outlined star, duotone-fillable by the caller for lit/unlit states. */
    @Composable
    fun Achievement(modifier: Modifier = Modifier, size: Dp = 24.dp, tint: Color, filled: Boolean = false) {
        IconCanvas(modifier, size) {
            val s = scaleFactor()
            val path = starPath(center = Offset(12f * s, 12f * s), outerRadius = 9f * s, innerRadius = 3.8f * s)
            if (filled) {
                drawPath(path, color = tint)
            } else {
                drawPath(path, color = tint, style = Stroke(width = 1.6f * s, cap = StrokeCap.Round))
            }
        }
    }

    private fun starPath(center: Offset, outerRadius: Float, innerRadius: Float): androidx.compose.ui.graphics.Path {
        val path = androidx.compose.ui.graphics.Path()
        val points = 5
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = (Math.PI / points * i) - Math.PI / 2
            val x = center.x + (radius * cos(angle)).toFloat()
            val y = center.y + (radius * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    @Composable
    private fun IconCanvas(modifier: Modifier, size: Dp, draw: DrawScope.() -> Unit) {
        Canvas(modifier = modifier.size(size)) {
            draw()
        }
    }

    /** Scale factor so paths authored against a 24-unit grid fit whatever [Dp] size is requested. */
    private fun DrawScope.scaleFactor(): Float = size.minDimension / GRID
}
