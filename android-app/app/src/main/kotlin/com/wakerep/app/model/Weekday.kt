package com.wakerep.app.model

import kotlinx.serialization.Serializable

/** Mirrors `java.util.Calendar.DAY_OF_WEEK` numbering (Sunday = 1) for easy AlarmManager scheduling. */
@Serializable
enum class Day(val calendarValue: Int, val shortLabel: String) {
    SUNDAY(1, "S"),
    MONDAY(2, "M"),
    TUESDAY(3, "T"),
    WEDNESDAY(4, "W"),
    THURSDAY(5, "T"),
    FRIDAY(6, "F"),
    SATURDAY(7, "S");

    companion object {
        val ordered = entries
    }
}

@Serializable
data class Weekday(val days: Set<Day> = emptySet()) {

    fun contains(day: Day) = days.contains(day)

    val isEmpty get() = days.isEmpty()

    val summary: String
        get() = when {
            this == EVERYDAY -> "Every day"
            this == WEEKDAYS -> "Weekdays"
            this == WEEKEND -> "Weekends"
            isEmpty -> "Once"
            else -> Day.ordered.filter { it in days }
                .joinToString(", ") { it.name.take(3).lowercase().replaceFirstChar(Char::uppercase) }
        }

    companion object {
        val NONE = Weekday(emptySet())
        val EVERYDAY = Weekday(Day.entries.toSet())
        val WEEKDAYS = Weekday(setOf(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY))
        val WEEKEND = Weekday(setOf(Day.SUNDAY, Day.SATURDAY))
    }
}
