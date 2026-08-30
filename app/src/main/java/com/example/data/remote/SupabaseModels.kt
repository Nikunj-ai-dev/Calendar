package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseAuthRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String? = null,
    @Json(name = "data") val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String? = null,
    @Json(name = "token_type") val tokenType: String? = null,
    @Json(name = "expires_in") val expiresIn: Long? = null,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "user") val user: SupabaseUserDto? = null,
    @Json(name = "error_description") val errorDescription: String? = null,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseUserDto(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String? = null,
    @Json(name = "user_metadata") val userMetadata: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseProfileDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "timezone") val timezone: String? = "Asia/Kolkata",
    @Json(name = "email") val email: String? = null,
    @Json(name = "birth_data") val birthData: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseCalendarDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "user_id") val userId: String,
    @Json(name = "name") val name: String = "My Calendar",
    @Json(name = "color") val color: String? = "#386B01",
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "metadata") val metadata: Map<String, Any?>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseEventDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "user_id") val userId: String,
    @Json(name = "calendar_id") val calendarId: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "location") val location: String? = null,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String? = null,
    @Json(name = "all_day") val allDay: Boolean = false,
    @Json(name = "category") val category: String? = "Personal",
    @Json(name = "color") val color: String? = "#386B01",
    @Json(name = "completed") val completed: Boolean = false,
    @Json(name = "is_recurring") val isRecurring: Boolean = false,
    @Json(name = "recurrence_rule") val recurrenceRule: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseReminderDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "event_id") val eventId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "minutes_before") val minutesBefore: Int = 10,
    @Json(name = "browser_notification") val browserNotification: Boolean = true,
    @Json(name = "email_notification") val emailNotification: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseNoteDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "user_id") val userId: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String? = null,
    @Json(name = "note_date") val noteDate: String? = null,
    @Json(name = "event_id") val eventId: String? = null,
    @Json(name = "pinned") val pinned: Boolean = false,
    @Json(name = "archived") val archived: Boolean = false,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

