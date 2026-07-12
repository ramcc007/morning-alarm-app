package com.wakerep.app.model

import kotlinx.serialization.Serializable
import java.util.Calendar
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

    /**
     * Next timestamp this alarm will fire, for display purposes only (the
     * home screen's "next alarm" heat hierarchy) - mirrors AlarmScheduler's
     * own occurrence math without touching AlarmManager. Null if disabled.
     */
    fun nextTriggerMillis(now: Calendar = Calendar.getInstance()): Long? {
        if (!isEnabled) return null

        fun candidateAt(): Calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (repeatDays.isEmpty) {
            val candidate = candidateAt()
            if (candidate.before(now)) candidate.add(Calendar.DAY_OF_YEAR, 1)
            return candidate.timeInMillis
        }

        return repeatDays.days.minOfOrNull { day ->
            val candidate = candidateAt()
            while (candidate.get(Calendar.DAY_OF_WEEK) != day.calendarValue || candidate.before(now)) {
                candidate.add(Calendar.DAY_OF_YEAR, 1)
            }
            candidate.timeInMillis
        }
    }
}
