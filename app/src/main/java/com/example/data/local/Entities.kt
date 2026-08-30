package com.example.data.local

import java.util.UUID

data class UserEntity(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val name: String = "",
    val avatarUrl: String = "",
    val timezone: String = "Asia/Kolkata",
    val birthDate: String? = null, // "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CalendarEntity(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String = "My Calendar",
    val color: String = "#386B01",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class EventEntity(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val calendarId: String? = null,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val startTime: Long, // epoch millis
    val endTime: Long? = null, // epoch millis
    val allDay: Boolean = false,
    val category: String? = "Personal",
    val color: String = "#386B01",
    val completed: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY", "BIRTHDAY"
    // Repeating reminder alert settings
    val reminderMinutesBefore: Int = 10,
    val repeatNotificationCount: Int = 3, // e.g. send up to 3 times
    val repeatGapMinutes: Int = 5, // gap in minutes between repeated alarms
    val isAcknowledged: Boolean = false, // Stops repeating notifications when acknowledged
    val notificationsSentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class EventReminderEntity(
    val id: String = UUID.randomUUID().toString(),
    val eventId: String,
    val userId: String,
    val minutesBefore: Int = 10,
    val browserNotification: Boolean = true,
    val emailNotification: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class NoteEntity(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val content: String = "",
    val noteDate: String? = null, // "YYYY-MM-DD"
    val eventId: String? = null,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val color: String = "#F59E0B",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
