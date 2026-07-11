package com.wakerep.app.ui.screens

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
import com.wakerep.app.BuildConfig
import com.wakerep.app.billing.BillingManager
import com.wakerep.app.ui.theme.WakerepColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(billingManager: BillingManager, onUpgrade: () -> Unit) {
    val isSubscribed by billingManager.isSubscribed.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = WakerepColors.InkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = WakerepColors.TextHigh) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WakerepColors.InkCanvas),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WakerepColors.Surface, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        if (isSubscribed) "Wakerep Plus" else "Free",
                        color = WakerepColors.TextHigh,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        if (isSubscribed) "Unlimited alarms & exercises" else "1 active alarm. Upgrade for unlimited.",
                        color = WakerepColors.TextMid,
                        fontSize = 12.sp,
                    )
                }
                if (!isSubscribed) {
                    TextButton(onClick = onUpgrade) {
                        Text("Upgrade", color = WakerepColors.Coral, fontWeight = FontWeight.Bold)
                    }
                }
            }

            SettingsSection(title = "About") {
                SettingsLink("Visit our website") { uriHandler.openUri("https://wakerep.app") }
                SettingsLink("Privacy Policy") { uriHandler.openUri("https://wakerep.app/privacy.html") }
                SettingsLink("Terms of Use") { uriHandler.openUri("https://wakerep.app/terms.html") }
                SettingsLink("Contact Support") { uriHandler.openUri("mailto:support@wakerep.app") }
            }

            SettingsSection(title = "Subscription") {
                SettingsLink("Restore Purchases") {
                    coroutineScope.launch { billingManager.refreshEntitlements() }
                }
            }

            SettingsSection(title = "") {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Version", color = WakerepColors.TextMid)
                    Text(BuildConfig.VERSION_NAME, color = WakerepColors.TextMid)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        if (title.isNotEmpty()) {
            Text(title.uppercase(), color = WakerepColors.TextMid, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(WakerepColors.Surface, RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 8.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingsLink(text: String, onClick: () -> Unit) {
    Text(
        text,
        color = WakerepColors.TextHigh,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    )
}
