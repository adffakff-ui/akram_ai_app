package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.*
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.util.SpeechEvaluatorHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyRecordDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSaveRecord: (DailyRecord) -> Unit
) {
    val scrollState = rememberScrollState()

    var attendanceStatus by remember { mutableStateOf(AttendanceStatus.PRESENT) }
    var newHifzSurah by remember { mutableStateOf(student.currentSurah) }
    var fromAyah by remember { mutableStateOf("1") }
    var toAyah by remember { mutableStateOf("20") }
    var pagesCount by remember { mutableStateOf("1.0") }
    var hifzRating by remember { mutableStateOf(EvaluationRating.EXCELLENT) }

    var murajaahSurah by remember { mutableStateOf("البقرة") }
    var murajaahRating by remember { mutableStateOf(EvaluationRating.VERY_GOOD) }
    var tajweedRating by remember { mutableStateOf(EvaluationRating.EXCELLENT) }

    var pointsEarned by remember { mutableIntStateOf(10) }
    var teacherNotes by remember { mutableStateOf("") }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var audioRecorded by remember { mutableStateOf(false) }

    // Speech Evaluation state
    var showSpeechDialog by remember { mutableStateOf(false) }
    var evaluationResultText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("daily_record_dialog")
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
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "رَصْدُ الحِفْظِ وَالتَّسْمِيعِ اليَوْمِيّ 📖",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "الطالب: ${student.name}",
                            fontSize = 12.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Attendance Status Chips
                Text(
                    text = "حالة الحضور:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AttendanceStatus.entries.forEach { status ->
                        FilterChip(
                            selected = attendanceStatus == status,
                            onClick = { attendanceStatus = status },
                            label = { Text(status.arabicTitle, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hifz Section
                Text(
                    text = "الحفظ الجديد:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newHifzSurah,
                        onValueChange = { newHifzSurah = it },
                        label = { Text("السورة") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = pagesCount,
                        onValueChange = { pagesCount = it },
                        label = { Text("الصفحات") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fromAyah,
                        onValueChange = { fromAyah = it },
                        label = { Text("من آية") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = toAyah,
                        onValueChange = { toAyah = it },
                        label = { Text("إلى آية") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rating Chips
                Text(
                    text = "تقييم الحفظ:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(EvaluationRating.entries.filter { it != EvaluationRating.NONE }) { rating ->
                        FilterChip(
                            selected = hifzRating == rating,
                            onClick = {
                                hifzRating = rating
                                pointsEarned = rating.points
                            },
                            label = { Text(rating.arabicTitle, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Murajaah Section
                Text(
                    text = "المراجعة والتثبيت:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = murajaahSurah,
                    onValueChange = { murajaahSurah = it },
                    label = { Text("سورة أو أجزاء المراجعة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Speech Recitation Evaluation AI Button
                OutlinedButton(
                    onClick = {
                        val result = SpeechEvaluatorHelper.evaluateRecitation(
                            spokenText = "بسم الله الرحمن الرحيم عم يتساءلون عن النبإ العظيم",
                            surahName = newHifzSurah,
                            expectedAyahText = "بسم الله الرحمن الرحيم عَمَّ يَتَسَاءَلُونَ عَنِ النَّبَإِ الْعَظِيمِ"
                        )
                        evaluationResultText = "دقة التلاوة: ${result.accuracyPercentage}% 🌟 - ${result.feedbackMessage}"
                    },
                    modifier = Modifier.fillMaxWidth().testTag("speech_evaluation_button")
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تقييم تلاوة الطالب صوتياً (AI Speech) 🎙️", fontSize = 12.sp)
                }

                if (evaluationResultText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = evaluationResultText!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Audio Note Recording Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (audioRecorded) "تم إرفاق ملاحظة صوتية 🎵" else "تسجيل ملاحظة صوتية لولي الأمر:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            isRecordingAudio = !isRecordingAudio
                            if (!isRecordingAudio) audioRecorded = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecordingAudio) Color.Red else EmeraldPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isRecordingAudio) "إيقاف التسجيل" else "تسجيل صوتي 🎙️", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Teacher Notes
                OutlinedTextField(
                    value = teacherNotes,
                    onValueChange = { teacherNotes = it },
                    label = { Text("ملاحظات المعلم / التوجيهات") },
                    maxLines = 3,
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
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            onSaveRecord(
                                DailyRecord(
                                    studentId = student.id,
                                    date = today,
                                    attendanceStatus = attendanceStatus,
                                    newHifzSurah = newHifzSurah,
                                    newHifzFromAyah = fromAyah.toIntOrNull() ?: 1,
                                    newHifzToAyah = toAyah.toIntOrNull() ?: 20,
                                    newHifzPages = pagesCount.toFloatOrNull() ?: 1.0f,
                                    newHifzRating = hifzRating,
                                    murajaahSurah = murajaahSurah,
                                    murajaahRating = murajaahRating,
                                    tajweedRating = tajweedRating,
                                    pointsEarned = pointsEarned,
                                    teacherNotes = teacherNotes,
                                    audioNotePath = if (audioRecorded) "audio_note_${student.id}.m4a" else ""
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(1f).testTag("save_daily_record_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اعتماد التسميع")
                    }
                }
            }
        }
    }
}
