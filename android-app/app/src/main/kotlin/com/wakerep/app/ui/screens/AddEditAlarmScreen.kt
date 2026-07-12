package com.wakerep.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakerep.app.model.Alarm
import com.wakerep.app.model.Day
import com.wakerep.app.model.ExerciseType
import com.wakerep.app.model.Weekday
import com.wakerep.app.ui.components.HorizonArc
import com.wakerep.app.ui.theme.WakerepColors
import com.wakerep.app.ui.theme.WakerepRadius
import com.wakerep.app.ui.theme.WakerepSpacing
import com.wakerep.app.ui.theme.WakerepType

/**
 * Add/Edit alarm (design-system reference doc, section 07 "Set the contract
 * you'll have to honor"): giant time first, then the exercise you're
 * committing to. The exercise picker is the emotional core - selecting one
 * lights its card with an amber border + arc glow rather than a plain
 * checkmark, and resets the rep target to that exercise's default.
 */
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
        containerColor = WakerepColors.InkCanvas,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingAlarm == null) "New Alarm" else "Edit Alarm",
                        style = WakerepType.H2,
                        color = WakerepColors.TextHigh,
                    )
                },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancel", style = WakerepType.Body, color = WakerepColors.TextMid) } },
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
                    }) { Text("Save", style = WakerepType.BodyEmphasis, color = WakerepColors.Coral) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WakerepColors.InkCanvas),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(WakerepSpacing.xl),
        ) {
            TimePickerCard(hour = hour, minute = minute, onHourChange = { hour = it }, onMinuteChange = { minute = it })

            SectionTitle("Label", topPadding = WakerepSpacing.xl)
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                shape = RoundedCornerShape(WakerepRadius.field),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WakerepColors.Coral,
                    unfocusedBorderColor = WakerepColors.Hairline,
                    focusedTextColor = WakerepColors.TextHigh,
                    unfocusedTextColor = WakerepColors.TextHigh,
                    cursorColor = WakerepColors.Coral,
                ),
                modifier = Modifier.fillMaxWidth().padding(top = WakerepSpacing.sm),
            )

            SectionTitle("Repeat", topPadding = WakerepSpacing.xl)
            WeekdayPicker(selection = repeatDays, onChange = { repeatDays = it })

            SectionTitle("Exercise to Stop Alarm", topPadding = WakerepSpacing.xl)
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
                    .padding(top = WakerepSpacing.xl)
                    .clip(RoundedCornerShape(WakerepRadius.card))
                    .background(WakerepColors.Surface)
                    .padding(WakerepSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Allow Snooze", style = WakerepType.Body, color = WakerepColors.TextHigh)
                Switch(
                    checked = snoozeEnabled,
                    onCheckedChange = { snoozeEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = WakerepColors.TextHigh, checkedTrackColor = WakerepColors.Coral),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, topPadding: Dp = 0.dp) {
    Text(
        text.uppercase(),
        style = WakerepType.Label,
        color = WakerepColors.TextMid,
        modifier = Modifier.padding(top = topPadding, bottom = WakerepSpacing.sm),
    )
}

@Composable
private fun TimePickerCard(hour: Int, minute: Int, onHourChange: (Int) -> Unit, onMinuteChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WakerepRadius.card))
            .background(WakerepColors.Surface)
            .background(WakerepColors.dawnGlow(radius = 700f))
            .padding(vertical = WakerepSpacing.xl),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberStepper(value = hour, range = 0..23, onChange = onHourChange)
        Text(":", style = WakerepType.DisplayXl.copy(fontSize = 56.sp), color = WakerepColors.TextHigh, modifier = Modifier.padding(horizontal = WakerepSpacing.sm))
        NumberStepper(value = minute, range = 0..59, onChange = onMinuteChange)
    }
}

@Composable
private fun NumberStepper(value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "%02d".format(value),
            style = WakerepType.DisplayXl.copy(fontSize = 56.sp),
            color = WakerepColors.TextHigh,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(WakerepSpacing.lg)) {
            TextButton(onClick = { onChange(if (value <= range.first) range.last else value - 1) }) {
                Text("−", style = WakerepType.H2, color = WakerepColors.Coral)
            }
            TextButton(onClick = { onChange(if (value >= range.last) range.first else value + 1) }) {
                Text("+", style = WakerepType.H2, color = WakerepColors.Coral)
            }
        }
    }
}

@Composable
private fun WeekdayPicker(selection: Weekday, onChange: (Weekday) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(WakerepSpacing.sm)) {
        Day.ordered.forEach { day ->
            val isSelected = selection.contains(day)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier.background(WakerepColors.DawnGradient)
                        else Modifier.background(WakerepColors.SurfaceHigh)
                    )
                    .clickable {
                        val newDays = if (isSelected) selection.days - day else selection.days + day
                        onChange(Weekday(newDays))
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    day.shortLabel,
                    style = WakerepType.BodyEmphasis,
                    color = if (isSelected) Color.Black else WakerepColors.TextMid,
                )
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
            .padding(top = WakerepSpacing.sm)
            .clip(RoundedCornerShape(WakerepRadius.card))
            .background(if (isSelected) WakerepColors.SurfaceHigh else WakerepColors.Surface)
            .then(if (isSelected) Modifier.background(WakerepColors.dawnGlow(radius = 400f)) else Modifier)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) WakerepColors.Amber else Color.Transparent,
                shape = RoundedCornerShape(WakerepRadius.card),
            )
            .clickable(onClick = onSelect)
            .padding(WakerepSpacing.lg),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(type.displayName, style = WakerepType.BodyEmphasis, color = WakerepColors.TextHigh)
            HorizonArc(progress = if (isSelected) 1f else 0f, lit = isSelected, diameter = 22.dp, strokeWidth = 2.5.dp)
        }
        if (isSelected) {
            Row(
                modifier = Modifier.padding(top = WakerepSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Target: $repTarget reps", style = WakerepType.Body, color = WakerepColors.TextMid)
                Row {
                    TextButton(onClick = {
                        if (repTarget > type.targetRange.first) onRepTargetChange(repTarget - 1)
                    }) { Text("−", style = WakerepType.BodyEmphasis, color = WakerepColors.Coral) }
                    TextButton(onClick = {
                        if (repTarget < type.targetRange.last) onRepTargetChange(repTarget + 1)
                    }) { Text("+", style = WakerepType.BodyEmphasis, color = WakerepColors.Coral) }
                }
            }
        }
    }
}
