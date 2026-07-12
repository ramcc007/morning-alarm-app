package com.wakerep.app.model

/**
 * Medallions (design-system reference doc, section 07 "Achievements" -
 * "earned, a little smug, never cutesy"). Every unlock rule is a pure
 * function of the real [CompletionRecord] history - nothing here is
 * pre-seeded or fabricated, so a fresh install starts fully locked.
 */
enum class AchievementId { FIRST_LIGHT, DAWN_PATROL, NO_MERCY, SUNUP_30, CENTURY, IRON_DAWN }

data class Achievement(
    val id: AchievementId,
    val title: String,
    val description: String,
)

val ACHIEVEMENTS = listOf(
    Achievement(AchievementId.FIRST_LIGHT, "First Light", "Win your first morning"),
    Achievement(AchievementId.DAWN_PATROL, "Dawn Patrol", "Hit a 7-day streak"),
    Achievement(AchievementId.NO_MERCY, "No Mercy", "A full week, zero snoozes"),
    Achievement(AchievementId.SUNUP_30, "Sunup ×30", "30 mornings won"),
    Achievement(AchievementId.CENTURY, "Century", "100 mornings won"),
    Achievement(AchievementId.IRON_DAWN, "Iron Dawn", "Hit a 30-day streak"),
)

fun isAchievementUnlocked(id: AchievementId, records: List<CompletionRecord>, stats: CompletionStats): Boolean =
    when (id) {
        AchievementId.FIRST_LIGHT -> stats.totalMorningsWon >= 1
        AchievementId.DAWN_PATROL -> stats.bestStreak >= 7
        AchievementId.SUNUP_30 -> stats.totalMorningsWon >= 30
        AchievementId.CENTURY -> stats.totalMorningsWon >= 100
        AchievementId.IRON_DAWN -> stats.bestStreak >= 30
        AchievementId.NO_MERCY -> hasSnoozeFreeRun(records, minLength = 7)
    }

/** Whether any [minLength]-day consecutive run in the whole history has zero snoozed mornings in it. */
private fun hasSnoozeFreeRun(records: List<CompletionRecord>, minLength: Int): Boolean {
    val byDate = records.groupBy { it.date }
    val sortedDates = byDate.keys.sorted()
    if (sortedDates.size < minLength) return false

    var runStart = 0
    for (i in sortedDates.indices) {
        if (i > 0 && sortedDates[i - 1].plusDays(1) != sortedDates[i]) {
            runStart = i
        }
        val runLength = i - runStart + 1
        if (runLength >= minLength) {
            val window = sortedDates.subList(i - minLength + 1, i + 1)
            if (window.all { date -> byDate.getValue(date).none { it.usedSnooze } }) return true
        }
    }
    return false
}
