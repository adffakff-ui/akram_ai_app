package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Gender
import com.example.data.model.Halaqah
import com.example.data.model.Student
import com.example.ui.theme.EmeraldPrimary

@Composable
fun StudentDialog(
    initialStudent: Student? = null,
    halaqat: List<Halaqah>,
    onDismiss: () -> Unit,
    onSaveStudent: (Student) -> Unit
) {
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var gender by remember { mutableStateOf(initialStudent?.gender ?: Gender.MALE) }
    var halaqahId by remember { mutableStateOf(initialStudent?.halaqahId ?: halaqat.firstOrNull()?.id ?: 1L) }
    var parentName by remember { mutableStateOf(initialStudent?.parentName ?: "") }
    var parentPhone by remember { mutableStateOf(initialStudent?.parentPhone ?: "") }
    var parentCode by remember { mutableStateOf(initialStudent?.parentCode ?: "100" + (1..9).random()) }
    var currentJuz by remember { mutableStateOf(initialStudent?.currentJuz?.toString() ?: "30") }
    var currentSurah by remember { mutableStateOf(initialStudent?.currentSurah ?: "النبأ") }
    var hifzGoal by remember { mutableStateOf(initialStudent?.memorizationGoalPagesPerDay?.toString() ?: "1.0") }
    var revisionGoal by remember { mutableStateOf(initialStudent?.revisionGoalPagesPerDay?.toString() ?: "5.0") }
    var notes by remember { mutableStateOf(initialStudent?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("student_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (initialStudent != null) Icons.Default.Edit else Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialStudent != null) "تَعْدِيلُ بَيَانَاتِ الطَّالِبِ ✏️" else "إِضَافَةُ طَالِبٍ جَدِيدٍ 👤",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الطالب الثلاثي") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("student_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Gender Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = gender == Gender.MALE,
                        onClick = { gender = Gender.MALE },
                        label = { Text("طالب (بنين)") }
                    )
                    FilterChip(
                        selected = gender == Gender.FEMALE,
                        onClick = { gender = Gender.FEMALE },
                        label = { Text("طالبة (بنات)") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Halaqah Picker Chips
                Text(
                    text = "الحلقة المنتسب إليها:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    halaqat.forEach { h ->
                        FilterChip(
                            selected = halaqahId == h.id,
                            onClick = { halaqahId = h.id },
                            label = { Text(h.name, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Parent Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { parentName = it },
                        label = { Text("اسم ولي الأمر") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = { Text("رقم الجوال") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Current Juz & Surah
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentJuz,
                        onValueChange = { currentJuz = it },
                        label = { Text("الجزء الحالي") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentSurah,
                        onValueChange = { currentSurah = it },
                        label = { Text("السورة الحالية") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Daily Smart Goals (Smart Goal Setting Feature)
                Text(
                    text = "أهداف الحفظ والمراجعة اليومية الذكية:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = hifzGoal,
                        onValueChange = { hifzGoal = it },
                        label = { Text("هدف الحفظ (صفحات)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = revisionGoal,
                        onValueChange = { revisionGoal = it },
                        label = { Text("هدف المراجعة (صفحات)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Parent Access Code
                OutlinedTextField(
                    value = parentCode,
                    onValueChange = { parentCode = it },
                    label = { Text("كود دخول ولي الأمر الخاص") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

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
                            if (name.isNotBlank()) {
                                onSaveStudent(
                                    Student(
                                        id = initialStudent?.id ?: 0,
                                        name = name,
                                        gender = gender,
                                        halaqahId = halaqahId,
                                        parentName = parentName.ifBlank { "ولي الأمر" },
                                        parentPhone = parentPhone,
                                        parentCode = parentCode,
                                        currentJuz = currentJuz.toIntOrNull() ?: 30,
                                        currentSurah = currentSurah,
                                        memorizationGoalPagesPerDay = hifzGoal.toFloatOrNull() ?: 1.0f,
                                        revisionGoalPagesPerDay = revisionGoal.toFloatOrNull() ?: 5.0f,
                                        notes = notes
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(1f).testTag("save_student_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ")
                    }
                }
            }
        }
    }
}
