package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.components.CategoryChip
import com.example.ui.theme.CalendarCategories
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun DayView(
    selectedDate: Calendar,
    events: List<EventEntity>,
    notes: List<NoteEntity>,
    onEventClick: (EventEntity) -> Unit,
    onAcknowledgeEvent: (String) -> Unit,
    onToggleComplete: (String, Boolean) -> Unit,
    onNoteClick: (NoteEntity) -> Unit,
    onAddEvent: (Long) -> Unit,
    onAddNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedStart = DateUtils.getStartOfDay(selectedDate)
    val selectedEnd = DateUtils.getEndOfDay(selectedDate)
    val dateStr = DateUtils.isoDateFormatter.format(selectedDate.time)

    val dayEvents = events.filter { it.startTime in selectedStart..selectedEnd }.sortedBy { it.startTime }
    val dayNotes = notes.filter { it.noteDate == dateStr }

    val hours = (0..23).toList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("day_view_timeline"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Date & Integration summary Header: "August 30: 📅 3 events, 📝 2 notes"
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("day_integration_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = DateUtils.fullDateFormatter.format(selectedDate.time),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📅 ${dayEvents.size} events",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("•", color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = "📝 ${dayNotes.size} notes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD97706)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onAddEvent(selectedDate.timeInMillis) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("day_add_event_btn")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Event", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { onAddNote(dateStr) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("day_add_note_btn")
                            ) {
                                Icon(Icons.Outlined.NoteAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Note", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Timeline Hours
        items(hours) { hour ->
            val hourLabel = when {
                hour == 0 -> "12 AM"
                hour < 12 -> "$hour AM"
                hour == 12 -> "12 PM"
                else -> "${hour - 12} PM"
            }

            val slotTime = (selectedDate.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val hourEvents = dayEvents.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.startTime }
                cal.get(Calendar.HOUR_OF_DAY) == hour
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                // Time gutter
                Text(
                    text = hourLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .width(48.dp)
                        .padding(top = 4.dp),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Slot content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { if (hourEvents.isEmpty()) onAddEvent(slotTime) }
                ) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    if (hourEvents.isEmpty()) {
                        Spacer(modifier = Modifier.height(36.dp))
                    } else {
                        hourEvents.forEach { event ->
                            val eventColor = CalendarCategories.parseColor(event.color)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onEventClick(event) }
                                    .testTag("day_event_card_${event.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = eventColor.copy(alpha = 0.12f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, eventColor.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(eventColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
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
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textDecoration = if (event.completed) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    }

                                    if (!event.isAcknowledged && !event.completed) {
                                        IconButton(
                                            onClick = { onAcknowledgeEvent(event.id) },
                                            modifier = Modifier.size(32.dp).testTag("day_ack_btn_${event.id}")
                                        ) {
                                            Icon(
                                                Icons.Filled.NotificationsActive,
                                                contentDescription = "Acknowledge",
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { onToggleComplete(event.id, !event.completed) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (event.completed) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                            contentDescription = "Complete",
                                            tint = if (event.completed) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notes section at bottom of day
        if (dayNotes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Notes for ${DateUtils.monthShortFormatter.format(selectedDate.time)} ${selectedDate.get(Calendar.DAY_OF_MONTH)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            items(dayNotes, key = { it.id }) { note ->
                DateNoteItem(
                    note = note,
                    onClick = { onNoteClick(note) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}
