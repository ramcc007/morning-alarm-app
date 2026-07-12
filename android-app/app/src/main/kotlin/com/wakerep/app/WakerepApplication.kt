package com.wakerep.app

import android.app.Application
import com.wakerep.app.billing.BillingManager
import com.wakerep.app.data.AlarmRepository
import com.wakerep.app.data.CompletionRepository
import com.wakerep.app.data.SettingsRepository
import java.util.Collections

/**
 * Minimal manual-DI container: no Hilt/Dagger, just a couple of
 * long-lived singletons the rest of the app reads off `application`.
 * Simple enough for this app's size; revisit if the dependency graph grows.
 */
class WakerepApplication : Application() {
    lateinit var alarmRepository: AlarmRepository
        private set
    lateinit var billingManager: BillingManager
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var completionRepository: CompletionRepository
        private set

    /**
     * Alarm IDs snoozed at least once during the current wake attempt, so
     * the eventual completion record can be marked [not] snooze-free for
     * the "No Mercy" achievement. In-memory only - losing this to a process
     * death mid-snooze just means that one morning isn't flagged, which is
     * an acceptable edge case for a gamification detail.
     */
    val snoozedAlarmIds: MutableSet<String> = Collections.synchronizedSet(mutableSetOf())

    override fun onCreate() {
        super.onCreate()
        alarmRepository = AlarmRepository(this)
        billingManager = BillingManager(this).also { it.start() }
        settingsRepository = SettingsRepository(this)
        completionRepository = CompletionRepository(this)
    }
}
