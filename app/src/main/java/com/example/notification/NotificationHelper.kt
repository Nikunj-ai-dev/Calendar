package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.EventEntity

object NotificationHelper {
    const val CHANNEL_ID = "calendar_event_reminders"
    const val CHANNEL_NAME = "Event Reminders"
    const val ACTION_ACKNOWLEDGE = "com.example.calendar.ACTION_ACKNOWLEDGE_EVENT"
    const val ACTION_EVENT_REMINDER = "com.example.calendar.ACTION_EVENT_REMINDER"
    const val EXTRA_EVENT_ID = "extra_event_id"
    const val EXTRA_EVENT_TITLE = "extra_event_title"
    const val EXTRA_NOTIFICATION_COUNT = "extra_notification_count"
    const val EXTRA_TOTAL_ALLOWED = "extra_total_allowed"
    const val EXTRA_GAP_MINUTES = "extra_gap_minutes"

    private const val ACK_PREFS = "calendar_acknowledged_events"

    fun markAcknowledged(context: Context, eventId: String) {
        context.getSharedPreferences(ACK_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(eventId, true)
            .apply()
    }

    fun isAcknowledged(context: Context, eventId: String): Boolean {
        return context.getSharedPreferences(ACK_PREFS, Context.MODE_PRIVATE)
            .getBoolean(eventId, false)
    }

    fun clearAcknowledged(context: Context, eventId: String) {
        context.getSharedPreferences(ACK_PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(eventId)
            .apply()
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "High priority event arrival reminders and repeated alerts"
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleEventReminder(context: Context, event: EventEntity) {
        if (event.completed || event.isAcknowledged) {
            cancelEventReminder(context, event.id)
            return
        }

        clearAcknowledged(context, event.id)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = event.startTime - (event.reminderMinutesBefore * 60 * 1000L)

        // Only schedule if in future or within past 2 minutes
        if (triggerTime < System.currentTimeMillis() - 120000L) {
            return
        }

        val actualTrigger = if (triggerTime < System.currentTimeMillis()) System.currentTimeMillis() + 1000L else triggerTime

        val intent = Intent(context, CalendarAlarmReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            putExtra(EXTRA_EVENT_ID, event.id)
            putExtra(EXTRA_EVENT_TITLE, event.title)
            putExtra(EXTRA_NOTIFICATION_COUNT, 0)
            putExtra(EXTRA_TOTAL_ALLOWED, event.repeatNotificationCount)
            putExtra(EXTRA_GAP_MINUTES, event.repeatGapMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, actualTrigger, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, actualTrigger, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, actualTrigger, pendingIntent)
        }
    }

    fun scheduleRepeatAlarm(
        context: Context,
        eventId: String,
        title: String,
        currentCount: Int,
        totalAllowed: Int,
        gapMinutes: Int
    ) {
        if (isAcknowledged(context, eventId)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (gapMinutes * 60 * 1000L)

        val intent = Intent(context, CalendarAlarmReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            putExtra(EXTRA_EVENT_ID, eventId)
            putExtra(EXTRA_EVENT_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_COUNT, currentCount + 1)
            putExtra(EXTRA_TOTAL_ALLOWED, totalAllowed)
            putExtra(EXTRA_GAP_MINUTES, gapMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelEventReminder(context: Context, eventId: String) {
        markAcknowledged(context, eventId)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CalendarAlarmReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(eventId.hashCode())
    }

    fun showEventNotification(
        context: Context,
        eventId: String,
        title: String,
        count: Int,
        totalAllowed: Int,
        gapMinutes: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_EVENT_ID", eventId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val ackIntent = Intent(context, CalendarAlarmReceiver::class.java).apply {
            action = ACTION_ACKNOWLEDGE
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        val ackPendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode() + 1,
            ackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val subtitle = if (totalAllowed > 1) {
            "Reminder ${count + 1} of $totalAllowed (Repeats every $gapMinutes min until acknowledged)"
        } else {
            "Event is happening now!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📅 $title")
            .setContentText(subtitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Event starting: $title\n$subtitle. Tap Acknowledge to stop reminders."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Acknowledge",
                ackPendingIntent
            )
            .build()

        notificationManager.notify(eventId.hashCode(), notification)
    }
}
