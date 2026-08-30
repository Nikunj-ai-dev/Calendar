package com.example.ui.calendar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.EventEntity
import com.example.util.DateUtils
import java.util.Calendar
import java.util.Date

@Composable
fun QuickRescheduleDialog(
    event: EventEntity,
    onDismiss: () -> Unit,
    onReschedule: (newStart: Long, newEnd: Long?) -> Unit
) {
    val context = LocalContext.current
    var newStartTime by remember { mutableLongStateOf(event.startTime) }
    val duration = (event.endTime ?: (event.startTime + 3600000L)) - event.startTime

    val cal = remember(newStartTime) {
        Calendar.getInstance().apply { timeInMillis = newStartTime }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("reschedule_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reschedule Event",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = event.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick presets
                Text("Quick Shift", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = newStartTime }
                            c.add(Calendar.HOUR_OF_DAY, 1)
                            newStartTime = c.timeInMillis
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+1 Hour", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = newStartTime }
                            c.add(Calendar.DAY_OF_YEAR, 1)
                            newStartTime = c.timeInMillis
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Tomorrow", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            val c = Calendar.getInstance().apply { timeInMillis = newStartTime }
                            c.add(Calendar.WEEK_OF_YEAR, 1)
                            newStartTime = c.timeInMillis
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+1 Week", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Date & Time
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("New Date:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Button(
                                onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            cal.set(Calendar.YEAR, y)
                                            cal.set(Calendar.MONTH, m)
                                            cal.set(Calendar.DAY_OF_MONTH, d)
                                            newStartTime = cal.timeInMillis
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(DateUtils.fullDateFormatter.format(Date(newStartTime)), fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("New Time:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Button(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, h, m ->
                                            cal.set(Calendar.HOUR_OF_DAY, h)
                                            cal.set(Calendar.MINUTE, m)
                                            newStartTime = cal.timeInMillis
                                        },
                                        cal.get(Calendar.HOUR_OF_DAY),
                                        cal.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(DateUtils.timeFormatter.format(Date(newStartTime)), fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onReschedule(newStartTime, newStartTime + duration)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reschedule_confirm_btn")
                ) {
                    Text("Confirm Reschedule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
