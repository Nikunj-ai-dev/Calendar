package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CalendarAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        NotificationHelper.createNotificationChannel(context)
        val action = intent.action ?: return

        when (action) {
            NotificationHelper.ACTION_ACKNOWLEDGE -> {
                val eventId = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_ID) ?: return
                NotificationHelper.cancelEventReminder(context, eventId)
            }

            NotificationHelper.ACTION_EVENT_REMINDER -> {
                val eventId = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_ID) ?: return
                val title = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_TITLE) ?: "Event"
                val category = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_CATEGORY)
                val location = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_LOCATION)
                val description = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_DESCRIPTION)
                val currentCount = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_COUNT, 0)
                val totalAllowed = intent.getIntExtra(NotificationHelper.EXTRA_TOTAL_ALLOWED, 3)
                val gapMinutes = intent.getIntExtra(NotificationHelper.EXTRA_GAP_MINUTES, 5)
                val isBirthday = intent.getBooleanExtra(NotificationHelper.EXTRA_IS_BIRTHDAY, false)

                if (!NotificationHelper.isAcknowledged(context, eventId)) {
                    NotificationHelper.showEventNotification(
                        context = context,
                        eventId = eventId,
                        title = title,
                        count = currentCount,
                        totalAllowed = totalAllowed,
                        gapMinutes = gapMinutes,
                        isBirthday = isBirthday,
                        category = category,
                        location = location,
                        description = description
                    )

                    val nextCount = currentCount + 1
                    // If not acknowledged and we haven't reached the maximum repeat notifications limit
                    if (nextCount < totalAllowed) {
                        NotificationHelper.scheduleRepeatAlarm(
                            context = context,
                            eventId = eventId,
                            title = title,
                            currentCount = nextCount,
                            totalAllowed = totalAllowed,
                            gapMinutes = gapMinutes,
                            isBirthday = isBirthday,
                            category = category,
                            location = location
                        )
                    }
                }
            }

            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                // Device rebooted or time changed; notification channel ensured
                NotificationHelper.createNotificationChannel(context)
            }
        }
    }
}


