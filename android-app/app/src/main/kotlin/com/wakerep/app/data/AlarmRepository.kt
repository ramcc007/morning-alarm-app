package com.wakerep.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wakerep.app.model.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.alarmsDataStore by preferencesDataStore(name = "alarms")
private val ALARMS_KEY = stringPreferencesKey("alarms_json")
private val json = Json { ignoreUnknownKeys = true }

/** Source of truth for the user's alarms, persisted as JSON in DataStore. */
class AlarmRepository(private val context: Context) {

    val alarms: Flow<List<Alarm>> = context.alarmsDataStore.data.map { prefs ->
        val raw = prefs[ALARMS_KEY] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<Alarm>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun save(alarms: List<Alarm>) {
        context.alarmsDataStore.edit { prefs ->
            prefs[ALARMS_KEY] = json.encodeToString(alarms)
        }
    }

    suspend fun upsert(alarm: Alarm) {
        val current = currentAlarms().toMutableList()
        val index = current.indexOfFirst { it.id == alarm.id }
        if (index >= 0) current[index] = alarm else current.add(alarm)
        save(current.sortedWith(compareBy({ it.hour }, { it.minute })))
    }

    suspend fun delete(alarmId: String) {
        save(currentAlarms().filterNot { it.id == alarmId })
    }

    suspend fun setEnabled(alarmId: String, enabled: Boolean) {
        save(currentAlarms().map { if (it.id == alarmId) it.copy(isEnabled = enabled) else it })
    }

    private suspend fun currentAlarms(): List<Alarm> = alarms.first()
}
