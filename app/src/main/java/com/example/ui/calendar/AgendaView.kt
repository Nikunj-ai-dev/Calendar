package com.example.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EventEntity
import com.example.util.DateUtils
import java.util.Calendar
import java.util.Date

@Composable
fun AgendaView(
    events: List<EventEntity>,
    onEventClick: (EventEntity) -> Unit,
    onAcknowledgeEvent: (String) -> Unit,
    onToggleComplete: (String, Boolean) -> Unit,
    onRescheduleEvent: (EventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedEvents = events.sortedBy { it.startTime }
    val grouped = sortedEvents.groupBy { event ->
        DateUtils.isoDateFormatter.format(Date(event.startTime))
    }

    if (grouped.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp)
                .testTag("empty_agenda_view"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📅", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No events in agenda",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tap the + button below to add your first event",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("agenda_list"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            grouped.forEach { (dateStr, eventsOnDate) ->
                item(key = "header_$dateStr") {
                    val firstEventTime = eventsOnDate.first().startTime
                    val isToday = DateUtils.isToday(firstEventTime)
                    val headerText = if (isToday) {
                        "Today, ${DateUtils.fullDateFormatter.format(Date(firstEventTime))}"
                    } else {
                        DateUtils.fullDateFormatter.format(Date(firstEventTime))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = headerText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${eventsOnDate.size} items",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                items(eventsOnDate, key = { it.id }) { event ->
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
        }
    }
}
