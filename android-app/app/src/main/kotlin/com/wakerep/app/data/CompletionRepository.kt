package com.wakerep.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wakerep.app.model.CompletionRecord
import com.wakerep.app.model.ExerciseType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

private val Context.completionsDataStore by preferencesDataStore(name = "completions")
private val COMPLETIONS_KEY = stringPreferencesKey("completions_json")
private val json = Json { ignoreUnknownKeys = true }

/**
 * Source of truth for "mornings won" (design-system reference doc, section
 * 07 Streaks & Achievements screens). Persisted as JSON in DataStore,
 * mirroring [AlarmRepository]'s pattern.
 */
class CompletionRepository(private val context: Context) {

    val completions: Flow<List<CompletionRecord>> = context.completionsDataStore.data.map { prefs ->
        val raw = prefs[COMPLETIONS_KEY] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<CompletionRecord>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun recordCompletion(exercise: ExerciseType, repsCompleted: Int, usedSnooze: Boolean, date: LocalDate = LocalDate.now()) {
        val current = completions.first().toMutableList()
        current.add(CompletionRecord(date.toEpochDay(), exercise, repsCompleted, usedSnooze))
        context.completionsDataStore.edit { prefs ->
            prefs[COMPLETIONS_KEY] = json.encodeToString(current)
        }
    }
}
