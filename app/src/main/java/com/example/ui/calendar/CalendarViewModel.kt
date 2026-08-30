package com.example.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.EventEntity
import com.example.data.local.NoteEntity
import com.example.data.local.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.CalendarRepository
import com.example.notification.NotificationHelper
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class CalendarViewMode {
    MONTH, WEEK, DAY, AGENDA
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = AuthRepository(application)
    private val calendarRepo = CalendarRepository(application, authRepo)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // User / Auth State
    val currentUser: StateFlow<UserEntity?> = authRepo.currentUserFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Calendar Navigation State
    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()

    // Filter & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null) // null = All
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _filterOnlyUncompleted = MutableStateFlow(false)
    val filterOnlyUncompleted: StateFlow<Boolean> = _filterOnlyUncompleted.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // UI Dialog States
    private val _showEventDialog = MutableStateFlow(false)
    val showEventDialog: StateFlow<Boolean> = _showEventDialog.asStateFlow()

    private val _editingEvent = MutableStateFlow<EventEntity?>(null)
    val editingEvent: StateFlow<EventEntity?> = _editingEvent.asStateFlow()

    private val _showQuickAddDialog = MutableStateFlow(false)
    val showQuickAddDialog: StateFlow<Boolean> = _showQuickAddDialog.asStateFlow()

    private val _showNoteDialog = MutableStateFlow(false)
    val showNoteDialog: StateFlow<Boolean> = _showNoteDialog.asStateFlow()

    private val _editingNote = MutableStateFlow<NoteEntity?>(null)
    val editingNote: StateFlow<NoteEntity?> = _editingNote.asStateFlow()

    private val _reschedulingEvent = MutableStateFlow<EventEntity?>(null)
    val reschedulingEvent: StateFlow<EventEntity?> = _reschedulingEvent.asStateFlow()

    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    private val _showBirthDatePicker = MutableStateFlow(false)
    val showBirthDatePicker: StateFlow<Boolean> = _showBirthDatePicker.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    private val _showShortcutsDialog = MutableStateFlow(false)
    val showShortcutsDialog: StateFlow<Boolean> = _showShortcutsDialog.asStateFlow()

    // All Events flow for user
    val allEvents: StateFlow<List<EventEntity>> = calendarRepo.eventsFlow

    // All Notes flow for user
    val allNotes: StateFlow<List<NoteEntity>> = calendarRepo.notesFlow

    // All Calendars flow for user
    val allCalendars: StateFlow<List<com.example.data.local.CalendarEntity>> = calendarRepo.calendarsFlow

    // Filtered Events
    val filteredEvents: StateFlow<List<EventEntity>> = combine(
        allEvents,
        _searchQuery,
        _selectedCategory,
        _filterOnlyUncompleted
    ) { events, query, category, uncompletedOnly ->
        events.filter { event ->
            val matchesQuery = query.isBlank() ||
                    event.title.contains(query, ignoreCase = true) ||
                    (event.description?.contains(query, ignoreCase = true) == true) ||
                    (event.location?.contains(query, ignoreCase = true) == true) ||
                    (event.category?.contains(query, ignoreCase = true) == true)

            val matchesCategory = category == null || event.category.equals(category, ignoreCase = true)
            val matchesUncompleted = !uncompletedOnly || !event.completed

            matchesQuery && matchesCategory && matchesUncompleted
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Notes
    val filteredNotes: StateFlow<List<NoteEntity>> = combine(
        allNotes,
        _searchQuery
    ) { notes, query ->
        if (query.isBlank()) {
            notes
        } else {
            notes.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's events for Swipable Cards Carousel
    val todayEvents: StateFlow<List<EventEntity>> = allEvents.combine(_selectedDate) { events, _ ->
        val today = Calendar.getInstance()
        events.filter { DateUtils.isSameDay(it.startTime, today.timeInMillis) }
              .sortedBy { it.startTime }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        NotificationHelper.createNotificationChannel(application)
        viewModelScope.launch {
            val user = authRepo.restoreSession()
            if (user != null) {
                calendarRepo.loadAllFromCloud(user.id)
                calendarRepo.syncBirthdayEvent(user)
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun refreshCloudData() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            calendarRepo.loadAllFromCloud(user.id)
        }
    }

    // --- Authentication & Profile ---
    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authRepo.signIn(email, pass)
            _isAuthLoading.value = false
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                calendarRepo.loadAllFromCloud(user.id)
                calendarRepo.syncBirthdayEvent(user)
                _snackbarMessage.value = "Welcome back, ${user.name}!"
            } else {
                _authErrorMessage.value = result.exceptionOrNull()?.message ?: "Sign in failed"
            }
        }
    }

    fun signUp(email: String, pass: String, name: String, birthDate: String?) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authRepo.signUp(email, pass, name, birthDate)
            _isAuthLoading.value = false
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                calendarRepo.loadAllFromCloud(user.id)
                calendarRepo.syncBirthdayEvent(user)
                _snackbarMessage.value = "Account created! Birthday marked on calendar."
            } else {
                _authErrorMessage.value = result.exceptionOrNull()?.message ?: "Sign up failed"
            }
        }
    }

    fun updateProfile(name: String, birthDate: String?, timezone: String) {
        viewModelScope.launch {
            val result = authRepo.updateProfile(name, birthDate, timezone)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                calendarRepo.syncBirthdayEvent(user)
                _showProfileDialog.value = false
                _snackbarMessage.value = "Profile updated successfully"
            }
        }
    }

    fun setBirthDateAndMarkCalendar(birthDate: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val result = authRepo.updateProfile(user.name, birthDate, user.timezone)
            if (result.isSuccess) {
                val updatedUser = result.getOrNull()!!
                calendarRepo.syncBirthdayEvent(updatedUser)
                _showBirthDatePicker.value = false
                _snackbarMessage.value = "🎂 Birthday marked on calendar!"
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            val res = authRepo.resetPassword(email)
            _snackbarMessage.value = res.getOrDefault("Password reset email sent")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepo.signOut()
            _snackbarMessage.value = "Signed out"
        }
    }

    // --- Calendar Navigation ---
    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun jumpToToday() {
        _selectedDate.value = Calendar.getInstance()
    }

    fun selectDate(cal: Calendar) {
        _selectedDate.value = cal.clone() as Calendar
    }

    fun navigatePrevious() {
        val current = _selectedDate.value.clone() as Calendar
        when (_viewMode.value) {
            CalendarViewMode.MONTH -> current.add(Calendar.MONTH, -1)
            CalendarViewMode.WEEK -> current.add(Calendar.WEEK_OF_YEAR, -1)
            CalendarViewMode.DAY -> current.add(Calendar.DAY_OF_YEAR, -1)
            CalendarViewMode.AGENDA -> current.add(Calendar.MONTH, -1)
        }
        _selectedDate.value = current
    }

    fun navigateNext() {
        val current = _selectedDate.value.clone() as Calendar
        when (_viewMode.value) {
            CalendarViewMode.MONTH -> current.add(Calendar.MONTH, 1)
            CalendarViewMode.WEEK -> current.add(Calendar.WEEK_OF_YEAR, 1)
            CalendarViewMode.DAY -> current.add(Calendar.DAY_OF_YEAR, 1)
            CalendarViewMode.AGENDA -> current.add(Calendar.MONTH, 1)
        }
        _selectedDate.value = current
    }

    // --- Search & Filters ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleFilterOnlyUncompleted() {
        _filterOnlyUncompleted.value = !_filterOnlyUncompleted.value
    }

    // --- Dialog Controls ---
    fun openAddEventDialog(prefilledStart: Long? = null) {
        val defaultStart = prefilledStart ?: run {
            val c = _selectedDate.value.clone() as Calendar
            c.set(Calendar.HOUR_OF_DAY, 10)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.timeInMillis
        }
        _editingEvent.value = EventEntity(
            userId = currentUser.value?.id ?: "",
            title = "",
            startTime = defaultStart,
            endTime = defaultStart + (60 * 60 * 1000L),
            category = "Work",
            color = "#3B82F6",
            reminderMinutesBefore = 10,
            repeatNotificationCount = 3,
            repeatGapMinutes = 5
        )
        _showEventDialog.value = true
    }

    fun openEditEventDialog(event: EventEntity) {
        _editingEvent.value = event
        _showEventDialog.value = true
    }

    fun closeEventDialog() {
        _showEventDialog.value = false
        _editingEvent.value = null
    }

    fun openQuickAddDialog() {
        _showQuickAddDialog.value = true
    }

    fun closeQuickAddDialog() {
        _showQuickAddDialog.value = false
    }

    fun openAddNoteDialog(dateStr: String? = null) {
        val defaultDate = dateStr ?: dateFormat.format(_selectedDate.value.time)
        _editingNote.value = NoteEntity(
            userId = currentUser.value?.id ?: "",
            title = "",
            content = "",
            noteDate = defaultDate,
            color = "#3B82F6"
        )
        _showNoteDialog.value = true
    }

    fun openEditNoteDialog(note: NoteEntity) {
        _editingNote.value = note
        _showNoteDialog.value = true
    }

    fun closeNoteDialog() {
        _showNoteDialog.value = false
        _editingNote.value = null
    }

    fun openRescheduleDialog(event: EventEntity) {
        _reschedulingEvent.value = event
    }

    fun closeRescheduleDialog() {
        _reschedulingEvent.value = null
    }

    fun openProfileDialog() {
        _showProfileDialog.value = true
    }

    fun closeProfileDialog() {
        _showProfileDialog.value = false
    }

    fun openBirthDatePicker() {
        _showBirthDatePicker.value = true
    }

    fun closeBirthDatePicker() {
        _showBirthDatePicker.value = false
    }

    fun openFilterSheet() {
        _showFilterSheet.value = true
    }

    fun closeFilterSheet() {
        _showFilterSheet.value = false
    }

    fun toggleShortcutsDialog(show: Boolean) {
        _showShortcutsDialog.value = show
    }

    // --- Event Actions ---
    fun saveEvent(event: EventEntity) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val finalizedEvent = if (event.userId.isBlank()) event.copy(userId = user.id) else event
            calendarRepo.createOrUpdateEvent(finalizedEvent)
            closeEventDialog()
            _snackbarMessage.value = "Event '${finalizedEvent.title}' saved"
        }
    }

    fun quickAddEvent(title: String, startTimeEpoch: Long, category: String = "Personal", color: String = "#10B981") {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val newEvent = EventEntity(
                userId = user.id,
                title = title,
                startTime = startTimeEpoch,
                endTime = startTimeEpoch + (60 * 60 * 1000L),
                category = category,
                color = color,
                reminderMinutesBefore = 10,
                repeatNotificationCount = 3,
                repeatGapMinutes = 5
            )
            calendarRepo.createOrUpdateEvent(newEvent)
            closeQuickAddDialog()
            _snackbarMessage.value = "Quick event added: '$title'"
        }
    }

    fun acknowledgeEvent(eventId: String) {
        viewModelScope.launch {
            calendarRepo.acknowledgeEvent(eventId)
            _snackbarMessage.value = "Event acknowledged. Repeat alerts stopped."
        }
    }

    fun toggleEventCompleted(eventId: String, completed: Boolean) {
        viewModelScope.launch {
            calendarRepo.setEventCompleted(eventId, completed)
            if (completed) {
                _snackbarMessage.value = "Event marked complete! 🎉"
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            calendarRepo.deleteEvent(eventId)
            closeEventDialog()
            _snackbarMessage.value = "Event deleted"
        }
    }

    fun rescheduleEvent(eventId: String, newStart: Long, newEnd: Long?) {
        viewModelScope.launch {
            calendarRepo.rescheduleEvent(eventId, newStart, newEnd)
            closeRescheduleDialog()
            _snackbarMessage.value = "Event rescheduled"
        }
    }

    // --- Note Actions ---
    fun saveNote(note: NoteEntity) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val finalizedNote = if (note.userId.isBlank()) note.copy(userId = user.id) else note
            calendarRepo.createOrUpdateNote(finalizedNote)
            closeNoteDialog()
            _snackbarMessage.value = "Note saved"
        }
    }

    fun toggleNotePinned(noteId: String, pinned: Boolean) {
        viewModelScope.launch {
            calendarRepo.setNotePinned(noteId, pinned)
        }
    }

    fun toggleNoteArchived(noteId: String, archived: Boolean) {
        viewModelScope.launch {
            calendarRepo.setNoteArchived(noteId, archived)
            _snackbarMessage.value = if (archived) "Note archived" else "Note unarchived"
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            calendarRepo.deleteNote(noteId)
            closeNoteDialog()
            _snackbarMessage.value = "Note deleted"
        }
    }
}
