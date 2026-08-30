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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EventEntity
import com.example.ui.theme.CalendarCategories
import com.example.util.CalendarDay
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun WeekView(
    selectedDate: Calendar,
    events: List<EventEntity>,
    onDateSelected: (Calendar) -> Unit,
    onEventClick: (EventEntity) -> Unit,
    onAddEventAtTime: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDays = DateUtils.getDaysOfWeek(selectedDate)
    val hours = (0..23).toList()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("week_view_container")
    ) {
        // Week Days Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { day ->
                    val isSelected = DateUtils.isSameDay(day.calendar, selectedDate)
                    val isToday = day.isToday
                    val dayName = DateUtils.dayOfWeekFormatter.format(day.calendar.time)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { onDateSelected(day.calendar) }
                            .padding(vertical = 6.dp)
                            .testTag("week_header_day_${day.dayNumber}")
                    ) {
                        Text(
                            text = dayName.take(3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${day.dayNumber}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Scrollable Hours Schedule
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            hours.forEach { hour ->
                val hourLabel = when {
                    hour == 0 -> "12 AM"
                    hour < 12 -> "$hour AM"
                    hour == 12 -> "12 PM"
                    else -> "${hour - 12} PM"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    // Time Label
                    Text(
                        text = hourLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .width(46.dp)
                            .padding(start = 6.dp, top = 2.dp),
                        textAlign = TextAlign.End
                    )

                    // 7 Column grid slots
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(width = 0.3.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        weekDays.forEach { day ->
                            val slotTime = (day.calendar.clone() as Calendar).apply {
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }.timeInMillis

                            // Events that fall in this hour
                            val slotEvents = events.filter { event ->
                                DateUtils.isSameDay(event.startTime, slotTime) &&
                                Calendar.getInstance().apply { timeInMillis = event.startTime }.get(Calendar.HOUR_OF_DAY) == hour
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .border(width = 0.2.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    .clickable { onAddEventAtTime(slotTime) }
                                    .padding(1.dp)
                            ) {
                                slotEvents.firstOrNull()?.let { event ->
                                    val eventColor = CalendarCategories.parseColor(event.color)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onEventClick(event) },
                                        color = eventColor.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(2.dp)) {
                                            Text(
                                                text = event.title,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
