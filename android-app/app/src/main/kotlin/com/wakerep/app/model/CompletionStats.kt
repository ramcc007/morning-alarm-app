package com.wakerep.app.model

import java.time.LocalDate

/**
 * Derived stats over the raw [CompletionRecord] history - pure functions so
 * the Streaks and Achievements screens can compute them straight from
 * whatever DataStore returns, with no separate cache to fall out of sync.
 */
data class CompletionStats(
    val wonDates: Set<LocalDate>,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalMorningsWon: Int,
    val totalReps: Int,
    val snoozeFreeCurrentStreak: Boolean,
) {
    companion object {
        val EMPTY = CompletionStats(emptySet(), 0, 0, 0, 0, false)

        fun from(records: List<CompletionRecord>, today: LocalDate = LocalDate.now()): CompletionStats {
            if (records.isEmpty()) return EMPTY

            val byDate = records.groupBy { it.date }
            val wonDates = byDate.keys
            val totalReps = records.sumOf { it.repsCompleted }

            var streak = 0
            var cursor = if (wonDates.contains(today)) today else today.minusDays(1)
            while (wonDates.contains(cursor)) {
                streak++
                cursor = cursor.minusDays(1)
            }

            val sortedDates = wonDates.sorted()
            var best = 0
            var run = 0
            var previous: LocalDate? = null
            for (date in sortedDates) {
                run = if (previous != null && previous.plusDays(1) == date) run + 1 else 1
                best = maxOf(best, run)
                previous = date
            }

            val snoozeFree = streak > 0 && (0 until streak).all { offset ->
                val date = today.minusDays(offset.toLong())
                byDate[date]?.none { it.usedSnooze } ?: false
            }

            return CompletionStats(
                wonDates = wonDates,
                currentStreak = streak,
                bestStreak = best,
                totalMorningsWon = wonDates.size,
                totalReps = totalReps,
                snoozeFreeCurrentStreak = snoozeFree,
            )
        }
    }
}
