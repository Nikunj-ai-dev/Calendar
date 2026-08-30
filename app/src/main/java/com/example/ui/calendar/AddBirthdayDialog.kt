package com.example.ui.calendar

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.CalendarCategories
import com.example.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBirthdayDialog(
    onDismiss: () -> Unit,
    onSaveBirthday: (
        personName: String,
        birthDateIso: String,
        relationship: String,
        giftIdeas: String,
        notes: String,
        reminderMinutesBefore: Int,
        color: String
    ) -> Unit
) {
    val context = LocalContext.current

    var personName by remember { mutableStateOf("") }
    var selectedRelationship by remember { mutableStateOf("Friend") }
    val relationships = listOf("Friend", "Family", "Partner", "Colleague", "Loved One")

    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }

    var giftIdeas by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#EC4899") } // Festive pink

    // Reminder option
    var reminderOptionIndex by remember { mutableIntStateOf(0) }
    val reminderPresets = listOf(
        Pair("On Birthday (9:00 AM)", 0),
        Pair("1 day before (to get gifts)", 1440),
        Pair("3 days before", 4320),
        Pair("1 week before", 10080)
    )

    val birthdayDateText = remember(selectedMonth, selectedDay, selectedYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
        }
        val monthStr = SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
        if (selectedYear != null) "$monthStr $selectedDay, $selectedYear" else "$monthStr $selectedDay (Every Year)"
    }

    val ageCalculation = remember(selectedYear) {
        if (selectedYear != null) {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val turningAge = currentYear - selectedYear!!
            if (turningAge > 0) "Turning $turningAge years old! 🎂" else null
        } else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("birthday_dialog_surface"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Header with celebratory theme
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFDF2F8))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF472B6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎂", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                text = "Add Birthday Event",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF831843)
                            )
                            Text(
                                text = "Yearly recurring reminder & countdown",
                                fontSize = 12.sp,
                                color = Color(0xFFBE185D)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF831843))
                    }
                }

                HorizontalDivider(color = Color(0xFFFBCFE8))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // Person Name Input
                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("Person's Name *") },
                        placeholder = { Text("e.g. Mom, Rahul, Sarah") },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFFEC4899)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("birthday_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Relationship Chips
                    Text(
                        text = "Relationship / Group",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        relationships.forEach { rel ->
                            FilterChip(
                                selected = selectedRelationship == rel,
                                onClick = { selectedRelationship = rel },
                                label = { Text(rel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFCE7F3),
                                    selectedLabelColor = Color(0xFFBE185D)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Birthday Date Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        selectedYear = y
                                        selectedMonth = m
                                        selectedDay = d
                                    },
                                    selectedYear ?: currentYear,
                                    selectedMonth,
                                    selectedDay
                                ).show()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2F8))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFCE7F3)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color(0xFFEC4899)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Birthday Date",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF9D174D)
                                    )
                                    Text(
                                        text = birthdayDateText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF831843)
                                    )
                                    if (ageCalculation != null) {
                                        Text(
                                            text = ageCalculation,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFBE185D)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            selectedYear = y
                                            selectedMonth = m
                                            selectedDay = d
                                        },
                                        selectedYear ?: currentYear,
                                        selectedMonth,
                                        selectedDay
                                    ).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEC4899),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Change", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-frequency indicator
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(
                                    text = "Frequency: Automatically Set to Yearly 🔁",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Will repeat every year automatically so you never miss it",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reminder Presets
                    Text(
                        text = "Notification Reminder",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        reminderPresets.forEachIndexed { idx, pair ->
                            val isSelected = reminderOptionIndex == idx
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { reminderOptionIndex = idx },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFFCE7F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                tonalElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.NotificationsActive,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFFEC4899) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = pair.first,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF831843) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Gift Ideas
                    OutlinedTextField(
                        value = giftIdeas,
                        onValueChange = { giftIdeas = it },
                        label = { Text("Gift Ideas (Optional)") },
                        placeholder = { Text("e.g. Watch, books, favorite cake") },
                        leadingIcon = { Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = Color(0xFFEC4899)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Celebration Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Celebration Notes (Optional)") },
                        placeholder = { Text("e.g. Planning surprise party at 7 PM") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fixed Bottom Action Bar with Elevated Navigation Padding
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text("Cancel", fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                if (personName.isNotBlank()) {
                                    val m = (selectedMonth + 1).toString().padStart(2, '0')
                                    val d = selectedDay.toString().padStart(2, '0')
                                    val birthDateIso = if (selectedYear != null) "$selectedYear-$m-$d" else "$m-$d"
                                    val reminderMinutes = reminderPresets[reminderOptionIndex].second

                                    onSaveBirthday(
                                        personName.trim(),
                                        birthDateIso,
                                        selectedRelationship,
                                        giftIdeas.trim(),
                                        notes.trim(),
                                        reminderMinutes,
                                        selectedColor
                                    )
                                }
                            },
                            enabled = personName.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEC4899),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("save_birthday_btn")
                        ) {
                            Text("Save Birthday 🎂", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
