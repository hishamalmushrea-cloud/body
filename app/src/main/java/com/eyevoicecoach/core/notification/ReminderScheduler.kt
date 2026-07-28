package com.eyevoicecoach.core.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eyevoicecoach.MainActivity
import com.eyevoicecoach.R
import java.util.Calendar
import javax.inject.Inject
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

private const val CHANNEL_ID = "daily_coaching_reminders"
private const val REMINDER_REQUEST = 903

/** Creates and updates the exact daily coaching reminder when platform access allows it. */
class ReminderScheduler @Inject constructor(private val context: Context) {
    /** Schedules the next reminder at [hour]:[minute], or removes it when [enabled] is false. */
    fun update(enabled: Boolean, hour: Int, minute: Int) {
        val manager = context.getSystemService(AlarmManager::class.java)
        val intent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST,
            Intent(context, DailyReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.cancel(intent)
        if (!enabled) return
        val whenToFire = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenToFire, intent)
        } else {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenToFire, intent)
        }
    }

    /** Creates the Android notification channel once. */
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "تذكير التدريب اليومي",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "تذكير خاص بتمرين العين والجسد والصوت" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

/** Receives one alarm, displays a private reminder, and schedules the next daily occurrence. */
class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val openApp = PendingIntent.getActivity(
            context,
            REMINDER_REQUEST,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("حان وقت تمرينك اليومي")
            .setContentText("دقيقة واحدة تكفي لتقوية حضورك وصوتك.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(REMINDER_REQUEST, notification)
        }
    }
}
