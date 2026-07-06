package com.morningalarm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morningalarm.app.model.Alarm
import com.morningalarm.app.model.Day
import com.morningalarm.app.model.ExerciseType
import com.morningalarm.app.model.Weekday
import com.morningalarm.app.ui.theme.AccentOrange
import com.morningalarm.app.ui.theme.Background
import com.morningalarm.app.ui.theme.SuccessGreen
import com.morningalarm.app.ui.theme.SurfaceElevated
import com.morningalarm.app.ui.theme.TextPrimary
import com.morningalarm.app.ui.theme.TextSecondary

@Composable
fun AddEditAlarmScreen(
    existingAlarm: Alarm?,
    onSave: (Alarm) -> Unit,
    onCancel: () -> Unit,
) {
    var hour by remember { mutableIntStateOf(existingAlarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(existingAlarm?.minute ?: 0) }
    var label by remember { mutableStateOf(existingAlarm?.label ?: "Alarm") }
    var repeatDays by remember { mutableStateOf(existingAlarm?.repeatDays ?: Weekday.NONE) }
    var exercise by remember { mutableStateOf(existingAlarm?.exercise ?: ExerciseType.PUSHUPS) }
    var repTarget by remember { mutableIntStateOf(existingAlarm?.repTarget ?: ExerciseType.PUSHUPS.defaultTarget) }
    var snoozeEnabled by remember { mutableStateOf(existingAlarm?.snoozeEnabled ?: true) }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(if (existingAlarm == null) "New Alarm" else "Edit Alarm", color = TextPrimary) },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancel", color = TextSecondary) } },
                actions = {
                    TextButton(onClick = {
                        val alarm = (existingAlarm ?: Alarm(hour = hour, minute = minute)).copy(
                            hour = hour,
                            minute = minute,
                            label = label.ifBlank { "Alarm" },
                            repeatDays = repeatDays,
                            exercise = exercise,
                            repTarget = repTarget,
                            snoozeEnabled = snoozeEnabled,
                        )
                        onSave(alarm)
                    }) { Text("Save", color = AccentOrange, fontWeight = FontWeight.Bold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(20.dp),
        ) {
            TimePickerRow(hour = hour, minute = minute, onHourChange = { hour = it }, onMinuteChange = { minute = it })

            SectionTitle("Label")
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp),
            )

            SectionTitle("Repeat")
            WeekdayPicker(selection = repeatDays, onChange = { repeatDays = it })

            SectionTitle("Exercise to Stop Alarm", topPadding = 24.dp)
            ExerciseType.entries.forEach { type ->
                ExerciseOptionCard(
                    type = type,
                    isSelected = exercise == type,
                    repTarget = repTarget,
                    onSelect = { exercise = type; repTarget = type.defaultTarget },
                    onRepTargetChange = { repTarget = it },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .background(SurfaceElevated, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Allow Snooze", color = TextPrimary)
                Switch(
                    checked = snoozeEnabled,
                    onCheckedChange = { snoozeEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AccentOrange),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, topPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    Text(
        text.uppercase(),
        color = TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = topPadding),
    )
}

@Composable
private fun TimePickerRow(hour: Int, minute: Int, onHourChange: (Int) -> Unit, onMinuteChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated, RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberStepper(value = hour, range = 0..23, onChange = onHourChange, label = "Hour")
        Text(":", color = TextPrimary, fontSize = 28.sp, modifier = Modifier.padding(horizontal = 12.dp))
        NumberStepper(value = minute, range = 0..59, onChange = onMinuteChange, label = "Min")
    }
}

@Composable
private fun NumberStepper(value: Int, range: IntRange, onChange: (Int) -> Unit, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("%02d".format(value), color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Row {
            TextButton(onClick = { onChange(if (value <= range.first) range.last else value - 1) }) { Text("-", color = AccentOrange) }
            TextButton(onClick = { onChange(if (value >= range.last) range.first else value + 1) }) { Text("+", color = AccentOrange) }
        }
    }
}

@Composable
private fun WeekdayPicker(selection: Weekday, onChange: (Weekday) -> Unit) {
    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Day.ordered.forEach { day ->
            val isSelected = selection.contains(day)
            Row(
                modifier = Modifier
                    .size(36.dp)
                    .background(if (isSelected) AccentOrange else SurfaceElevated, CircleShape)
                    .clickable {
                        val newDays = if (isSelected) selection.days - day else selection.days + day
                        onChange(Weekday(newDays))
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(day.shortLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ExerciseOptionCard(
    type: ExerciseType,
    isSelected: Boolean,
    repTarget: Int,
    onSelect: () -> Unit,
    onRepTargetChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(SurfaceElevated, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(type.displayName, color = TextPrimary)
            if (isSelected) Icon(Icons.Filled.Check, contentDescription = null, tint = SuccessGreen)
        }
        if (isSelected) {
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Target: $repTarget reps", color = TextSecondary, fontSize = 14.sp)
                Row {
                    TextButton(onClick = {
                        if (repTarget > type.targetRange.first) onRepTargetChange(repTarget - 1)
                    }) { Text("-", color = AccentOrange) }
                    TextButton(onClick = {
                        if (repTarget < type.targetRange.last) onRepTargetChange(repTarget + 1)
                    }) { Text("+", color = AccentOrange) }
                }
            }
        }
    }
}
