package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QuickAttendanceScreen(
    viewModel: QuranViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentTeacherHalaqahId by viewModel.currentTeacherHalaqahId.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val halaqat by viewModel.allHalaqat.collectAsState()
    val selectedHalaqahId by viewModel.selectedHalaqahId.collectAsState()

    val attendanceMap = remember { mutableStateMapOf<Long, AttendanceStatus>() }

    // Initialize map
    LaunchedEffect(students) {
        students.forEach { s ->
            if (!attendanceMap.containsKey(s.id)) {
                attendanceMap[s.id] = AttendanceStatus.PRESENT
            }
        }
    }

    val filteredList = remember(students, selectedHalaqahId, currentRole, currentTeacherHalaqahId) {
        if (currentRole == UserRole.TEACHER) {
            val effectiveHalaqahId = currentTeacherHalaqahId ?: halaqat.firstOrNull()?.id
            if (effectiveHalaqahId != null) students.filter { it.halaqahId == effectiveHalaqahId } else students
        } else {
            if (selectedHalaqahId == null) students else students.filter { it.halaqahId == selectedHalaqahId }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("quick_attendance_screen")
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "التَّحْضِيرُ السَّرِيعُ لِلحَلَقَاتِ ⏰",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            val today = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
                            Text(
                                text = "تاريخ اليوم: $today",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            filteredList.forEach { s -> attendanceMap[s.id] = AttendanceStatus.PRESENT }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("الكل حاضر ✅", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Students Attendance List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredList, key = { it.id }) { student ->
                val currentStatus = attendanceMap[student.id] ?: AttendanceStatus.PRESENT
                val hName = halaqat.find { it.id == student.halaqahId }?.name ?: "الحلقة"

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = hName, fontSize = 11.sp, color = Color.Gray)
                        }

                        // Toggle Buttons for status
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { attendanceMap[student.id] = AttendanceStatus.PRESENT },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (currentStatus == AttendanceStatus.PRESENT) AttendancePresentGreen else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "حاضر",
                                    tint = if (currentStatus == AttendanceStatus.PRESENT) Color.White else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { attendanceMap[student.id] = AttendanceStatus.ABSENT },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (currentStatus == AttendanceStatus.ABSENT) AttendanceAbsentRed else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "غائب",
                                    tint = if (currentStatus == AttendanceStatus.ABSENT) Color.White else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { attendanceMap[student.id] = AttendanceStatus.EXCUSED },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (currentStatus == AttendanceStatus.EXCUSED) AttendanceExcusedYellow else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "مستأذن",
                                    tint = if (currentStatus == AttendanceStatus.EXCUSED) Color.White else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Save All Button
        Button(
            onClick = {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                filteredList.forEach { student ->
                    val status = attendanceMap[student.id] ?: AttendanceStatus.PRESENT
                    viewModel.recordDailyEvaluation(
                        record = DailyRecord(
                            studentId = student.id,
                            date = today,
                            attendanceStatus = status,
                            pointsEarned = if (status == AttendanceStatus.PRESENT) 5 else 0
                        ),
                        student = student
                    )
                    // If absent, send immediate notification to parent
                    if (status == AttendanceStatus.ABSENT) {
                        viewModel.sendPushNotificationToStudent(
                            student = student,
                            title = "تنبيه غياب عن حلقة اليوم ⚠️",
                            message = "المكرم ${student.parentName}، نود إحاطتكم بتغيب ابنكم ${student.name} عن حلقة اليوم. نأمل الاطمئنان عليه.",
                            type = NotificationType.ATTENDANCE,
                            postSystemNotification = true
                        )
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_all_attendance_button")
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("حِفْظُ وَاعْتِمَادُ التَّحْضِيرِ لِلجَمِيعِ 💾", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
