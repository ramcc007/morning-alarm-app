package com.morningalarm.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.morningalarm.app.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** AlarmManager entries don't survive a reboot, so re-arm every enabled alarm when the device comes back up. */
class BootRescheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = AlarmRepository(context.applicationContext)
                val scheduler = AlarmScheduler(context.applicationContext)
                repository.alarms.first().filter { it.isEnabled }.forEach { scheduler.schedule(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
