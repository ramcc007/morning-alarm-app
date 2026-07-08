package com.wakerep.app.ui.screens

import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerep.app.model.Alarm
import com.wakerep.app.ui.AlarmRingingViewModel
import com.wakerep.app.ui.theme.AccentYellow
import com.wakerep.app.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun AlarmRingingScreen(
    alarm: Alarm,
    viewModel: AlarmRingingViewModel,
    onFinished: () -> Unit,
    onSnooze: () -> Unit,
) {
    val repCount by viewModel.repCount.collectAsStateWithLifecycle()
    val isComplete by viewModel.isComplete.collectAsStateWithLifecycle()
    val guidance by viewModel.guidance.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isComplete) {
        if (isComplete) {
            delay(1600)
            onFinished()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).also { previewView ->
                    viewModel.cameraController.bindToLifecycle(lifecycleOwner, previewView)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                ))
                .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(alarm.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(
                "$repCount / ${viewModel.targetReps} ${alarm.exercise.displayName.lowercase()}",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
            )
            LinearProgressIndicator(
                progress = { (repCount.toFloat() / viewModel.targetReps.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(200.dp),
                color = SuccessGreen,
                trackColor = Color.White.copy(alpha = 0.15f),
            )
            guidance?.let {
                Text(
                    it,
                    color = AccentYellow,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                ))
                .padding(bottom = 40.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (alarm.snoozeEnabled) {
                Button(
                    onClick = onSnooze,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                ) {
                    Text("Snooze ${alarm.snoozeMinutes} min", color = Color.White)
                }
            }
            Text(
                "The alarm keeps ringing until you finish your reps.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        AnimatedVisibility(visible = isComplete) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    Text("Nice work! Alarm dismissed.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}
