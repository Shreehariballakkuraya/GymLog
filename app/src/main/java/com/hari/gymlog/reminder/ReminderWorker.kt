package com.hari.gymlog.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hari.gymlog.R

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "gymlog_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val KEY_EXERCISE     = "exercise_name"
        const val KEY_MODE          = "exercise_mode"
        const val KEY_TARGET_REPS   = "target_reps"
        const val KEY_COUNTDOWN_SECS = "countdown_secs"
        const val KEY_PACE_SECS     = "pace_secs"
        const val KEY_HOLD_SECS     = "hold_secs"
        const val KEY_START_HOUR    = "start_hour"
        const val KEY_END_HOUR      = "end_hour"
        const val KEY_SCHEDULE_ID   = "schedule_id"
        const val WORK_TAG_PREFIX   = "exercise_reminder_work_"
    }

    override fun doWork(): Result {
        val exerciseName  = inputData.getString(KEY_EXERCISE) ?: "Push-ups"
        val mode          = inputData.getString(KEY_MODE) ?: ReminderSettingsFragment.MODE_REPS
        val targetReps    = inputData.getInt(KEY_TARGET_REPS, 10)
        val countdownSecs = inputData.getInt(KEY_COUNTDOWN_SECS, 10)
        val paceSecs      = inputData.getInt(KEY_PACE_SECS, 3)
        val holdSecs      = inputData.getInt(KEY_HOLD_SECS, 60)
        val startHour     = inputData.getInt(KEY_START_HOUR, 10)
        val endHour       = inputData.getInt(KEY_END_HOUR, 18)

        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (currentHour < startHour || currentHour >= endHour) {
            // Outside of active hours, silently succeed without notifying
            return Result.success()
        }

        createNotificationChannel()
        sendReminderNotification(exerciseName, mode, targetReps, countdownSecs, paceSecs, holdSecs)
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Exercise Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timely reminders to do quick exercises"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendReminderNotification(
        exerciseName: String, mode: String, targetReps: Int,
        countdownSecs: Int, paceSecs: Int, holdSecs: Int
    ) {
        // Intent to launch the session activity when notification is tapped
        val sessionIntent = Intent(context, ReminderSessionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderSessionActivity.EXTRA_EXERCISE, exerciseName)
            putExtra(ReminderSessionActivity.EXTRA_MODE, mode)
            putExtra(ReminderSessionActivity.EXTRA_TARGET_REPS, targetReps)
            putExtra(ReminderSessionActivity.EXTRA_COUNTDOWN_SECS, countdownSecs)
            putExtra(ReminderSessionActivity.EXTRA_PACE_SECS, paceSecs)
            putExtra(ReminderSessionActivity.EXTRA_HOLD_SECS, holdSecs)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            sessionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_workout)
            .setContentTitle("💪 Time for $exerciseName!")
            .setContentText("Tap to start your $targetReps rep set. Ready?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted on Android 13+
        }
    }
}
