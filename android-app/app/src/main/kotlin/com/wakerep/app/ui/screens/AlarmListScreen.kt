package com.wakerep.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerep.app.model.Alarm
import com.wakerep.app.ui.AlarmListViewModel
import com.wakerep.app.ui.icons.WakerepIcons
import com.wakerep.app.ui.theme.WakerepColors
import com.wakerep.app.ui.theme.WakerepRadius
import com.wakerep.app.ui.theme.WakerepSpacing
import com.wakerep.app.ui.theme.WakerepType

@Composable
fun AlarmListScreen(
    viewModel: AlarmListViewModel,
    isSubscribed: Boolean,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Alarm) -> Unit,
    onBlockedByPaywall: () -> Unit,
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = WakerepColors.InkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("Alarms", style = WakerepType.H2, color = WakerepColors.TextHigh) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WakerepColors.InkCanvas),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAlarm,
                containerColor = WakerepColors.Coral,
                shape = RoundedCornerShape(WakerepRadius.pill),
            ) {
                WakerepIcons.Add(tint = Color.Black)
            }
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            EmptyAlarmsState(modifier = Modifier.padding(padding), onAddAlarm = onAddAlarm)
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmRow(
                        alarm = alarm,
                        onClick = { onEditAlarm(alarm) },
                        onToggle = { enabled ->
                            val enabledCount = alarms.count { it.isEnabled }
                            if (enabled && !isSubscribed && enabledCount >= 1) {
                                onBlockedByPaywall()
                            } else {
                                viewModel.setEnabled(alarm, enabled)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAlarmsState(modifier: Modifier = Modifier, onAddAlarm: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        WakerepIcons.Exercise(
            size = 40.dp,
            tint = WakerepColors.Coral,
            modifier = Modifier.padding(bottom = WakerepSpacing.lg),
        )
        Text("No alarms yet", style = WakerepType.H2, color = WakerepColors.TextHigh)
        Text(
            "Add an alarm that only stops once you've earned it.",
            style = WakerepType.Body,
            color = WakerepColors.TextMid,
            modifier = Modifier.padding(top = WakerepSpacing.sm, bottom = WakerepSpacing.xl),
        )
        Text(
            "Create Your First Alarm",
            style = WakerepType.BodyEmphasis,
            color = Color.Black,
            modifier = Modifier
                .clip(RoundedCornerShape(WakerepRadius.pill))
                .background(WakerepColors.DawnGradient)
                .clickable(onClick = onAddAlarm)
                .padding(horizontal = WakerepSpacing.xl, vertical = WakerepSpacing.md),
        )
    }
}

@Composable
private fun AlarmRow(alarm: Alarm, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = WakerepSpacing.xl, vertical = WakerepSpacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                alarm.timeString,
                style = WakerepType.H1,
                color = if (alarm.isEnabled) WakerepColors.TextHigh else WakerepColors.TextLow,
            )
            Text(
                "${alarm.repTarget} ${alarm.exercise.displayName.lowercase()} · ${alarm.repeatSummary}",
                style = WakerepType.Body,
                color = WakerepColors.TextMid,
            )
        }
        Switch(
            checked = alarm.isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WakerepColors.TextHigh,
                checkedTrackColor = WakerepColors.Coral,
            ),
        )
    }
}
