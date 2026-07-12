package com.wakerep.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wakerep.app.ui.theme.WakerepColors

/**
 * Wakerep's signature shape (design-system reference doc, section 07: "the
 * arc is our checkmark"): a horizon-line gauge that fills with the dawn
 * gradient as [progress] rises, rather than a generic ring or checkmark.
 * Used tiny on alarm cards, medium in stats, huge (as the ringing screen's
 * own bespoke sun/horizon) on the wake flow.
 *
 * Status reads by brightness/fill, not hue - colorblind-safe by
 * construction (no green-for-done anywhere in the app).
 */
@Composable
fun HorizonArc(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 28.dp,
    strokeWidth: Dp = 3.dp,
    lit: Boolean = true,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val startAngle = 125f
    val sweepAngle = 290f

    Canvas(modifier = modifier.size(diameter)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        val inset = strokeWidth.toPx() / 2f
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = WakerepColors.Hairline,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )

        if (clamped > 0f && lit) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(WakerepColors.Pink, WakerepColors.Coral, WakerepColors.Amber),
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle * clamped,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
    }
}
