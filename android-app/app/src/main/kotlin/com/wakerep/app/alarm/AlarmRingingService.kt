package com.wakerep.app.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

private const val CHANNEL_ID = "alarm_ringing"
private const val NOTIFICATION_ID = 42
const val ACTION_STOP_RINGING = "com.wakerep.app.action.STOP_RINGING"

/**
 * Foreground service that owns the alarm's looping audio and the
 * full-screen-intent notification that launches [AlarmRingingActivity] over
 * the lock screen. This is the piece that makes the alarm "impossible to
 * ignore" on Android: the notification is posted with
 * [NotificationCompat.Builder.setFullScreenIntent], which the system is
 * required to honor by launching the activity immediately, even when the
 * screen is off or locked.
 */
class AlarmRingingService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_RINGING) {
            stopRinging()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getStringExtra(EXTRA_ALARM_ID)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(alarmId))
        startAudio()
        return START_STICKY
    }

    private fun startAudio() {
        if (mediaPlayer != null) return
        val soundResId = resources.getIdentifier("classic_alarm", "raw", packageName)
        if (soundResId == 0) return // No sound bundled yet - see res/raw for setup instructions.

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            isLooping = true
            try {
                val afd = resources.openRawResourceFd(soundResId)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                start()
            } catch (_: Exception) {
                release()
            }
        }
    }

    private fun stopRinging() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }

    private fun buildNotification(alarmId: String?) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle("Wakerep")
        .setContentText("Do your reps to stop the alarm")
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setOngoing(true)
        .setFullScreenIntent(ringingActivityIntent(alarmId), true)
        .build()

    private fun ringingActivityIntent(alarmId: String?): PendingIntent {
        val intent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        return PendingIntent.getActivity(
            this, NOTIFICATION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID, "Alarm ringing", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows while an alarm is ringing and waiting for your reps"
            setSound(null, null) // MediaPlayer handles the looping alarm sound instead.
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        fun stopIntent(context: Context) =
            Intent(context, AlarmRingingService::class.java).setAction(ACTION_STOP_RINGING)
    }
}
