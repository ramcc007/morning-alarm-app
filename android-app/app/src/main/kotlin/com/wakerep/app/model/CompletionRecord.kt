package com.wakerep.app.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * One morning won: logged only when the user actually dismisses an alarm by
 * finishing their reps (never for a raw stop or a snooze). Backs the
 * Streaks/Stats and Achievements screens - both read straight off this
 * history so nothing is fabricated.
 */
@Serializable
data class CompletionRecord(
    val dateEpochDay: Long,
    val exercise: ExerciseType,
    val repsCompleted: Int,
    val usedSnooze: Boolean = false,
) {
    val date: LocalDate get() = LocalDate.ofEpochDay(dateEpochDay)

    companion object {
        fun forToday(exercise: ExerciseType, repsCompleted: Int, usedSnooze: Boolean = false) =
            CompletionRecord(LocalDate.now().toEpochDay(), exercise, repsCompleted, usedSnooze)
    }
}
