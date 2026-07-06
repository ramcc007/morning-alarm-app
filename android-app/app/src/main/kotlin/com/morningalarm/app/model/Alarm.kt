package com.morningalarm.app.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "Alarm",
    val hour: Int,
    val minute: Int,
    val repeatDays: Weekday = Weekday.NONE,
    val isEnabled: Boolean = true,
    val exercise: ExerciseType = ExerciseType.PUSHUPS,
    val repTarget: Int = exercise.defaultTarget,
    val soundName: String = "classic_alarm",
    val snoozeEnabled: Boolean = true,
    val snoozeMinutes: Int = 5,
) {
    val timeString: String
        get() {
            val h12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val amPm = if (hour < 12) "AM" else "PM"
            return "%d:%02d %s".format(h12, minute, amPm)
        }

    val repeatSummary: String get() = repeatDays.summary

    /**
     * A stable per-alarm base request code for AlarmManager PendingIntents.
     * Combined with a weekday offset (0-7) in AlarmScheduler so every
     * (alarm, weekday) pair gets its own unique alarm slot.
     */
    val baseRequestCode: Int get() = id.hashCode()
}
