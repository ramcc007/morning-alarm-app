package com.morningalarm.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.morningalarm.app.MainActivity
import com.morningalarm.app.model.Alarm
import com.morningalarm.app.model.Day
import java.util.Calendar

const val EXTRA_ALARM_ID = "extra_alarm_id"
const val EXTRA_DAY_CODE = "extra_day_code" // Calendar.DAY_OF_WEEK value, or 0 for a one-shot alarm
private const val SNOOZE_DAY_CODE = 100 // outside the 0-7 range used by one-shot/weekly slots

/**
 * Wraps [AlarmManager] to schedule/cancel the alarms that back an [Alarm].
 *
 * AlarmManager has no native "repeat weekly" primitive, so each enabled
 * weekday gets its own single-shot [AlarmManager.setAlarmClock] entry for
 * its next occurrence; [AlarmReceiver] re-schedules that same weekday
 * 7 days out every time it fires, giving the effect of a weekly repeat.
 * `setAlarmClock` (rather than `setExactAndAllowWhileIdle`) is used
 * deliberately — it's the API meant for user-visible alarm-clock behavior
 * and is exempt from Doze/battery-optimization deferral.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        cancel(alarm)
        if (!alarm.isEnabled) return

        if (alarm.repeatDays.isEmpty) {
            scheduleOne(alarm, dayCode = 0, triggerAt = nextOccurrence(alarm, forDay = null))
        } else {
            for (day in alarm.repeatDays.days) {
                scheduleOne(alarm, dayCode = day.calendarValue, triggerAt = nextOccurrence(alarm, forDay = day))
            }
        }
    }

    /** Re-arms a single weekday 7 days after it just fired (called from [AlarmReceiver]). */
    fun rescheduleNextWeek(alarm: Alarm, dayCode: Int) {
        val day = Day.ordered.firstOrNull { it.calendarValue == dayCode } ?: return
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 7)
        }
        scheduleOne(alarm, dayCode = day.calendarValue, triggerAt = calendar.timeInMillis)
    }

    /** Schedules a one-shot snooze alarm at an arbitrary timestamp, independent of the regular weekly slots. */
    fun scheduleSnooze(alarm: Alarm, triggerAtMillis: Long) {
        scheduleOne(alarm, dayCode = SNOOZE_DAY_CODE, triggerAt = triggerAtMillis)
    }

    fun cancel(alarm: Alarm) {
        // Cancel every possible slot (one-shot + all 7 weekdays + snooze); harmless if never scheduled.
        val codes = listOf(0, SNOOZE_DAY_CODE) + (1..7)
        for (dayCode in codes) {
            val pendingIntent = pendingIntentFor(alarm, dayCode, create = false) ?: continue
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun scheduleOne(alarm: Alarm, dayCode: Int, triggerAt: Long) {
        val pendingIntent = pendingIntentFor(alarm, dayCode, create = true) ?: return
        val showIntent = PendingIntent.getActivity(
            context, alarm.baseRequestCode + dayCode,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, showIntent),
            pendingIntent
        )
    }

    private fun pendingIntentFor(alarm: Alarm, dayCode: Int, create: Boolean): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_DAY_CODE, dayCode)
        }
        val flags = if (create) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, alarm.baseRequestCode + dayCode, intent, flags)
    }

    /** Next timestamp (ms) matching [alarm]'s hour:minute, either today/tomorrow (forDay == null) or the next occurrence of [forDay]. */
    private fun nextOccurrence(alarm: Alarm, forDay: Day?): Long {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (forDay == null) {
            if (candidate.before(now)) candidate.add(Calendar.DAY_OF_YEAR, 1)
            return candidate.timeInMillis
        }

        while (candidate.get(Calendar.DAY_OF_WEEK) != forDay.calendarValue || candidate.before(now)) {
            candidate.add(Calendar.DAY_OF_YEAR, 1)
        }
        return candidate.timeInMillis
    }
}
