package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.EventEntity
import com.example.util.DateUtils

object NotificationHelper {
    const val CHANNEL_ID = "calendar_event_reminders"
    const val CHANNEL_NAME = "Event Reminders"
    const val CHANNEL_BIRTHDAY_ID = "calendar_birthday_reminders"
    const val CHANNEL_BIRTHDAY_NAME = "Birthday & Celebrations"

    const val ACTION_ACKNOWLEDGE = "com.example.calendar.ACTION_ACKNOWLEDGE_EVENT"
    const val ACTION_EVENT_REMINDER = "com.example.calendar.ACTION_EVENT_REMINDER"

    const val EXTRA_EVENT_ID = "extra_event_id"
    const val EXTRA_EVENT_TITLE = "extra_event_title"
    const val EXTRA_EVENT_CATEGORY = "extra_event_category"
    const val EXTRA_EVENT_LOCATION = "extra_event_location"
    const val EXTRA_EVENT_DESCRIPTION = "extra_event_description"
    const val EXTRA_NOTIFICATION_COUNT = "extra_notification_count"
    const val EXTRA_TOTAL_ALLOWED = "extra_total_allowed"
    const val EXTRA_GAP_MINUTES = "extra_gap_minutes"
    const val EXTRA_IS_BIRTHDAY = "extra_is_birthday"

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
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            // 1. Standard Event Channel
            val eventChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority event arrival reminders and repeated alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                enableLights(true)
                lightColor = Color.GREEN
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(eventChannel)

            // 2. Birthday & Celebrations Channel
            val birthdayChannel = NotificationChannel(
                CHANNEL_BIRTHDAY_ID,
                CHANNEL_BIRTHDAY_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Special birthday alerts, party reminders and greeting notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300, 150, 600)
                enableLights(true)
                lightColor = Color.MAGENTA
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(birthdayChannel)
        }
    }

    fun scheduleEventReminder(context: Context, event: EventEntity) {
        createNotificationChannel(context)

        if (event.completed || event.isAcknowledged) {
            cancelEventReminder(context, event.id)
            return
        }

        clearAcknowledged(context, event.id)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Calculate next upcoming occurrence for recurring / yearly / birthday events
        val nextOccurrence = DateUtils.getNextOccurrenceTime(
            startTimeEpoch = event.startTime,
            isRecurring = event.isRecurring || event.category == "Birthday",
            recurrenceRule = event.recurrenceRule ?: if (event.category == "Birthday") "YEARLY" else null,
            reminderMinutesBefore = event.reminderMinutesBefore
        )

        val triggerTime = nextOccurrence - (event.reminderMinutesBefore * 60 * 1000L)
        val now = System.currentTimeMillis()

        // If trigger is very close or now, schedule with minimal 1-second delay
        val actualTrigger = if (triggerTime <= now) now + 1500L else triggerTime

        val isBirthday = event.category == "Birthday" ||
                event.recurrenceRule == "BIRTHDAY" ||
                event.title.contains("Birthday", ignoreCase = true)

        val intent = Intent(context, CalendarAlarmReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            putExtra(EXTRA_EVENT_ID, event.id)
            putExtra(EXTRA_EVENT_TITLE, event.title)
            putExtra(EXTRA_EVENT_CATEGORY, event.category)
            putExtra(EXTRA_EVENT_LOCATION, event.location)
            putExtra(EXTRA_EVENT_DESCRIPTION, event.description)
            putExtra(EXTRA_NOTIFICATION_COUNT, 0)
            putExtra(EXTRA_TOTAL_ALLOWED, event.repeatNotificationCount)
            putExtra(EXTRA_GAP_MINUTES, event.repeatGapMinutes)
            putExtra(EXTRA_IS_BIRTHDAY, isBirthday)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, actualTrigger, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, actualTrigger, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, actualTrigger, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, actualTrigger, pendingIntent)
            }
        } catch (e: Exception) {
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, actualTrigger, pendingIntent)
            } catch (e2: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, actualTrigger, pendingIntent)
            }
        }
    }

    fun scheduleRepeatAlarm(
        context: Context,
        eventId: String,
        title: String,
        currentCount: Int,
        totalAllowed: Int,
        gapMinutes: Int,
        isBirthday: Boolean = false,
        category: String? = null,
        location: String? = null
    ) {
        if (isAcknowledged(context, eventId)) return

        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + (gapMinutes * 60 * 1000L)

        val intent = Intent(context, CalendarAlarmReceiver::class.java).apply {
            action = ACTION_EVENT_REMINDER
            putExtra(EXTRA_EVENT_ID, eventId)
            putExtra(EXTRA_EVENT_TITLE, title)
            putExtra(EXTRA_EVENT_CATEGORY, category)
            putExtra(EXTRA_EVENT_LOCATION, location)
            putExtra(EXTRA_NOTIFICATION_COUNT, currentCount + 1)
            putExtra(EXTRA_TOTAL_ALLOWED, totalAllowed)
            putExtra(EXTRA_GAP_MINUTES, gapMinutes)
            putExtra(EXTRA_IS_BIRTHDAY, isBirthday)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (e: Exception) {
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } catch (e2: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
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
        gapMinutes: Int,
        isBirthday: Boolean = false,
        category: String? = null,
        location: String? = null,
        description: String? = null
    ) {
        createNotificationChannel(context)
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

        val channelIdToUse = if (isBirthday) CHANNEL_BIRTHDAY_ID else CHANNEL_ID

        val notifTitle = if (isBirthday) {
            "🎂 Happy Birthday: $title!"
        } else {
            "📅 $title"
        }

        val repeatText = if (totalAllowed > 1) {
            "Alert ${count + 1} of $totalAllowed (repeats every $gapMinutes min)"
        } else {
            "Starting now"
        }

        val bodyText = buildString {
            if (isBirthday) {
                append("🎉 Today is a special birthday celebration! Don't forget to send best wishes & love.")
            } else {
                append(repeatText)
                if (!location.isNullOrBlank()) append(" • 📍 $location")
                if (!description.isNullOrBlank()) append("\n$description")
            }
        }

        val builder = NotificationCompat.Builder(context, channelIdToUse)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notifTitle)
            .setContentText(if (isBirthday) "🎂 Celebrate and wish them a wonderful day!" else repeatText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(if (isBirthday) NotificationCompat.CATEGORY_EVENT else NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(if (isBirthday) longArrayOf(0, 300, 150, 300, 150, 600) else longArrayOf(0, 400, 200, 400))
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Acknowledge",
                ackPendingIntent
            )

        notificationManager.notify(eventId.hashCode(), builder.build())
    }

    fun sendImmediateTestNotification(context: Context) {
        createNotificationChannel(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            999999,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔔 Calendar Notification Test")
            .setContentText("Notifications and alerts are working perfectly on this device!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("✅ Notifications & Repeating Reminders are working properly!\nYou will receive on-time reminders and birthday countdown alerts.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setContentIntent(openAppPendingIntent)
            .build()

        notificationManager.notify(999999, notification)
    }
}

