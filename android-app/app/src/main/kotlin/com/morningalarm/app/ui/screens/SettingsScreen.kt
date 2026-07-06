package com.morningalarm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morningalarm.app.BuildConfig
import com.morningalarm.app.billing.BillingManager
import com.morningalarm.app.ui.theme.AccentOrange
import com.morningalarm.app.ui.theme.Background
import com.morningalarm.app.ui.theme.Surface
import com.morningalarm.app.ui.theme.TextPrimary
import com.morningalarm.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(billingManager: BillingManager, onUpgrade: () -> Unit) {
    val isSubscribed by billingManager.isSubscribed.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        if (isSubscribed) "MorningAlarm Plus" else "Free",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (isSubscribed) "Unlimited alarms & exercises" else "1 active alarm. Upgrade for unlimited.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                }
                if (!isSubscribed) {
                    TextButton(onClick = onUpgrade) {
                        Text("Upgrade", color = AccentOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }

            SettingsSection(title = "About") {
                SettingsLink("Visit our website") { uriHandler.openUri("https://morningalarm.app") }
                SettingsLink("Privacy Policy") { uriHandler.openUri("https://morningalarm.app/privacy.html") }
                SettingsLink("Terms of Use") { uriHandler.openUri("https://morningalarm.app/terms.html") }
                SettingsLink("Contact Support") { uriHandler.openUri("mailto:support@morningalarm.app") }
            }

            SettingsSection(title = "Subscription") {
                SettingsLink("Restore Purchases") {
                    coroutineScope.launch { billingManager.refreshEntitlements() }
                }
            }

            SettingsSection(title = "") {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Version", color = TextSecondary)
                    Text(BuildConfig.VERSION_NAME, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        if (title.isNotEmpty()) {
            Text(title.uppercase(), color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(Surface, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 8.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingsLink(text: String, onClick: () -> Unit) {
    Text(
        text,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    )
}
