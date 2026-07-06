package com.morningalarm.app

import android.app.Application
import com.morningalarm.app.billing.BillingManager
import com.morningalarm.app.data.AlarmRepository

/**
 * Minimal manual-DI container: no Hilt/Dagger, just a couple of
 * long-lived singletons the rest of the app reads off `application`.
 * Simple enough for this app's size; revisit if the dependency graph grows.
 */
class MorningAlarmApplication : Application() {
    lateinit var alarmRepository: AlarmRepository
        private set
    lateinit var billingManager: BillingManager
        private set

    override fun onCreate() {
        super.onCreate()
        alarmRepository = AlarmRepository(this)
        billingManager = BillingManager(this).also { it.start() }
    }
}
