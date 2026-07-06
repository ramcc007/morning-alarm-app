package com.morningalarm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morningalarm.app.billing.BillingManager
import com.morningalarm.app.ui.theme.AccentOrange
import com.morningalarm.app.ui.theme.Background
import com.morningalarm.app.ui.theme.SuccessGreen
import com.morningalarm.app.ui.theme.Surface
import com.morningalarm.app.ui.theme.TextPrimary
import com.morningalarm.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private data class Benefit(val icon: androidx.compose.ui.graphics.vector.ImageVector, val text: String)

private val benefits = listOf(
    Benefit(Icons.Filled.AllInclusive, "Unlimited active alarms"),
    Benefit(Icons.Filled.FitnessCenter, "Every exercise type, present and future"),
    Benefit(Icons.Filled.NotificationsActive, "Custom sounds, snooze rules & smart repeats"),
    Benefit(Icons.Filled.AutoAwesome, "New exercises added at no extra cost"),
)

@Composable
fun PaywallScreen(billingManager: BillingManager, onDismiss: () -> Unit) {
    val isSubscribed by billingManager.isSubscribed.collectAsStateWithLifecycle()
    val errorMessage by billingManager.lastErrorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(isSubscribed) {
        if (isSubscribed) onDismiss()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = AccentOrange, modifier = Modifier.padding(top = 24.dp))
        Text("MorningAlarm Plus", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 26.sp, modifier = Modifier.padding(top = 8.dp))
        Text("Wake up. Earn it. Own your morning.", color = TextSecondary, fontSize = 14.sp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .background(Surface, RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    Icon(benefit.icon, contentDescription = null, tint = SuccessGreen, modifier = Modifier.padding(end = 14.dp))
                    Text(benefit.text, color = TextPrimary)
                }
            }
        }

        Text(
            billingManager.trialDescription() ?: "3-day free trial, then ₹50.00/month",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            "Cancel anytime in Google Play. No charge until your trial ends.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp),
        )

        Button(
            onClick = { (context as? android.app.Activity)?.let { billingManager.purchase(it) } },
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
        ) {
            Text("Start Free Trial", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = { coroutineScope.launch { billingManager.refreshEntitlements() } }) {
            Text("Restore Purchases", color = TextSecondary, fontSize = 13.sp)
        }

        errorMessage?.let {
            Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp, textAlign = TextAlign.Center)
        }

        TextButton(onClick = onDismiss) {
            Text("Not Now", color = TextSecondary)
        }
    }
}
