package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CalendarAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val action = intent.action ?: return

        when (action) {
            NotificationHelper.ACTION_ACKNOWLEDGE -> {
                val eventId = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_ID) ?: return
                NotificationHelper.cancelEventReminder(context, eventId)
            }

            NotificationHelper.ACTION_EVENT_REMINDER -> {
                val eventId = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_ID) ?: return
                val title = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_TITLE) ?: "Event"
                val currentCount = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_COUNT, 0)
                val totalAllowed = intent.getIntExtra(NotificationHelper.EXTRA_TOTAL_ALLOWED, 3)
                val gapMinutes = intent.getIntExtra(NotificationHelper.EXTRA_GAP_MINUTES, 5)

                if (!NotificationHelper.isAcknowledged(context, eventId)) {
                    NotificationHelper.showEventNotification(
                        context = context,
                        eventId = eventId,
                        title = title,
                        count = currentCount,
                        totalAllowed = totalAllowed,
                        gapMinutes = gapMinutes
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
                            gapMinutes = gapMinutes
                        )
                    }
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // System rebooted
            }
        }
    }
}

