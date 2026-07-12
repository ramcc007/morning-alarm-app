package com.wakerep.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerep.app.billing.BillingManager
import com.wakerep.app.ui.components.HorizonArc
import com.wakerep.app.ui.theme.WakerepColors
import com.wakerep.app.ui.theme.WakerepRadius
import com.wakerep.app.ui.theme.WakerepSpacing
import com.wakerep.app.ui.theme.WakerepType
import kotlinx.coroutines.launch

private val benefits = listOf(
    "Unlimited active alarms",
    "Every exercise type, present and future",
    "Momentum mode & full stats",
    "Custom sounds & snooze rules",
)

/**
 * Paywall (design-system reference doc, section 07 "The ask, and the
 * controls"): leads with the rising sun as the hero, not a feature-icon
 * table. Benefits use arc-checks, price is plain and honest - no countdown
 * timers, no fake discounts. "Not now" stays fully visible, never a hidden
 * close button.
 */
@Composable
fun PaywallScreen(billingManager: BillingManager, onDismiss: () -> Unit) {
    val isSubscribed by billingManager.isSubscribed.collectAsStateWithLifecycle()
    val errorMessage by billingManager.lastErrorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isSubscribed) {
        if (isSubscribed) onDismiss()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakerepColors.InkCanvas)
            .verticalScroll(rememberScrollState())
            .padding(WakerepSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SunHero()

        Text("Wakerep Plus", style = WakerepType.H1, color = WakerepColors.TextHigh, modifier = Modifier.padding(top = WakerepSpacing.lg))
        Text("Wake up. Earn it. Own your morning.", style = WakerepType.Body, color = WakerepColors.TextMid)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WakerepSpacing.xl)
                .clip(RoundedCornerShape(WakerepRadius.card))
                .background(WakerepColors.Surface)
                .padding(WakerepSpacing.lg),
        ) {
            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = WakerepSpacing.sm),
                ) {
                    HorizonArc(progress = 1f, diameter = 20.dp, strokeWidth = 2.5.dp, modifier = Modifier.padding(end = WakerepSpacing.md))
                    Text(benefit, style = WakerepType.Body, color = WakerepColors.TextHigh)
                }
            }
        }

        Text(
            billingManager.trialDescription() ?: "3 days free, then ₹50/month",
            style = WakerepType.BodyEmphasis,
            color = WakerepColors.TextHigh,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = WakerepSpacing.xl),
        )
        Text(
            "Cancel anytime in Google Play. No charge until your trial ends.",
            style = WakerepType.Label,
            color = WakerepColors.TextMid,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = WakerepSpacing.xs, start = WakerepSpacing.lg, end = WakerepSpacing.lg),
        )

        Button(
            onClick = { (context as? android.app.Activity)?.let { billingManager.purchase(it) } },
            shape = RoundedCornerShape(WakerepRadius.pill),
            modifier = Modifier.fillMaxWidth().padding(top = WakerepSpacing.lg),
            colors = ButtonDefaults.buttonColors(containerColor = WakerepColors.Coral),
        ) {
            Text("Start free trial", style = WakerepType.BodyEmphasis, color = Color.Black)
        }

        TextButton(onClick = { coroutineScope.launch { billingManager.refreshEntitlements() } }) {
            Text("Restore", style = WakerepType.Label, color = WakerepColors.TextMid)
        }

        errorMessage?.let {
            Text(it, style = WakerepType.Label, color = WakerepColors.Alert, textAlign = TextAlign.Center)
        }

        TextButton(onClick = onDismiss) {
            Text("Not now", style = WakerepType.Body, color = WakerepColors.TextMid)
        }
    }
}

@Composable
private fun SunHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(top = WakerepSpacing.lg),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val horizonY = size.height * 0.72f
            drawLine(
                brush = WakerepColors.DawnGradient,
                start = Offset(0f, horizonY),
                end = Offset(size.width, horizonY),
                strokeWidth = 1.5.dp.toPx(),
            )
            val center = Offset(size.width / 2f, horizonY)
            val radius = size.height * 0.42f
            clipRect(left = 0f, top = 0f, right = size.width, bottom = horizonY + 1f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(WakerepColors.Amber.copy(alpha = 0.35f), Color.Transparent),
                        center = center,
                        radius = radius * 2.6f,
                    ),
                    radius = radius * 2.6f,
                    center = center,
                )
                drawCircle(
                    brush = Brush.verticalGradient(colors = listOf(WakerepColors.Pink, WakerepColors.Coral, WakerepColors.Amber)),
                    radius = radius,
                    center = center,
                )
            }
        }
    }
}
