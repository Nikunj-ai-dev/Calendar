package com.example.data.repository

import android.content.Context
import com.example.data.local.CalendarEntity
import com.example.data.local.EventEntity
import com.example.data.local.NoteEntity
import com.example.data.local.UserEntity
import com.example.data.remote.SupabaseCalendarDto
import com.example.data.remote.SupabaseClient
import com.example.data.remote.SupabaseEventDto
import com.example.data.remote.SupabaseNoteDto
import com.example.data.remote.SupabaseReminderDto
import com.example.notification.NotificationHelper
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

class CalendarRepository(private val context: Context, private val authRepository: AuthRepository) {

    private val _eventsFlow = MutableStateFlow<List<EventEntity>>(emptyList())
    val eventsFlow: StateFlow<List<EventEntity>> = _eventsFlow.asStateFlow()

    private val _notesFlow = MutableStateFlow<List<NoteEntity>>(emptyList())
    val notesFlow: StateFlow<List<NoteEntity>> = _notesFlow.asStateFlow()

    private val _calendarsFlow = MutableStateFlow<List<CalendarEntity>>(emptyList())
    val calendarsFlow: StateFlow<List<CalendarEntity>> = _calendarsFlow.asStateFlow()

    private fun getAuthBearer(): String {
        val token = authRepository.getStoredToken()
        return if (!token.isNullOrBlank()) "Bearer $token" else "Bearer ${SupabaseClient.supabaseAnonKey}"
    }

    suspend fun loadAllFromCloud(userId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        val bearer = getAuthBearer()

        // 1. Fetch Events from Supabase
        try {
            val response = SupabaseClient.restApi.getEvents(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                userIdFilter = "eq.$userId"
            )
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val mapped = dtoList.map { dto ->
                    val startEpoch = DateUtils.parseIso8601(dto.startTime) ?: System.currentTimeMillis()
                    val endEpoch = dto.endTime?.let { DateUtils.parseIso8601(it) }
                    EventEntity(
                        id = dto.id ?: UUID.randomUUID().toString(),
                        userId = dto.userId,
                        calendarId = dto.calendarId,
                        title = dto.title,
                        description = dto.description,
                        location = dto.location,
                        startTime = startEpoch,
                        endTime = endEpoch,
                        allDay = dto.allDay,
                        category = dto.category ?: "Personal",
                        color = dto.color ?: "#386B01",
                        completed = dto.completed,
                        isRecurring = dto.isRecurring,
                        recurrenceRule = dto.recurrenceRule,
                        reminderMinutesBefore = 10,
                        repeatNotificationCount = 3,
                        repeatGapMinutes = 5,
                        isAcknowledged = false,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                _eventsFlow.value = mapped

                // Schedule notifications for all upcoming active events
                mapped.forEach { event ->
                    if (!event.completed && !event.isAcknowledged) {
                        NotificationHelper.scheduleEventReminder(context, event)
                    }
                }
            }
        } catch (e: Exception) {
            // Network logging
        }

        // 2. Fetch Notes from Supabase
        try {
            val response = SupabaseClient.restApi.getNotes(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                userIdFilter = "eq.$userId"
            )
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val mapped = dtoList.map { dto ->
                    NoteEntity(
                        id = dto.id ?: UUID.randomUUID().toString(),
                        userId = dto.userId,
                        title = dto.title,
                        content = dto.content ?: "",
                        noteDate = dto.noteDate,
                        eventId = dto.eventId,
                        pinned = dto.pinned,
                        archived = dto.archived,
                        color = "#F59E0B",
                        updatedAt = System.currentTimeMillis()
                    )
                }
                _notesFlow.value = mapped
            }
        } catch (e: Exception) {
            // Network logging
        }

        // 3. Fetch Calendars from Supabase
        try {
            val response = SupabaseClient.restApi.getCalendars(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                userIdFilter = "eq.$userId"
            )
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                val mapped = dtoList.map { dto ->
                    CalendarEntity(
                        id = dto.id ?: UUID.randomUUID().toString(),
                        userId = dto.userId,
                        name = dto.name,
                        color = dto.color ?: "#386B01",
                        updatedAt = System.currentTimeMillis()
                    )
                }
                _calendarsFlow.value = mapped
            }
        } catch (e: Exception) {
            // Network logging
        }
    }

    suspend fun createOrUpdateEvent(event: EventEntity) = withContext(Dispatchers.IO) {
        val currentList = _eventsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == event.id }
        if (index >= 0) {
            currentList[index] = event
        } else {
            currentList.add(event)
        }
        _eventsFlow.value = currentList

        if (!event.completed && !event.isAcknowledged) {
            NotificationHelper.scheduleEventReminder(context, event)
        } else {
            NotificationHelper.cancelEventReminder(context, event.id)
        }

        // Cloud sync with Supabase
        val bearer = getAuthBearer()
        val startIso = DateUtils.formatIso8601(event.startTime)
        val endIso = event.endTime?.let { DateUtils.formatIso8601(it) }

        val dto = SupabaseEventDto(
            id = event.id,
            userId = event.userId,
            calendarId = event.calendarId,
            title = event.title,
            description = event.description,
            location = event.location,
            startTime = startIso,
            endTime = endIso,
            allDay = event.allDay,
            category = event.category,
            color = event.color,
            completed = event.completed,
            isRecurring = event.isRecurring,
            recurrenceRule = event.recurrenceRule
        )

        try {
            SupabaseClient.restApi.createEvent(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                event = dto
            )

            // Also persist reminder settings in public.event_reminders
            if (event.reminderMinutesBefore > 0) {
                SupabaseClient.restApi.createReminder(
                    apiKey = SupabaseClient.supabaseAnonKey,
                    bearerToken = bearer,
                    reminder = SupabaseReminderDto(
                        eventId = event.id,
                        userId = event.userId,
                        minutesBefore = event.reminderMinutesBefore,
                        browserNotification = true,
                        emailNotification = false
                    )
                )
            }
        } catch (e: Exception) {
            // Log cloud sync exception
        }
    }

    suspend fun acknowledgeEvent(eventId: String) = withContext(Dispatchers.IO) {
        NotificationHelper.cancelEventReminder(context, eventId)

        val currentList = _eventsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == eventId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(isAcknowledged = true)
            _eventsFlow.value = currentList
        }
    }

    suspend fun setEventCompleted(eventId: String, completed: Boolean) = withContext(Dispatchers.IO) {
        val currentList = _eventsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == eventId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(completed = completed)
            _eventsFlow.value = currentList
        }

        if (completed) {
            NotificationHelper.cancelEventReminder(context, eventId)
        }

        val bearer = getAuthBearer()
        try {
            SupabaseClient.restApi.updateEvent(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                idFilter = "eq.$eventId",
                event = mapOf("completed" to completed)
            )
        } catch (e: Exception) {
            // Cloud sync
        }
    }

    suspend fun deleteEvent(eventId: String) = withContext(Dispatchers.IO) {
        NotificationHelper.cancelEventReminder(context, eventId)

        val currentList = _eventsFlow.value.toMutableList()
        currentList.removeAll { it.id == eventId }
        _eventsFlow.value = currentList

        val bearer = getAuthBearer()
        try {
            SupabaseClient.restApi.deleteEvent(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                idFilter = "eq.$eventId"
            )
            SupabaseClient.restApi.deleteReminder(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                eventIdFilter = "eq.$eventId"
            )
        } catch (e: Exception) {
            // Cloud sync
        }
    }

    suspend fun rescheduleEvent(eventId: String, newStartTime: Long, newEndTime: Long?) = withContext(Dispatchers.IO) {
        val current = _eventsFlow.value.find { it.id == eventId } ?: return@withContext
        val updated = current.copy(
            startTime = newStartTime,
            endTime = newEndTime,
            isAcknowledged = false,
            notificationsSentCount = 0,
            updatedAt = System.currentTimeMillis()
        )
        createOrUpdateEvent(updated)
    }

    suspend fun createOrUpdateNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        val currentList = _notesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            currentList[index] = note
        } else {
            currentList.add(0, note)
        }
        _notesFlow.value = currentList

        val bearer = getAuthBearer()
        val dto = SupabaseNoteDto(
            id = note.id,
            userId = note.userId,
            title = note.title,
            content = note.content,
            noteDate = note.noteDate,
            eventId = note.eventId,
            pinned = note.pinned,
            archived = note.archived
        )

        try {
            SupabaseClient.restApi.createNote(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                note = dto
            )
        } catch (e: Exception) {
            // Cloud sync
        }
    }

    suspend fun setNotePinned(noteId: String, pinned: Boolean) = withContext(Dispatchers.IO) {
        val currentList = _notesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == noteId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(pinned = pinned)
            _notesFlow.value = currentList
        }

        val bearer = getAuthBearer()
        try {
            SupabaseClient.restApi.updateNote(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                idFilter = "eq.$noteId",
                note = mapOf("pinned" to pinned)
            )
        } catch (e: Exception) {
            // Cloud sync
        }
    }

    suspend fun setNoteArchived(noteId: String, archived: Boolean) = withContext(Dispatchers.IO) {
        val currentList = _notesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == noteId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(archived = archived)
            _notesFlow.value = currentList
        }

        val bearer = getAuthBearer()
        try {
            SupabaseClient.restApi.updateNote(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                idFilter = "eq.$noteId",
                note = mapOf("archived" to archived)
            )
        } catch (e: Exception) {
            // Cloud sync
        }
    }

    suspend fun deleteNote(noteId: String) = withContext(Dispatchers.IO) {
        val currentList = _notesFlow.value.toMutableList()
        currentList.removeAll { it.id == noteId }
        _notesFlow.value = currentList

        val bearer = getAuthBearer()
        try {
            SupabaseClient.restApi.deleteNote(
                apiKey = SupabaseClient.supabaseAnonKey,
                bearerToken = bearer,
                idFilter = "eq.$noteId"
            )
        } catch (e: Exception) {
            // Cloud sync
        }
    }

    suspend fun syncBirthdayEvent(user: UserEntity) = withContext(Dispatchers.IO) {
        val birthDateStr = user.birthDate ?: return@withContext
        try {
            val parts = birthDateStr.split("-")
            if (parts.size == 3) {
                val month = parts[1].toInt() - 1
                val day = parts[2].toInt()

                val nowCal = Calendar.getInstance()
                val currentYear = nowCal.get(Calendar.YEAR)

                val bdayCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val title = if (user.name.isNotBlank()) "🎂 ${user.name}'s Birthday" else "🎂 Your Birthday"
                val bdayEventId = "bday_${user.id.take(8)}"

                val existing = _eventsFlow.value.find { it.id == bdayEventId || (it.category == "Birthday" && it.isRecurring) }
                if (existing == null) {
                    val bdayEvent = EventEntity(
                        id = bdayEventId,
                        userId = user.id,
                        title = title,
                        description = "Special celebration day! Remember to celebrate and rejoice with loved ones.",
                        location = "Celebration",
                        startTime = bdayCal.timeInMillis,
                        endTime = bdayCal.timeInMillis + (12 * 3600 * 1000L),
                        allDay = true,
                        category = "Birthday",
                        color = "#EC4899",
                        isRecurring = true,
                        recurrenceRule = "YEARLY",
                        reminderMinutesBefore = 60,
                        repeatNotificationCount = 3,
                        repeatGapMinutes = 15,
                        isAcknowledged = false
                    )

                    createOrUpdateEvent(bdayEvent)
                }
            }
        } catch (ignored: Exception) {}
    }
}

