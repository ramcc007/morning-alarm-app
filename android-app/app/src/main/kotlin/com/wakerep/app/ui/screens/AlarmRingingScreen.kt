package com.wakerep.app.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerep.app.model.Alarm
import com.wakerep.app.ui.AlarmRingingViewModel
import com.wakerep.app.ui.icons.WakerepIcons
import com.wakerep.app.ui.theme.WakerepColors
import com.wakerep.app.ui.theme.WakerepMotion
import com.wakerep.app.ui.theme.WakerepRadius
import com.wakerep.app.ui.theme.WakerepSpacing
import com.wakerep.app.ui.theme.WakerepType
import com.wakerep.app.ui.theme.rememberReduceMotion
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * The ringing screen's hero interaction (design-system reference doc,
 * section 07): a horizon that lights up in real time as the camera sees the
 * user move through a rep. [AlarmRingingViewModel.displayedFraction] already
 * blends banked reps with the live in-progress extension of the current one,
 * so the sun's height is spatially bound to the body, not just a per-rep tick.
 */
@Composable
fun AlarmRingingScreen(
    alarm: Alarm,
    viewModel: AlarmRingingViewModel,
    onFinished: () -> Unit,
    onSnooze: () -> Unit,
) {
    val repCount by viewModel.repCount.collectAsStateWithLifecycle()
    val displayedFraction by viewModel.displayedFraction.collectAsStateWithLifecycle()
    val isComplete by viewModel.isComplete.collectAsStateWithLifecycle()
    val guidance by viewModel.guidance.collectAsStateWithLifecycle()
    val isBodyDetected by viewModel.isBodyDetected.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val reduceMotion = rememberReduceMotion()

    val animatedFraction by animateFloatAsState(
        targetValue = displayedFraction,
        animationSpec = tween(
            durationMillis = if (reduceMotion) 0 else WakerepMotion.RepPulseMs,
            easing = WakerepMotion.EaseDawn,
        ),
        label = "horizon-fraction",
    )

    LaunchedEffect(Unit) {
        viewModel.repTick.collect {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(WakerepMotion.DaybreakMs.toLong() + 400)
            onFinished()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(WakerepColors.InkCanvas)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).also { previewView ->
                    viewModel.cameraController.bindToLifecycle(lifecycleOwner, previewView)
                }
            }
        )

        // Dim the raw feed so the sky/horizon overlay and text stay legible on top of it.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f))
        )

        HorizonSky(
            fraction = if (isBodyDetected) animatedFraction else 0f,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 56.dp, start = WakerepSpacing.xl, end = WakerepSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(alarm.label, style = WakerepType.H2, color = WakerepColors.TextHigh)
            Text(
                alarm.exercise.displayName,
                style = WakerepType.Label,
                color = WakerepColors.TextMid,
                modifier = Modifier.padding(top = WakerepSpacing.xs),
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "$repCount",
                color = WakerepColors.TextHigh,
                style = WakerepType.DisplayXl.copy(fontSize = WakerepType.RepNumeral),
            )
            Text(
                text = "of ${viewModel.targetReps} ${alarm.exercise.displayName.lowercase()}",
                style = WakerepType.BodyLg,
                color = WakerepColors.TextMid,
                modifier = Modifier.padding(top = WakerepSpacing.xs),
            )

            AnimatedVisibility(visible = !isBodyDetected, enter = fadeIn(), exit = fadeOut()) {
                GuidancePill(
                    text = "Step into frame so we can see your full body",
                    modifier = Modifier.padding(top = WakerepSpacing.lg),
                )
            }
            AnimatedVisibility(visible = isBodyDetected && guidance != null, enter = fadeIn(), exit = fadeOut()) {
                GuidancePill(
                    text = guidance ?: "",
                    modifier = Modifier.padding(top = WakerepSpacing.lg),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))
                )
                .padding(bottom = WakerepSpacing.xxl, top = WakerepSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (alarm.snoozeEnabled) {
                Text(
                    text = "Snooze ${alarm.snoozeMinutes} min",
                    style = WakerepType.BodyEmphasis,
                    color = WakerepColors.TextHigh.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(WakerepRadius.pill))
                        .background(WakerepColors.TextHigh.copy(alpha = 0.10f))
                        .clickable(onClick = onSnooze)
                        .padding(horizontal = WakerepSpacing.xl, vertical = WakerepSpacing.md),
                )
            }
            Text(
                "The alarm keeps ringing until you finish your reps.",
                style = WakerepType.Mono,
                color = WakerepColors.TextLow,
                modifier = Modifier.padding(top = WakerepSpacing.lg),
            )
        }

        AnimatedVisibility(visible = isComplete, enter = fadeIn(), exit = fadeOut()) {
            DaybreakOverlay()
        }
    }
}

@Composable
private fun GuidancePill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = WakerepType.Label,
        color = WakerepColors.Amber,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(WakerepRadius.pill))
            .background(WakerepColors.Amber.copy(alpha = 0.14f))
            .padding(horizontal = WakerepSpacing.lg, vertical = WakerepSpacing.sm),
    )
}

@Composable
private fun DaybreakOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WakerepColors.DawnGradient),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WakerepIcons.Achievement(size = 56.dp, tint = Color.Black.copy(alpha = 0.85f), filled = true)
            Text(
                "Nice work!",
                style = WakerepType.H1,
                color = Color.Black.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = WakerepSpacing.lg),
            )
            Text(
                "Alarm dismissed.",
                style = WakerepType.BodyLg,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = WakerepSpacing.xs),
            )
        }
    }
}

/**
 * The body-tracked horizon: a starfield that fades as light is earned, a
 * glowing horizon line, and a sun disc that rises from below the horizon to
 * near the top of frame as [fraction] goes 0f -> 1f. Stroke/positions are
 * fixed per-composition (not per-frame) so this stays cheap to redraw as
 * [fraction] animates every frame while the camera is analyzing.
 */
@Composable
private fun HorizonSky(fraction: Float, modifier: Modifier = Modifier) {
    val stars = remember {
        val random = Random(20260711)
        List(48) {
            Offset(random.nextFloat(), random.nextFloat() * 0.62f)
        }
    }

    Canvas(modifier = modifier) {
        val horizonY = size.height * 0.74f
        val starAlpha = (1f - fraction * 1.3f).coerceIn(0f, 1f)

        if (starAlpha > 0f) {
            stars.forEach { relative ->
                drawCircle(
                    color = WakerepColors.TextHigh.copy(alpha = starAlpha * 0.6f),
                    radius = 1.6f,
                    center = Offset(relative.x * size.width, relative.y * size.height),
                )
            }
        }

        val sunColor = when {
            fraction <= 0.5f -> lerp(WakerepColors.Pink, WakerepColors.Coral, fraction * 2f)
            else -> lerp(WakerepColors.Coral, WakerepColors.Amber, (fraction - 0.5f) * 2f)
        }

        // Horizon glow strip - brightens and widens as light is earned.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(sunColor.copy(alpha = 0.22f * fraction), Color.Transparent),
                startY = horizonY - 90f,
                endY = horizonY + 40f,
            ),
            topLeft = Offset(0f, horizonY - 90f),
            size = androidx.compose.ui.geometry.Size(size.width, 130f),
        )
        drawLine(
            color = sunColor.copy(alpha = 0.35f + 0.5f * fraction),
            start = Offset(0f, horizonY),
            end = Offset(size.width, horizonY),
            strokeWidth = 1.5f,
        )

        val sunRadius = androidx.compose.ui.util.lerp(70f, 130f, fraction)
        val sunCenterY = androidx.compose.ui.util.lerp(horizonY + sunRadius * 0.6f, size.height * 0.16f, fraction)
        val sunCenter = Offset(size.width / 2f, sunCenterY)

        // Sun only ever peeks above the horizon line, never draws below it - reads as a true sunrise.
        clipRect(left = 0f, top = 0f, right = size.width, bottom = horizonY) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(sunColor.copy(alpha = 0.35f * fraction), Color.Transparent),
                    center = sunCenter,
                    radius = sunRadius * 2.4f,
                ),
                radius = sunRadius * 2.4f,
                center = sunCenter,
            )
            drawCircle(color = sunColor, radius = sunRadius, center = sunCenter)
        }
    }
}
