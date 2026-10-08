package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Halaqah
import com.example.data.model.ScheduleEvent
import com.example.data.model.ScheduleEventType
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@Composable
fun ScheduleEventDialog(
    initialDate: String,
    halaqat: List<Halaqah>,
    onDismiss: () -> Unit,
    onSaveEvent: (ScheduleEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ScheduleEventType.HALAQAH_SESSION) }
    var date by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf("16:30") }
    var endTime by remember { mutableStateOf("18:00") }
    var location by remember { mutableStateOf("مسجد الحلقة - القاعة الرئيسية") }
    var examinerName by remember { mutableStateOf("") }
    var targetSurahOrJuz by remember { mutableStateOf("") }
    var selectedHalaqahId by remember { mutableStateOf(halaqat.firstOrNull()?.id) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("schedule_event_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جَدْوَلَةُ حَلْقَةٍ أَوْ امْتِحَانٍ 📅",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Event Type Selection Chips
                Text(
                    text = "نوع الموعد:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ScheduleEventType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                if (title.isBlank()) {
                                    title = when (type) {
                                        ScheduleEventType.EXAM -> "امتحان تقييم الجزء"
                                        ScheduleEventType.HALAQAH_SESSION -> "حلقة التحفيظ الدورية"
                                        ScheduleEventType.MAJOR_REVISION -> "جلسة مراجعة وتثبيت"
                                        else -> type.arabicTitle
                                    }
                                }
                            },
                            label = { Text(type.arabicTitle, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الموعد / الاختبار") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("schedule_event_title_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date & Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("التاريخ (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("من") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("إلى") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target Juz / Surah
                OutlinedTextField(
                    value = targetSurahOrJuz,
                    onValueChange = { targetSurahOrJuz = it },
                    label = { Text("المقرر المستهدف (مثال: جزء عم كامل)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location & Examiner Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("المكان / القاعة") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = examinerName,
                        onValueChange = { examinerName = it },
                        label = { Text("الممتحن / المشرف") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val halaqah = halaqat.find { it.id == selectedHalaqahId }
                                onSaveEvent(
                                    ScheduleEvent(
                                        title = title,
                                        eventType = selectedType,
                                        halaqahId = selectedHalaqahId,
                                        halaqahName = halaqah?.name ?: "جميع الحلقات",
                                        date = date,
                                        startTime = startTime,
                                        endTime = endTime,
                                        location = location,
                                        examinerName = examinerName,
                                        targetSurahOrJuz = targetSurahOrJuz,
                                        notes = notes
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(1f).testTag("save_schedule_event_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ الموعد")
                    }
                }
            }
        }
    }
}
