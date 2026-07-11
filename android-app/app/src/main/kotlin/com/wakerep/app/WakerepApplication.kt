package com.wakerep.app

import android.app.Application
import com.wakerep.app.billing.BillingManager
import com.wakerep.app.data.AlarmRepository
import com.wakerep.app.data.SettingsRepository

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

    override fun onCreate() {
        super.onCreate()
        alarmRepository = AlarmRepository(this)
        billingManager = BillingManager(this).also { it.start() }
        settingsRepository = SettingsRepository(this)
    }
}
