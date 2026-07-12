package com.wakerep.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerep.app.BuildConfig
import com.wakerep.app.billing.BillingManager
import com.wakerep.app.data.SettingsRepository
import com.wakerep.app.ui.theme.WakerepColors
import com.wakerep.app.ui.theme.WakerepRadius
import com.wakerep.app.ui.theme.WakerepSpacing
import com.wakerep.app.ui.theme.WakerepType
import kotlinx.coroutines.launch

/**
 * Settings (design-system reference doc, section 07 "Stays quiet and
 * honest"): grouped cards on ink, calm - except Momentum mode, which gets
 * an amber border because it's the one "spicy" control (the drain mechanic
 * on the ringing screen, off by default).
 */
@Composable
fun SettingsScreen(
    billingManager: BillingManager,
    settingsRepository: SettingsRepository,
    onUpgrade: () -> Unit,
) {
    val isSubscribed by billingManager.isSubscribed.collectAsStateWithLifecycle()
    val momentumMode by settingsRepository.momentumModeEnabled.collectAsStateWithLifecycle(initialValue = false)
    val gradualVolume by settingsRepository.gradualVolumeEnabled.collectAsStateWithLifecycle(initialValue = true)
    val haptics by settingsRepository.hapticsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val wakeSound by settingsRepository.wakeSoundName.collectAsStateWithLifecycle(initialValue = "classic_alarm")
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = WakerepColors.InkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = WakerepType.H2, color = WakerepColors.TextHigh) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WakerepColors.InkCanvas),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(WakerepSpacing.xl)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(WakerepRadius.card))
                    .background(WakerepColors.Surface)
                    .padding(WakerepSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        if (isSubscribed) "Wakerep Plus" else "Free plan",
                        style = WakerepType.BodyEmphasis,
                        color = WakerepColors.TextHigh,
                    )
                    Text(
                        if (isSubscribed) "Unlimited alarms & exercises" else "1 active alarm. Upgrade for unlimited.",
                        style = WakerepType.Label,
                        color = WakerepColors.TextMid,
                    )
                }
                if (!isSubscribed) {
                    TextButton(onClick = onUpgrade) {
                        Text("Upgrade", style = WakerepType.BodyEmphasis, color = WakerepColors.Coral)
                    }
                }
            }

            SettingsSection(title = "Ringing screen") {
                SettingsRow(
                    title = "Momentum mode",
                    subtitle = "Earned light drains slowly if you stall. For the ruthless. Off by default.",
                    checked = momentumMode,
                    onCheckedChange = { coroutineScope.launch { settingsRepository.setMomentumMode(it) } },
                    accent = true,
                )
                SettingsLink(title = "Wake sound", value = wakeSoundLabel(wakeSound))
                SettingsRow(
                    title = "Gradual volume",
                    subtitle = null,
                    checked = gradualVolume,
                    onCheckedChange = { coroutineScope.launch { settingsRepository.setGradualVolume(it) } },
                )
                SettingsRow(
                    title = "Haptics",
                    subtitle = null,
                    checked = haptics,
                    onCheckedChange = { coroutineScope.launch { settingsRepository.setHaptics(it) } },
                )
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
                    Text("Version", style = WakerepType.Body, color = WakerepColors.TextMid)
                    Text(BuildConfig.VERSION_NAME, style = WakerepType.Body, color = WakerepColors.TextMid)
                }
            }
        }
    }
}

private fun wakeSoundLabel(name: String): String = when (name) {
    "classic_alarm" -> "Classic"
    else -> name.replace('_', ' ').replaceFirstChar(Char::uppercase)
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = WakerepSpacing.xl)) {
        if (title.isNotEmpty()) {
            Text(title.uppercase(), style = WakerepType.Label, color = WakerepColors.TextMid)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WakerepSpacing.sm)
                .clip(RoundedCornerShape(WakerepRadius.card))
                .background(WakerepColors.Surface)
                .padding(horizontal = WakerepSpacing.lg, vertical = WakerepSpacing.xs),
            content = content,
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (accent) {
                    Modifier
                        .padding(vertical = WakerepSpacing.sm)
                        .clip(RoundedCornerShape(WakerepRadius.control))
                        .border(1.dp, WakerepColors.Amber.copy(alpha = 0.5f), RoundedCornerShape(WakerepRadius.control))
                        .padding(WakerepSpacing.md)
                } else {
                    Modifier.padding(vertical = WakerepSpacing.md)
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = WakerepSpacing.md)) {
            Text(title, style = WakerepType.Body, color = WakerepColors.TextHigh)
            subtitle?.let {
                Text(it, style = WakerepType.Label, color = WakerepColors.TextMid, modifier = Modifier.padding(top = WakerepSpacing.xs))
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WakerepColors.TextHigh,
                checkedTrackColor = if (accent) WakerepColors.Amber else WakerepColors.Coral,
            ),
        )
    }
}

@Composable
private fun SettingsLink(text: String, onClick: () -> Unit) {
    Text(
        text,
        style = WakerepType.Body,
        color = WakerepColors.TextHigh,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = WakerepSpacing.md),
    )
}

@Composable
private fun SettingsLink(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = WakerepSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = WakerepType.Body, color = WakerepColors.TextHigh)
        Text("$value ›", style = WakerepType.Body, color = WakerepColors.TextMid)
    }
}
