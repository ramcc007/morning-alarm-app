package com.wakerep.app.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.wakerep.app.ui.icons.WakerepIcons
import com.wakerep.app.ui.theme.WakerepColors
import com.wakerep.app.ui.theme.WakerepMotion
import com.wakerep.app.ui.theme.WakerepRadius
import com.wakerep.app.ui.theme.WakerepSpacing
import com.wakerep.app.ui.theme.WakerepType
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val subtitle: String)

private val pages = listOf(
    OnboardingPage(
        "The alarm you can't sleep through",
        "No snooze spiral. No shutting it off half-asleep. It rings until you move.",
    ),
    OnboardingPage(
        "Prove it with push-ups",
        "Point your front camera at yourself and knock out your set. We count every rep automatically.",
    ),
    OnboardingPage(
        "Start your day already winning",
        "By the time the alarm stops, you've already done your first workout of the day.",
    ),
)

/**
 * Onboarding (design-system reference doc, section 07 "Sell the fight, then
 * earn the permissions"): three swipes of attitude with a horizon glow that
 * rises a little brighter each page - onboarding is itself a tiny sunrise -
 * then a dedicated permissions screen honest about the camera being
 * on-device only, because that's the real objection.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var showPermissions by remember { mutableStateOf(false) }

    if (showPermissions) {
        PermissionsScreen(onFinished = onFinished)
    } else {
        PitchPager(onDone = { showPermissions = true })
    }
}

@Composable
private fun PitchPager(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    Box(modifier = Modifier.fillMaxSize().background(WakerepColors.InkCanvas)) {
        HorizonGlow(pageFraction = (pagerState.currentPage + 1f) / pages.size)

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier.fillMaxSize().padding(WakerepSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(WakerepColors.DawnGradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        WakerepIcons.Alarm(size = 30.dp, tint = Color.Black)
                    }
                    Text(
                        page.title,
                        style = WakerepType.H1,
                        color = WakerepColors.TextHigh,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = WakerepSpacing.xl),
                    )
                    Text(
                        page.subtitle,
                        style = WakerepType.BodyLg,
                        color = WakerepColors.TextMid,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = WakerepSpacing.md),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = WakerepSpacing.xl),
                horizontalArrangement = Arrangement.Center,
            ) {
                pages.indices.forEach { index ->
                    val active = index == pagerState.currentPage
                    val dotColor by animateColorAsState(
                        targetValue = if (active) WakerepColors.Amber else WakerepColors.Hairline,
                        animationSpec = tween(WakerepMotion.TapFeedbackMs),
                        label = "dot",
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (active) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                }
            }

            Button(
                onClick = {
                    if (isLastPage) onDone() else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                shape = RoundedCornerShape(WakerepRadius.pill),
                modifier = Modifier.fillMaxWidth().padding(horizontal = WakerepSpacing.xl).padding(bottom = WakerepSpacing.xxl),
                colors = ButtonDefaults.buttonColors(containerColor = WakerepColors.Coral),
            ) {
                Text(if (isLastPage) "Get started" else "Continue", style = WakerepType.BodyEmphasis, color = Color.Black)
            }
        }
    }
}

@Composable
private fun HorizonGlow(pageFraction: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val horizonY = size.height * 0.62f
        val glowColor = lerp(WakerepColors.Pink, WakerepColors.Amber, pageFraction)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, glowColor.copy(alpha = 0.10f + 0.18f * pageFraction)),
                startY = horizonY - 200f,
                endY = size.height,
            ),
            topLeft = Offset(0f, horizonY - 200f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height - horizonY + 200f),
        )
    }
}

private fun canScheduleExactAlarms(context: android.content.Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return true
    return alarmManager.canScheduleExactAlarms()
}

@Composable
private fun PermissionsScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var cameraGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var alarmsGranted by remember { mutableStateOf(canScheduleExactAlarms(context)) }
    val requestCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraGranted = granted
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                alarmsGranted = canScheduleExactAlarms(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(WakerepColors.InkCanvas).padding(WakerepSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(WakerepSpacing.xxl))
        Text("Two things, then we're set.", style = WakerepType.H1, color = WakerepColors.TextHigh, textAlign = TextAlign.Center)
        Text(
            "Pose detection runs on-device. No video is recorded or ever leaves your phone.",
            style = WakerepType.Body,
            color = WakerepColors.TextMid,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = WakerepSpacing.sm),
        )

        PermissionRow(
            icon = { WakerepIcons.Camera(size = 22.dp, tint = WakerepColors.Amber) },
            title = "Camera",
            subtitle = "To count your reps. On-device only.",
            granted = cameraGranted,
            onGrant = { requestCamera.launch(Manifest.permission.CAMERA) },
            modifier = Modifier.padding(top = WakerepSpacing.xxl),
        )
        PermissionRow(
            icon = { WakerepIcons.Alarm(size = 22.dp, tint = WakerepColors.Amber) },
            title = "Alarms & over-lock display",
            subtitle = "To fire on time and show over the lock screen.",
            granted = alarmsGranted,
            onGrant = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                    )
                }
            },
            modifier = Modifier.padding(top = WakerepSpacing.md),
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onFinished,
            shape = RoundedCornerShape(WakerepRadius.pill),
            modifier = Modifier.fillMaxWidth().padding(bottom = WakerepSpacing.xl),
            colors = ButtonDefaults.buttonColors(containerColor = WakerepColors.Coral),
        ) {
            Text("Allow & continue", style = WakerepType.BodyEmphasis, color = Color.Black)
        }
    }
}

@Composable
private fun PermissionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    granted: Boolean,
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WakerepRadius.card))
            .background(WakerepColors.Surface)
            .padding(WakerepSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(WakerepColors.SurfaceHigh),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Column(modifier = Modifier.weight(1f).padding(horizontal = WakerepSpacing.md)) {
            Text(title, style = WakerepType.BodyEmphasis, color = WakerepColors.TextHigh)
            Text(subtitle, style = WakerepType.Label, color = WakerepColors.TextMid, modifier = Modifier.padding(top = 2.dp))
        }
        if (granted) {
            Text("GRANTED", style = WakerepType.Mono, color = WakerepColors.Amber)
        } else {
            Text(
                "GRANT",
                style = WakerepType.Mono,
                color = Color.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(WakerepRadius.pill))
                    .background(WakerepColors.Amber)
                    .clickable(onClick = onGrant)
                    .padding(horizontal = WakerepSpacing.md, vertical = WakerepSpacing.sm),
            )
        }
    }
}
