package com.example.ui.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.EventEntity
import com.example.ui.components.CategoryChip
import com.example.ui.theme.CalendarCategories
import com.example.util.DateUtils
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDialog(
    event: EventEntity,
    onDismiss: () -> Unit,
    onSave: (EventEntity) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description ?: "") }
    var location by remember { mutableStateOf(event.location ?: "") }
    var startTime by remember { mutableLongStateOf(event.startTime) }
    var endTime by remember { mutableLongStateOf(event.endTime ?: (event.startTime + 3600000L)) }
    var allDay by remember { mutableStateOf(event.allDay) }
    var category by remember { mutableStateOf(event.category ?: "Work") }
    var colorHex by remember { mutableStateOf(event.color) }

    // Recurrence
    var isRecurring by remember { mutableStateOf(event.isRecurring) }
    var recurrenceRule by remember { mutableStateOf(event.recurrenceRule ?: "DAILY") }
    var recurrenceExpanded by remember { mutableStateOf(false) }
    val recurrenceOptions = listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY", "CUSTOM")

    // Reminder minutes before
    var reminderMinutes by remember { mutableIntStateOf(event.reminderMinutesBefore) }
    var reminderExpanded by remember { mutableStateOf(false) }
    val reminderOptions = listOf(0, 5, 10, 15, 30, 60, 1440) // 0 = at time, 1440 = 1 day

    // Repeating notification settings (gap and count)
    var repeatCount by remember { mutableIntStateOf(event.repeatNotificationCount) }
    var repeatGapMinutes by remember { mutableIntStateOf(event.repeatGapMinutes) }

    val startCal = remember(startTime) { Calendar.getInstance().apply { timeInMillis = startTime } }
    val endCal = remember(endTime) { Calendar.getInstance().apply { timeInMillis = endTime } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("event_dialog_surface"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Fixed Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (event.title.isBlank()) "Create Event" else "Edit Event",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title *") },
                        placeholder = { Text("e.g. Meeting with Rahul") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_title_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Selector in Straight Horizontal Scroll Line
                    Text(
                        text = "Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalendarCategories.ALL.forEach { cat ->
                            CategoryChip(
                                categoryName = cat.name,
                                selected = category.equals(cat.name, ignoreCase = true),
                                onClick = {
                                    category = cat.name
                                    colorHex = cat.defaultColor
                                }
                            )
                        }
                    }

                    // Palette Colors
                    Text(
                        text = "Event Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalendarCategories.PRESET_COLORS.forEach { hex ->
                            val parsed = CalendarCategories.parseColor(hex)
                            val isSelected = colorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(parsed)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { colorHex = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                // All Day Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All-day Event",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = allDay,
                        onCheckedChange = { allDay = it },
                        modifier = Modifier.testTag("all_day_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date & Time pickers
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Start Date / Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Start", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                startCal.set(Calendar.YEAR, y)
                                                startCal.set(Calendar.MONTH, m)
                                                startCal.set(Calendar.DAY_OF_MONTH, d)
                                                startTime = startCal.timeInMillis
                                                if (endTime < startTime) endTime = startTime + 3600000L
                                            },
                                            startCal.get(Calendar.YEAR),
                                            startCal.get(Calendar.MONTH),
                                            startCal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                ) {
                                    Text(
                                        text = DateUtils.fullDateFormatter.format(Date(startTime)),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }

                                if (!allDay) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable {
                                            TimePickerDialog(
                                                context,
                                                { _, h, min ->
                                                    startCal.set(Calendar.HOUR_OF_DAY, h)
                                                    startCal.set(Calendar.MINUTE, min)
                                                    startTime = startCal.timeInMillis
                                                    if (endTime < startTime) endTime = startTime + 3600000L
                                                },
                                                startCal.get(Calendar.HOUR_OF_DAY),
                                                startCal.get(Calendar.MINUTE),
                                                false
                                            ).show()
                                        }
                                    ) {
                                        Text(
                                            text = DateUtils.timeFormatter.format(Date(startTime)),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (!allDay) {
                            Spacer(modifier = Modifier.height(10.dp))
                            // End Date / Time
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("End", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable {
                                            DatePickerDialog(
                                                context,
                                                { _, y, m, d ->
                                                    endCal.set(Calendar.YEAR, y)
                                                    endCal.set(Calendar.MONTH, m)
                                                    endCal.set(Calendar.DAY_OF_MONTH, d)
                                                    endTime = endCal.timeInMillis
                                                },
                                                endCal.get(Calendar.YEAR),
                                                endCal.get(Calendar.MONTH),
                                                endCal.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }
                                    ) {
                                        Text(
                                            text = DateUtils.fullDateFormatter.format(Date(endTime)),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable {
                                            TimePickerDialog(
                                                context,
                                                { _, h, min ->
                                                    endCal.set(Calendar.HOUR_OF_DAY, h)
                                                    endCal.set(Calendar.MINUTE, min)
                                                    endTime = endCal.timeInMillis
                                                },
                                                endCal.get(Calendar.HOUR_OF_DAY),
                                                endCal.get(Calendar.MINUTE),
                                                false
                                            ).show()
                                        }
                                    ) {
                                        Text(
                                            text = DateUtils.timeFormatter.format(Date(endTime)),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recurrence Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Repeat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Repeat Event", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
                }

                if (isRecurring) {
                    ExposedDropdownMenuBox(
                        expanded = recurrenceExpanded,
                        onExpandedChange = { recurrenceExpanded = !recurrenceExpanded }
                    ) {
                        OutlinedTextField(
                            value = when (recurrenceRule) {
                                "DAILY" -> "Every day"
                                "WEEKLY" -> "Every week"
                                "MONTHLY" -> "Every month"
                                "YEARLY" -> "Every year"
                                else -> "Custom recurrence"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Recurrence Frequency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = recurrenceExpanded,
                            onDismissRequest = { recurrenceExpanded = false }
                        ) {
                            recurrenceOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (opt) {
                                                "DAILY" -> "Every day"
                                                "WEEKLY" -> "Every week"
                                                "MONTHLY" -> "Every month"
                                                "YEARLY" -> "Every year"
                                                else -> "Custom recurrence"
                                            }
                                        )
                                    },
                                    onClick = {
                                        recurrenceRule = opt
                                        recurrenceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Persistent Repeating Reminders Settings (CRITICAL REQUIREMENT)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Smart Repeating Reminders",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Send multiple alerts until acknowledged so you never miss an event",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reminder offset
                        ExposedDropdownMenuBox(
                            expanded = reminderExpanded,
                            onExpandedChange = { reminderExpanded = !reminderExpanded }
                        ) {
                            OutlinedTextField(
                                value = when (reminderMinutes) {
                                    0 -> "At event time"
                                    60 -> "1 hour before"
                                    1440 -> "1 day before"
                                    else -> "$reminderMinutes minutes before"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Initial Alert Time") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = reminderExpanded,
                                onDismissRequest = { reminderExpanded = false }
                            ) {
                                reminderOptions.forEach { mins ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                when (mins) {
                                                    0 -> "At event time"
                                                    60 -> "1 hour before"
                                                    1440 -> "1 day before"
                                                    else -> "$mins minutes before"
                                                }
                                            )
                                        },
                                        onClick = {
                                            reminderMinutes = mins
                                            reminderExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Repeat Count Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Repeat alert count", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("$repeatCount times", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = repeatCount.toFloat(),
                            onValueChange = { repeatCount = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Repeat Gap Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gap between repeat alerts", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("$repeatGapMinutes min gap", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = repeatGapMinutes.toFloat(),
                            onValueChange = { repeatGapMinutes = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Location & Description
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Notes") },
                        leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fixed Bottom Action Bar (Safe from device navigation bar)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onDelete != null && event.title.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onDelete(event.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("delete_event_btn")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }

                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onSave(
                                        event.copy(
                                            title = title.trim(),
                                            description = description.ifBlank { null },
                                            location = location.ifBlank { null },
                                            startTime = startTime,
                                            endTime = if (allDay) null else endTime,
                                            allDay = allDay,
                                            category = category,
                                            color = colorHex,
                                            isRecurring = isRecurring,
                                            recurrenceRule = if (isRecurring) recurrenceRule else null,
                                            reminderMinutesBefore = reminderMinutes,
                                            repeatNotificationCount = repeatCount,
                                            repeatGapMinutes = repeatGapMinutes,
                                            isAcknowledged = false,
                                            updatedAt = System.currentTimeMillis()
                                        )
                                    )
                                }
                            },
                            enabled = title.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("save_event_btn")
                        ) {
                            Text("Save Event", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
