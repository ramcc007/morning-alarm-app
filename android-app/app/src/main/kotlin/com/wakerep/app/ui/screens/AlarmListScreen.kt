package com.wakerep.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerep.app.model.Alarm
import com.wakerep.app.ui.AlarmListViewModel
import com.wakerep.app.ui.theme.AccentOrange
import com.wakerep.app.ui.theme.Background
import com.wakerep.app.ui.theme.TextPrimary
import com.wakerep.app.ui.theme.TextSecondary

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
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Alarms", color = TextPrimary, fontWeight = FontWeight.Bold) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = Background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAlarm, containerColor = AccentOrange) {
                Icon(Icons.Filled.Add, contentDescription = "Add alarm", tint = Color.Black)
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
        Icon(
            Icons.Filled.FitnessCenter,
            contentDescription = null,
            tint = AccentOrange,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Text("No alarms yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            "Add an alarm that only stops once you've earned it.",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )
        androidx.compose.material3.Button(onClick = onAddAlarm) {
            Text("Create Your First Alarm")
        }
    }
}

@Composable
private fun AlarmRow(alarm: Alarm, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                alarm.timeString,
                color = if (alarm.isEnabled) TextPrimary else TextSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
            )
            Text(
                "${alarm.repTarget} ${alarm.exercise.displayName.lowercase()} · ${alarm.repeatSummary}",
                color = TextSecondary,
                fontSize = 14.sp,
            )
        }
        Switch(
            checked = alarm.isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentOrange),
        )
    }
}
