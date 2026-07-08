package com.wakerep.app.alarm

import android.Manifest
import android.app.KeyguardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wakerep.app.WakerepApplication
import com.wakerep.app.model.Alarm
import com.wakerep.app.ui.AlarmRingingViewModel
import com.wakerep.app.ui.screens.AlarmRingingScreen
import com.wakerep.app.ui.theme.Background
import com.wakerep.app.ui.theme.WakerepTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Launched via the ringing service's full-screen-intent notification, so it
 * needs to be able to draw over the lock screen and turn the screen on
 * itself - a regular activity relies on the user having already unlocked
 * their phone, which defeats the point of an alarm.
 *
 * Deliberately overrides the back gesture/button: an alarm you can dismiss
 * with one swipe isn't an alarm.
 */
class AlarmRingingActivity : ComponentActivity() {

    private var alarmState by mutableStateOf<Alarm?>(null)
    private var cameraPermissionGranted by mutableStateOf(false)

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraPermissionGranted = granted }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        (getSystemService(KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Swallow back presses - the alarm is dismissed only by finishing the reps or snoozing.
            }
        })

        cameraPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!cameraPermissionGranted) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID)
        val app = application as WakerepApplication
        lifecycleScope.launch {
            alarmState = app.alarmRepository.alarms.first().firstOrNull { it.id == alarmId }
        }

        setContent {
            WakerepTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    val alarm = alarmState
                    when {
                        alarm == null -> Unit // brief loading gap while the alarm loads from disk
                        !cameraPermissionGranted -> CameraPermissionRequiredContent(
                            onOpenSettings = ::openAppSettings,
                        )
                        else -> {
                            val viewModel: AlarmRingingViewModel = viewModel(
                                factory = AlarmRingingViewModel.Factory(alarm, applicationContext)
                            )
                            AlarmRingingScreen(
                                alarm = alarm,
                                viewModel = viewModel,
                                onFinished = ::finishRinging,
                                onSnooze = ::snooze,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun finishRinging() {
        startService(AlarmRingingService.stopIntent(this))
        finish()
    }

    private fun snooze() {
        alarmState?.let { alarm ->
            val triggerAtMillis = System.currentTimeMillis() + alarm.snoozeMinutes * 60_000L
            AlarmScheduler(applicationContext).scheduleSnooze(alarm, triggerAtMillis)
        }
        finishRinging()
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }
}

@Composable
private fun CameraPermissionRequiredContent(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Camera access is required to validate your exercise and stop the alarm.",
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onOpenSettings, modifier = Modifier.padding(top = 16.dp)) {
            Text("Open Settings")
        }
    }
}
