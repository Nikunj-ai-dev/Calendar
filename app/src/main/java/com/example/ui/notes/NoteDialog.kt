package com.example.ui.notes

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.local.NoteEntity
import com.example.ui.theme.CalendarCategories
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun NoteDialog(
    note: NoteEntity,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }
    var noteDate by remember { mutableStateOf(note.noteDate ?: DateUtils.isoDateFormatter.format(Calendar.getInstance().time)) }
    var pinned by remember { mutableStateOf(note.pinned) }
    var archived by remember { mutableStateOf(note.archived) }
    var colorHex by remember { mutableStateOf(note.color ?: "#3B82F6") }

    val cal = remember { Calendar.getInstance() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("note_dialog_surface"),
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
                        text = if (note.title.isBlank()) "New Note" else "Edit Note",
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
                        label = { Text("Note Title *") },
                        placeholder = { Text("e.g. Sprint Roadmap Action Items") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Attachment Picker
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Attach to Date: $noteDate", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }

                            Button(
                                onClick = {
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            cal.set(Calendar.YEAR, y)
                                            cal.set(Calendar.MONTH, m)
                                            cal.set(Calendar.DAY_OF_MONTH, d)
                                            noteDate = DateUtils.isoDateFormatter.format(cal.time)
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Change", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Formatting Toolbar (Bold, Italic, Bullets, Checklists)
                    Text(
                        text = "Formatting Tools",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = { content += if (content.endsWith("\n") || content.isEmpty()) "**Bold text**" else "\n**Bold text**" },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp))
                        }

                        FilledTonalIconButton(
                            onClick = { content += if (content.endsWith("\n") || content.isEmpty()) "*Italic text*" else "\n*Italic text*" },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp))
                        }

                        FilledTonalIconButton(
                            onClick = { content += if (content.endsWith("\n") || content.isEmpty()) "• " else "\n• " },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.FormatListBulleted, contentDescription = "Bullets", modifier = Modifier.size(18.dp))
                        }

                        FilledTonalIconButton(
                            onClick = { content += if (content.endsWith("\n") || content.isEmpty()) "[ ] " else "\n[ ] " },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Outlined.Checklist, contentDescription = "Checklist", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Content TextField
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Note Content & Markdown") },
                        placeholder = { Text("Write details, tasks, meeting notes...") },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_content_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pin & Archive Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = null,
                                tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Text("Pin to Top", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Switch(checked = pinned, onCheckedChange = { pinned = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (archived) Icons.Filled.Archive else Icons.Outlined.Archive,
                                contentDescription = null,
                                tint = if (archived) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.outline
                            )
                            Text("Archive Note", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Switch(checked = archived, onCheckedChange = { archived = it })
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fixed Bottom Action Bar
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
                        if (onDelete != null && note.title.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onDelete(note.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }

                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onSave(
                                        note.copy(
                                            title = title.trim(),
                                            content = content.trim(),
                                            noteDate = noteDate.ifBlank { null },
                                            pinned = pinned,
                                            archived = archived,
                                            color = colorHex,
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
                                .testTag("save_note_btn")
                        ) {
                            Text("Save Note", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
