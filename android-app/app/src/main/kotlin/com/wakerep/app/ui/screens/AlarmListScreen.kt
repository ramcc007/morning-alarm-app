package com.wakerep.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerep.app.model.Alarm
import com.wakerep.app.ui.AlarmListViewModel
import com.wakerep.app.ui.components.HorizonArc
import com.wakerep.app.ui.icons.WakerepIcons
import com.wakerep.app.ui.theme.WakerepColors
import com.wakerep.app.ui.theme.WakerepRadius
import com.wakerep.app.ui.theme.WakerepSpacing
import com.wakerep.app.ui.theme.WakerepType

/**
 * Home / alarm list (design-system reference doc, section 07 "Home / Alarm
 * List"): "alarms carry a horizon of their own" - the soonest-firing armed
 * alarm gets the biggest, warmest card; the rest sit as flat surface rows;
 * disabled alarms go cold gray. Status reads by heat, not a checkbox.
 */
@Composable
fun AlarmListScreen(
    viewModel: AlarmListViewModel,
    isSubscribed: Boolean,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Alarm) -> Unit,
    onBlockedByPaywall: () -> Unit,
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val nextAlarmId = alarms
        .filter { it.isEnabled }
        .minByOrNull { it.nextTriggerMillis() ?: Long.MAX_VALUE }
        ?.id

    Scaffold(
        containerColor = WakerepColors.InkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("Alarms", style = WakerepType.H2, color = WakerepColors.TextHigh) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WakerepColors.InkCanvas),
            )
        },
        floatingActionButton = {
            if (alarms.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddAlarm,
                    containerColor = WakerepColors.Coral,
                    shape = RoundedCornerShape(WakerepRadius.pill),
                ) {
                    WakerepIcons.Add(tint = Color.Black)
                }
            }
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            EmptyAlarmsState(modifier = Modifier.padding(padding), onAddAlarm = onAddAlarm)
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = WakerepSpacing.xl,
                    vertical = WakerepSpacing.lg,
                ),
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        isNext = alarm.id == nextAlarmId,
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
                    Spacer(Modifier.height(WakerepSpacing.md))
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
        HorizonArc(progress = 0f, diameter = 56.dp, strokeWidth = 3.dp, lit = false)
        Text(
            "No alarms yet",
            style = WakerepType.H2,
            color = WakerepColors.TextHigh,
            modifier = Modifier.padding(top = WakerepSpacing.lg),
        )
        Text(
            "Add one that only stops once you've earned it.",
            style = WakerepType.Body,
            color = WakerepColors.TextMid,
            modifier = Modifier.padding(top = WakerepSpacing.sm, bottom = WakerepSpacing.xl),
        )
        Text(
            "Create Your First Alarm",
            style = WakerepType.BodyEmphasis,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(WakerepRadius.pill))
                .background(WakerepColors.DawnGradient)
                .clickable(onClick = onAddAlarm)
                .padding(vertical = WakerepSpacing.md),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AlarmCard(alarm: Alarm, isNext: Boolean, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    val isArmed = alarm.isEnabled
    val cardColor = when {
        isNext && isArmed -> WakerepColors.SurfaceHigh
        isArmed -> WakerepColors.Surface
        else -> WakerepColors.InkSunk
    }
    val timeColor = if (isArmed) WakerepColors.TextHigh else WakerepColors.TextLow
    val timeStyle = if (isNext && isArmed) WakerepType.DisplayXl.copy(fontSize = 44.sp, lineHeight = 48.sp) else WakerepType.H1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WakerepRadius.card))
            .background(cardColor)
            .then(
                if (isNext && isArmed) {
                    Modifier.background(WakerepColors.dawnGlow(radius = 500f))
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = WakerepSpacing.xl, vertical = WakerepSpacing.lg),
    ) {
        Column {
            if (isNext && isArmed) {
                Text(
                    "NEXT",
                    style = WakerepType.Mono,
                    color = WakerepColors.Amber,
                    modifier = Modifier.padding(bottom = WakerepSpacing.xs),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(alarm.timeString, style = timeStyle, color = timeColor)
                    Text(
                        "${alarm.repTarget} ${alarm.exercise.displayName.lowercase()} · ${alarm.repeatSummary}",
                        style = WakerepType.Body,
                        color = WakerepColors.TextMid,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizonArc(
                        progress = if (isArmed) 1f else 0f,
                        lit = isArmed,
                        diameter = 26.dp,
                        strokeWidth = 3.dp,
                        modifier = Modifier.padding(end = WakerepSpacing.md),
                    )
                    Switch(
                        checked = isArmed,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WakerepColors.TextHigh,
                            checkedTrackColor = WakerepColors.Coral,
                        ),
                    )
                }
            }
        }
    }
}
