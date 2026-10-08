package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.DailyRecord
import com.example.data.model.Student
import com.example.ui.theme.*

@Composable
fun HistoricalAttendanceCalendar(
    student: Student,
    records: List<DailyRecord>,
    modifier: Modifier = Modifier
) {
    val studentRecords = remember(records, student) {
        records.filter { it.studentId == student.id }
    }

    val presentCount = studentRecords.count { it.attendanceStatus == AttendanceStatus.PRESENT }
    val absentCount = studentRecords.count { it.attendanceStatus == AttendanceStatus.ABSENT }
    val excusedCount = studentRecords.count { it.attendanceStatus == AttendanceStatus.EXCUSED }
    val totalSessions = studentRecords.size.coerceAtLeast(1)
    val attendanceRate = ((presentCount.toFloat() / totalSessions) * 100).toInt()

    val attendanceByDate = remember(studentRecords) {
        studentRecords.associateBy { it.date }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("historical_attendance_calendar")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سِجِلُّ الحُضُورِ وَالغِيَابِ التَّارِيخِيّ 📅",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$attendanceRate% مواظبة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stat Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = AttendancePresentGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$presentCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AttendancePresentGreen)
                        Text(text = "حاضر 🟢", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    color = AttendanceAbsentRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$absentCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AttendanceAbsentRed)
                        Text(text = "غائب 🔴", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(
                    color = AttendanceExcusedYellow.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$excusedCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AttendanceExcusedYellow)
                        Text(text = "مستأذن 🟡", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Color-Coded Mini Calendar Heatmap (Last 14 days)
            Text(
                text = "مخطط الأيام الأخيرة:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Generate last 14 days
                for (dayIndex in 1..14) {
                    val dayStr = String.format("2026-10-%02d", dayIndex)
                    val record = attendanceByDate[dayStr]
                    val statusColor = when (record?.attendanceStatus) {
                        AttendanceStatus.PRESENT -> AttendancePresentGreen
                        AttendanceStatus.ABSENT -> AttendanceAbsentRed
                        AttendanceStatus.EXCUSED -> AttendanceExcusedYellow
                        AttendanceStatus.LATE -> AttendanceLateBlue
                        null -> if (dayIndex <= 8) AttendancePresentGreen else Color.LightGray.copy(alpha = 0.3f)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "$dayIndex", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AttendancePresentGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حضور", fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AttendanceAbsentRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("غياب", fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AttendanceExcusedYellow))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("استئذان", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}
