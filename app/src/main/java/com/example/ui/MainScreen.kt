package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.auth.AuthScreen
import com.example.ui.calendar.AgendaView
import com.example.ui.calendar.CalendarViewMode
import com.example.ui.calendar.CalendarViewModel
import com.example.ui.calendar.DayView
import com.example.ui.calendar.EventDialog
import com.example.ui.calendar.MonthView
import com.example.ui.calendar.QuickAddEventDialog
import com.example.ui.calendar.QuickRescheduleDialog
import com.example.ui.calendar.TodayCardsCarousel
import com.example.ui.calendar.WeekView
import com.example.ui.components.BirthdayBanner
import com.example.ui.components.CategoryChip
import com.example.ui.components.FilterSheet
import com.example.ui.components.ShortcutsDialog
import com.example.ui.notes.NoteDialog
import com.example.ui.notes.NotesScreen
import com.example.ui.profile.ProfileDialog
import com.example.ui.theme.CalendarCategories
import com.example.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authErrorMessage.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    val selectedDate by viewModel.selectedDate.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val filteredEvents by viewModel.filteredEvents.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val filteredNotes by viewModel.filteredNotes.collectAsState()
    val todayEvents by viewModel.todayEvents.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val onlyUncompleted by viewModel.filterOnlyUncompleted.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    // Dialog state collectors
    val showEventDialog by viewModel.showEventDialog.collectAsState()
    val editingEvent by viewModel.editingEvent.collectAsState()
    val showQuickAddDialog by viewModel.showQuickAddDialog.collectAsState()
    val showNoteDialog by viewModel.showNoteDialog.collectAsState()
    val editingNote by viewModel.editingNote.collectAsState()
    val reschedulingEvent by viewModel.reschedulingEvent.collectAsState()
    val showProfileDialog by viewModel.showProfileDialog.collectAsState()
    val showFilterSheet by viewModel.showFilterSheet.collectAsState()
    val showShortcutsDialog by viewModel.showShortcutsDialog.collectAsState()

    var showSearchRow by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf("CALENDAR") } // CALENDAR or NOTES

    val birthdayCountdown = remember(currentUser?.birthDate) {
        DateUtils.getBirthdayCountdownText(currentUser?.birthDate)
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearSnackbar()
        }
    }

    // If not authenticated, show Auth Screen
    if (currentUser == null) {
        AuthScreen(
            isLoading = isAuthLoading,
            errorMessage = authError,
            onSignIn = { email, pass -> viewModel.signIn(email, pass) },
            onSignUp = { email, pass, name, bday -> viewModel.signUp(email, pass, name, bday) },
            onResetPassword = { email -> viewModel.resetPassword(email) },
            onQuickStart = { email, name, bday -> viewModel.signUp(email, "password123", name, bday) }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Profile Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                viewModel.openProfileDialog()
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.name?.take(1)?.uppercase() ?: "U",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = currentUser?.name ?: "User",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentUser?.email ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (currentUser?.birthDate != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFCE7F3)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🎂", fontSize = 14.sp)
                                Text(
                                    text = "Birthday: ${currentUser?.birthDate}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9D174D)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = "Views",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.CalendarViewMonth, contentDescription = null) },
                        label = { Text("Month View") },
                        selected = currentTab == "CALENDAR" && viewMode == CalendarViewMode.MONTH,
                        onClick = {
                            currentTab = "CALENDAR"
                            viewModel.setViewMode(CalendarViewMode.MONTH)
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.CalendarViewWeek, contentDescription = null) },
                        label = { Text("Week View") },
                        selected = currentTab == "CALENDAR" && viewMode == CalendarViewMode.WEEK,
                        onClick = {
                            currentTab = "CALENDAR"
                            viewModel.setViewMode(CalendarViewMode.WEEK)
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.CalendarViewDay, contentDescription = null) },
                        label = { Text("Day View") },
                        selected = currentTab == "CALENDAR" && viewMode == CalendarViewMode.DAY,
                        onClick = {
                            currentTab = "CALENDAR"
                            viewModel.setViewMode(CalendarViewMode.DAY)
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.ViewAgenda, contentDescription = null) },
                        label = { Text("Agenda View") },
                        selected = currentTab == "CALENDAR" && viewMode == CalendarViewMode.AGENDA,
                        onClick = {
                            currentTab = "CALENDAR"
                            viewModel.setViewMode(CalendarViewMode.AGENDA)
                            scope.launch { drawerState.close() }
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Description, contentDescription = null) },
                        label = { Text("Notes & Docs (${allNotes.size})") },
                        selected = currentTab == "NOTES",
                        onClick = {
                            currentTab = "NOTES"
                            scope.launch { drawerState.close() }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Categories shortcuts
                    Text(
                        text = "Categories",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    CalendarCategories.ALL.take(5).forEach { cat ->
                        NavigationDrawerItem(
                            icon = { Text(cat.emoji) },
                            label = { Text(cat.name) },
                            selected = selectedCategory == cat.name,
                            onClick = {
                                viewModel.setSelectedCategory(if (selectedCategory == cat.name) null else cat.name)
                                currentTab = "CALENDAR"
                                scope.launch { drawerState.close() }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode, contentDescription = null) },
                        label = { Text(if (isDarkMode) "Light Mode" else "Dark Mode") },
                        selected = false,
                        onClick = { viewModel.toggleDarkMode() }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Keyboard, contentDescription = null) },
                        label = { Text("Shortcuts") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.toggleShortcutsDialog(true)
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val cal = selectedDate.clone() as Calendar
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            cal.set(Calendar.YEAR, y)
                                            cal.set(Calendar.MONTH, m)
                                            cal.set(Calendar.DAY_OF_MONTH, d)
                                            viewModel.selectDate(cal)
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("topbar_date_title")
                        ) {
                            Text(
                                text = if (currentTab == "NOTES") "My Notes" else DateUtils.monthYearFormatter.format(selectedDate.time),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("nav_drawer_button")
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // Previous / Next month/week/day navigation
                        if (currentTab == "CALENDAR") {
                            IconButton(
                                onClick = { viewModel.navigatePrevious() },
                                modifier = Modifier.size(34.dp).testTag("nav_prev_btn")
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(18.dp))
                            }

                            FilledTonalButton(
                                onClick = { viewModel.jumpToToday() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp).testTag("jump_today_btn")
                            ) {
                                Text("Today", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { viewModel.navigateNext() },
                                modifier = Modifier.size(34.dp).testTag("nav_next_btn")
                            ) {
                                Icon(Icons.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(18.dp))
                            }
                        }

                        IconButton(
                            onClick = { viewModel.refreshCloudData() },
                            modifier = Modifier.testTag("refresh_cloud_btn")
                        ) {
                            Icon(Icons.Filled.Sync, contentDescription = "Sync with Cloud")
                        }

                        IconButton(
                            onClick = { showSearchRow = !showSearchRow },
                            modifier = Modifier.testTag("toggle_search_btn")
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }

                        IconButton(
                            onClick = { viewModel.openFilterSheet() },
                            modifier = Modifier.testTag("open_filter_btn")
                        ) {
                            if (selectedCategory != null || onlyUncompleted) {
                                BadgedBox(badge = { Badge() }) {
                                    Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                                }
                            } else {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                            }
                        }

                        IconButton(
                            onClick = { viewModel.openProfileDialog() },
                            modifier = Modifier.testTag("profile_avatar_btn")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.name?.take(1)?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == "CALENDAR" && viewMode == CalendarViewMode.MONTH,
                        onClick = {
                            currentTab = "CALENDAR"
                            viewModel.setViewMode(CalendarViewMode.MONTH)
                        },
                        icon = { Icon(Icons.Filled.CalendarViewMonth, contentDescription = null) },
                        label = { Text("Month", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_month")
                    )

                    NavigationBarItem(
                        selected = currentTab == "CALENDAR" && viewMode == CalendarViewMode.WEEK,
                        onClick = {
                            currentTab = "CALENDAR"
                            viewModel.setViewMode(CalendarViewMode.WEEK)
                        },
                        icon = { Icon(Icons.Filled.CalendarViewWeek, contentDescription = null) },
                        label = { Text("Week", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_week")
                    )

                    NavigationBarItem(
                        selected = currentTab == "CALENDAR" && viewMode == CalendarViewMode.DAY,
                        onClick = {
                            currentTab = "CALENDAR"
                            viewModel.setViewMode(CalendarViewMode.DAY)
                        },
                        icon = { Icon(Icons.Filled.CalendarViewDay, contentDescription = null) },
                        label = { Text("Day", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_day")
                    )

                    NavigationBarItem(
                        selected = currentTab == "CALENDAR" && viewMode == CalendarViewMode.AGENDA,
                        onClick = {
                            currentTab = "CALENDAR"
                            viewModel.setViewMode(CalendarViewMode.AGENDA)
                        },
                        icon = { Icon(Icons.Filled.ViewAgenda, contentDescription = null) },
                        label = { Text("Agenda", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_agenda")
                    )

                    NavigationBarItem(
                        selected = currentTab == "NOTES",
                        onClick = { currentTab = "NOTES" },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (allNotes.isNotEmpty()) {
                                        Badge { Text("${allNotes.size}") }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Description, contentDescription = null)
                            }
                        },
                        label = { Text("Notes", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_item_notes")
                    )
                }
            },
            floatingActionButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick add lightning button
                    FilledIconButton(
                        onClick = { viewModel.openQuickAddDialog() },
                        modifier = Modifier.size(44.dp).testTag("quick_add_fab"),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.FlashOn, contentDescription = "Quick Add", tint = Color(0xFFF59E0B))
                    }

                    // Main FAB
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (currentTab == "NOTES") {
                                viewModel.openAddNoteDialog()
                            } else {
                                viewModel.openAddEventDialog()
                            }
                        },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text(if (currentTab == "NOTES") "New Note" else "New Event", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.testTag("main_add_fab")
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search Input Dropdown
                AnimatedVisibility(visible = showSearchRow) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search events, locations, descriptions...") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("global_search_input")
                    )
                }

                // Active Category Filter Bar
                if (selectedCategory != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Filtering by:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            CategoryChip(categoryName = selectedCategory, selected = true)
                        }
                        Text(
                            text = "Clear filter",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { viewModel.setSelectedCategory(null) }
                                .padding(4.dp)
                        )
                    }
                }

                // Main Content Body according to Tab and ViewMode
                Box(modifier = Modifier.weight(1f)) {
                    if (currentTab == "NOTES") {
                        NotesScreen(
                            notes = filteredNotes,
                            onNoteClick = { viewModel.openEditNoteDialog(it) },
                            onTogglePin = { id, pin -> viewModel.toggleNotePinned(id, pin) },
                            onToggleArchive = { id, arc -> viewModel.toggleNoteArchived(id, arc) },
                            onAddNote = { viewModel.openAddNoteDialog() }
                        )
                    } else {
                        when (viewMode) {
                            CalendarViewMode.MONTH -> {
                                MonthView(
                                    selectedDate = selectedDate,
                                    events = filteredEvents,
                                    notes = filteredNotes,
                                    todayEvents = todayEvents,
                                    birthdayCountdown = birthdayCountdown,
                                    onDateSelected = { viewModel.selectDate(it) },
                                    onEventClick = { viewModel.openEditEventDialog(it) },
                                    onAcknowledgeEvent = { viewModel.acknowledgeEvent(it) },
                                    onToggleComplete = { id, comp -> viewModel.toggleEventCompleted(id, comp) },
                                    onRescheduleEvent = { viewModel.openRescheduleDialog(it) },
                                    onNoteClick = { viewModel.openEditNoteDialog(it) },
                                    onAddEvent = { viewModel.openAddEventDialog(it) },
                                    onAddNote = { viewModel.openAddNoteDialog(it) }
                                )
                            }
                            CalendarViewMode.WEEK -> {
                                WeekView(
                                    selectedDate = selectedDate,
                                    events = filteredEvents,
                                    onDateSelected = { viewModel.selectDate(it) },
                                    onEventClick = { viewModel.openEditEventDialog(it) },
                                    onAddEventAtTime = { viewModel.openAddEventDialog(it) }
                                )
                            }
                            CalendarViewMode.DAY -> {
                                DayView(
                                    selectedDate = selectedDate,
                                    events = filteredEvents,
                                    notes = filteredNotes,
                                    onEventClick = { viewModel.openEditEventDialog(it) },
                                    onAcknowledgeEvent = { viewModel.acknowledgeEvent(it) },
                                    onToggleComplete = { id, comp -> viewModel.toggleEventCompleted(id, comp) },
                                    onNoteClick = { viewModel.openEditNoteDialog(it) },
                                    onAddEvent = { viewModel.openAddEventDialog(it) },
                                    onAddNote = { viewModel.openAddNoteDialog(it) }
                                )
                            }
                            CalendarViewMode.AGENDA -> {
                                AgendaView(
                                    events = filteredEvents,
                                    onEventClick = { viewModel.openEditEventDialog(it) },
                                    onAcknowledgeEvent = { viewModel.acknowledgeEvent(it) },
                                    onToggleComplete = { id, comp -> viewModel.toggleEventCompleted(id, comp) },
                                    onRescheduleEvent = { viewModel.openRescheduleDialog(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showEventDialog && editingEvent != null) {
        EventDialog(
            event = editingEvent!!,
            onDismiss = { viewModel.closeEventDialog() },
            onSave = { viewModel.saveEvent(it) },
            onDelete = { viewModel.deleteEvent(it) }
        )
    }

    if (showQuickAddDialog) {
        QuickAddEventDialog(
            initialDate = selectedDate,
            onDismiss = { viewModel.closeQuickAddDialog() },
            onQuickAdd = { title, startEpoch, cat, col ->
                viewModel.quickAddEvent(title, startEpoch, cat, col)
            }
        )
    }

    if (showNoteDialog && editingNote != null) {
        NoteDialog(
            note = editingNote!!,
            onDismiss = { viewModel.closeNoteDialog() },
            onSave = { viewModel.saveNote(it) },
            onDelete = { viewModel.deleteNote(it) }
        )
    }

    if (reschedulingEvent != null) {
        QuickRescheduleDialog(
            event = reschedulingEvent!!,
            onDismiss = { viewModel.closeRescheduleDialog() },
            onReschedule = { start, end ->
                viewModel.rescheduleEvent(reschedulingEvent!!.id, start, end)
            }
        )
    }

    if (showProfileDialog && currentUser != null) {
        ProfileDialog(
            user = currentUser!!,
            onDismiss = { viewModel.closeProfileDialog() },
            onSaveProfile = { name, bday, tz ->
                viewModel.updateProfile(name, bday, tz)
            },
            onSignOut = { viewModel.signOut() }
        )
    }

    if (showFilterSheet) {
        FilterSheet(
            selectedCategory = selectedCategory,
            onlyUncompleted = onlyUncompleted,
            onCategorySelected = { viewModel.setSelectedCategory(it) },
            onToggleUncompleted = { viewModel.toggleFilterOnlyUncompleted() },
            onDismiss = { viewModel.closeFilterSheet() }
        )
    }

    if (showShortcutsDialog) {
        ShortcutsDialog(
            onDismiss = { viewModel.toggleShortcutsDialog(false) }
        )
    }
}
