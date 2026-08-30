package com.example.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EventEntity
import com.example.data.local.NoteEntity
import com.example.ui.components.BirthdayBanner
import com.example.ui.components.CategoryChip
import com.example.ui.theme.CalendarCategories
import com.example.util.CalendarDay
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun MonthView(
    selectedDate: Calendar,
    events: List<EventEntity>,
    notes: List<NoteEntity>,
    todayEvents: List<EventEntity> = emptyList(),
    birthdayCountdown: String? = null,
    onDateSelected: (Calendar) -> Unit,
    onEventClick: (EventEntity) -> Unit,
    onAcknowledgeEvent: (String) -> Unit,
    onToggleComplete: (String, Boolean) -> Unit,
    onRescheduleEvent: (EventEntity) -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onAddEvent: (Long) -> Unit,
    onAddNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthDays = DateUtils.getDaysInMonthGrid(selectedDate)
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val selectedStart = DateUtils.getStartOfDay(selectedDate)
    val selectedEnd = DateUtils.getEndOfDay(selectedDate)
    val selectedDateStr = DateUtils.isoDateFormatter.format(selectedDate.time)

    val selectedDateEvents = events.filter { it.startTime in selectedStart..selectedEnd }
        .sortedBy { it.startTime }
    val selectedDateNotes = notes.filter { it.noteDate == selectedDateStr }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("month_view_list"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Birthday Banner (Scrollable)
        if (birthdayCountdown != null) {
            item {
                BirthdayBanner(countdownText = birthdayCountdown)
            }
        }

        // Today's Schedule Carousel (Scrolls with page so calendar has full clear visibility)
        item {
            TodayCardsCarousel(
                todayEvents = todayEvents,
                onAcknowledge = onAcknowledgeEvent,
                onToggleComplete = onToggleComplete,
                onEditEvent = onEventClick,
                onReschedule = onRescheduleEvent,
                onAddEvent = { onAddEvent(System.currentTimeMillis()) }
            )
        }

        // Month Day Headers (Sun..Sat)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Calendar Grid
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    val rows = monthDays.chunked(7)
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { day ->
                                MonthDayCell(
                                    day = day,
                                    isSelected = DateUtils.isSameDay(day.calendar, selectedDate),
                                    events = events.filter {
                                        DateUtils.isSameDay(it.startTime, day.calendar.timeInMillis)
                                    },
                                    hasNotes = notes.any {
                                        it.noteDate == DateUtils.isoDateFormatter.format(day.calendar.time)
                                    },
                                    onSelect = { onDateSelected(day.calendar) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Integration Bar: "August 30: 📅 3 events, 📝 2 notes"
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("month_date_integration_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = DateUtils.fullDateFormatter.format(selectedDate.time),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "📅 ${selectedDateEvents.size} events",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "•",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "📝 ${selectedDateNotes.size} notes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFD97706)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { onAddEvent(selectedDate.timeInMillis) },
                            modifier = Modifier.testTag("month_add_event_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Event", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onAddNote(selectedDateStr) },
                            modifier = Modifier.testTag("month_add_note_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.NoteAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Note", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section: Events for Selected Date
        item {
            Text(
                text = "Events",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        if (selectedDateEvents.isEmpty()) {
            item {
                Text(
                    text = "No events scheduled for this date",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        } else {
            items(selectedDateEvents, key = { it.id }) { event ->
                EventListItem(
                    event = event,
                    onEventClick = { onEventClick(event) },
                    onAcknowledge = { onAcknowledgeEvent(event.id) },
                    onToggleComplete = { onToggleComplete(event.id, !event.completed) },
                    onReschedule = { onRescheduleEvent(event) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Section: Notes for Selected Date
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Notes for this Date",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        if (selectedDateNotes.isEmpty()) {
            item {
                Text(
                    text = "No notes attached to this date yet",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }
        } else {
            items(selectedDateNotes, key = { it.id }) { note ->
                DateNoteItem(
                    note = note,
                    onClick = { onNoteClick(note) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MonthDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    events: List<EventEntity>,
    hasNotes: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBirthday = events.any { it.category.equals("Birthday", ignoreCase = true) }
    val hasUnacknowledged = events.any { !it.isAcknowledged && !it.completed }

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    day.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (day.isToday && !isSelected) 1.5.dp else 0.dp,
                color = if (day.isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .testTag("day_cell_${day.dayNumber}_${day.isCurrentMonth}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${day.dayNumber}",
                    fontSize = 13.sp,
                    fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> Color.White
                        !day.isCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        day.isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                if (isBirthday) {
                    Text("🎂", fontSize = 9.sp, modifier = Modifier.padding(start = 2.dp))
                }
            }

            // Event dots or bar chips
            if (events.isNotEmpty() || hasNotes) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    events.take(3).forEach { event ->
                        val color = CalendarCategories.parseColor(event.color)
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else color)
                        )
                    }
                    if (hasNotes) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (isSelected) Color.White else Color(0xFFF59E0B))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventListItem(
    event: EventEntity,
    onEventClick: () -> Unit,
    onAcknowledge: () -> Unit,
    onToggleComplete: () -> Unit,
    onReschedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eventColor = CalendarCategories.parseColor(event.color)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEventClick)
            .testTag("event_item_${event.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (event.isAcknowledged) Color(0xFF386B01).copy(alpha = 0.35f) else eventColor.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryChip(categoryName = event.category, customColor = event.color)
                    Text(
                        text = DateUtils.formatEventTimeRange(event.startTime, event.endTime, event.allDay),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = event.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (event.completed) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (event.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                if (!event.location.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = event.location,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Quick Acknowledge button or check
            if (!event.isAcknowledged && !event.completed) {
                IconButton(
                    onClick = onAcknowledge,
                    modifier = Modifier.size(34.dp).testTag("list_ack_btn_${event.id}")
                ) {
                    Icon(
                        Icons.Filled.NotificationsActive,
                        contentDescription = "Acknowledge Event",
                        tint = Color(0xFF825500),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Complete checkbox
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(34.dp).testTag("list_complete_btn_${event.id}")
            ) {
                Icon(
                    imageVector = if (event.completed) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    contentDescription = if (event.completed) "Completed" else "Complete",
                    tint = if (event.completed) Color(0xFF386B01) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DateNoteItem(
    note: NoteEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("date_note_${note.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📝", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
