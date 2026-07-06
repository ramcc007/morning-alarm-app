package com.morningalarm.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.morningalarm.app.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Fired by [AlarmManager] at the scheduled time. Starts the ringing foreground service. */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(EXTRA_ALARM_ID) ?: return
        val dayCode = intent.getIntExtra(EXTRA_DAY_CODE, 0)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = AlarmRepository(context.applicationContext)
                val alarm = repository.alarms.first().firstOrNull { it.id == alarmId }
                if (alarm != null && alarm.isEnabled) {
                    val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
                        putExtra(EXTRA_ALARM_ID, alarmId)
                    }
                    ContextCompat.startForegroundService(context, serviceIntent)

                    if (dayCode != 0) {
                        AlarmScheduler(context.applicationContext).rescheduleNextWeek(alarm, dayCode)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
