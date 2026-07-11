package com.wakerep.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

private val MOMENTUM_MODE_KEY = booleanPreferencesKey("momentum_mode_enabled")
private val GRADUAL_VOLUME_KEY = booleanPreferencesKey("gradual_volume_enabled")
private val HAPTICS_KEY = booleanPreferencesKey("haptics_enabled")
private val WAKE_SOUND_KEY = stringPreferencesKey("wake_sound_name")

/**
 * App-wide preferences that aren't tied to a specific alarm: the ringing
 * screen's optional "Momentum mode" (design-system reference doc, section 07
 * - earned light drains slowly if you stall, off by default), plus the
 * general sound/haptics toggles shown in Settings.
 */
class SettingsRepository(private val context: Context) {

    val momentumModeEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[MOMENTUM_MODE_KEY] ?: false }

    val gradualVolumeEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[GRADUAL_VOLUME_KEY] ?: true }

    val hapticsEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[HAPTICS_KEY] ?: true }

    val wakeSoundName: Flow<String> =
        context.settingsDataStore.data.map { it[WAKE_SOUND_KEY] ?: "classic_alarm" }

    suspend fun setMomentumMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[MOMENTUM_MODE_KEY] = enabled }
    }

    suspend fun setGradualVolume(enabled: Boolean) {
        context.settingsDataStore.edit { it[GRADUAL_VOLUME_KEY] = enabled }
    }

    suspend fun setHaptics(enabled: Boolean) {
        context.settingsDataStore.edit { it[HAPTICS_KEY] = enabled }
    }

    suspend fun setWakeSound(name: String) {
        context.settingsDataStore.edit { it[WAKE_SOUND_KEY] = name }
    }
}
