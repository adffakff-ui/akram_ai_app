package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.HistoricalAttendanceCalendar
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun ParentPortalScreen(
    viewModel: QuranViewModel,
    onNavigateToChat: (Long) -> Unit = {}
) {
    val context = LocalContext.current

    val students by viewModel.allStudents.collectAsState()
    val dailyRecords by viewModel.allDailyRecords.collectAsState()
    val certificates by viewModel.allCertificates.collectAsState()
    val pushNotifications by viewModel.allPushNotifications.collectAsState()
    val invoices by viewModel.allInvoices.collectAsState()

    var parentInputCode by remember { mutableStateOf("1001") }
    var selectedStudentId by remember { mutableStateOf<Long?>(students.firstOrNull()?.id) }

    val activeStudent = remember(students, selectedStudentId, parentInputCode) {
        students.find { it.id == selectedStudentId || it.parentCode == parentInputCode }
            ?: students.firstOrNull()
    }

    val studentRecords = remember(dailyRecords, activeStudent) {
        if (activeStudent != null) dailyRecords.filter { it.studentId == activeStudent.id } else emptyList()
    }
    val studentCerts = remember(certificates, activeStudent) {
        if (activeStudent != null) certificates.filter { it.studentId == activeStudent.id } else emptyList()
    }
    val studentAlerts = remember(pushNotifications, activeStudent) {
        if (activeStudent != null) pushNotifications.filter { it.studentId == activeStudent.id } else emptyList()
    }
    val studentInvoices = remember(invoices, activeStudent) {
        if (activeStudent != null) invoices.filter { it.studentId == activeStudent.id } else emptyList()
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Progress & Recharts, 1 = Alerts & Notes, 2 = Invoices & Fees, 3 = Certificates

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("parent_portal_screen")
    ) {
        // Child Selector & Welcome Banner
        Surface(
            color = EmeraldDark,
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = GoldPrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "بَوَّابَةُ وَلِيِّ الأَمْرِ الذَّكِيَّةِ 👨‍👧‍👦",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "متابعة الطالب: ${activeStudent?.name ?: "—"}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Student Switcher
                    if (students.size > 1) {
                        IconButton(onClick = {
                            val next = students.firstOrNull { it.id != selectedStudentId }
                            if (next != null) selectedStudentId = next.id
                        }) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "تبديل الابن", tint = GoldPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("المستوى والمخطط 📊", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("تنبيهات المعلم 🔔 (${studentAlerts.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("رسوم الحلقات 💳", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("الشهادات 📜 (${studentCerts.size})", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeStudent == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد بيانات متاحة للطالب")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: Progress, Recharts comparison chart, and attendance
                        item {
                            // Recharts Monthly Comparison Bar Chart for Parent
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "مقارنة الحفظ الفعلي بالأهداف الشهرية (Recharts) 📈",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            color = EmeraldPrimary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "هدف: ${activeStudent.memorizationGoalPagesPerDay.toInt()} ص/يوم",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Mini comparison bars
                                    val months = listOf("مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر")
                                    val actual = listOf(22, 28, 30, 26, 32, 29)
                                    val target = listOf(25, 25, 25, 25, 25, 25)

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        months.forEachIndexed { i, m ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = m, fontSize = 11.sp, modifier = Modifier.width(45.dp), color = Color.Gray)

                                                Box(modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                                    // Actual bar
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxHeight()
                                                            .fillMaxWidth(fraction = (actual[i] / 40f).coerceIn(0f, 1f))
                                                            .background(if (actual[i] >= target[i]) ChartEmerald else ChartAmber)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${actual[i]} / ${target[i]} ص",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (actual[i] >= target[i]) ChartEmerald else ChartAmber
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Historical Attendance Card
                        item {
                            HistoricalAttendanceCalendar(
                                student = activeStudent,
                                records = studentRecords
                            )
                        }

                        // Export PDF Button
                        item {
                            Button(
                                onClick = { viewModel.exportStudentReportPdf(context, activeStudent) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("parent_export_pdf_button")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تصدير تقرير أداء الطالب بصيغة PDF للطباعة 📄")
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: Push Alerts from Teacher
                        if (studentAlerts.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد تنبيهات جديدة من المعلم", color = Color.Gray)
                                }
                            }
                        } else {
                            items(studentAlerts, key = { it.id }) { alert ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = alert.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = alert.notificationType.arabicTitle, fontSize = 10.sp, color = GoldPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = alert.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "المحفظ: ${alert.senderTeacherName}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: Tuition Fees Payment Gateway
                        item {
                            Text(
                                text = "فواتير وسداد رسوم الحلقات القرآنية 💳",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldDark
                            )
                        }
                        if (studentInvoices.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                        Text("جميع الرسوم مسددة بالكامل ولا توجد فواتير معلقة ✅", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            items(studentInvoices, key = { it.id }) { inv ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = inv.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(text = "استحقاق: ${inv.dueDate}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Text(
                                                text = "${inv.amount.toInt()} ر.س",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = EmeraldPrimary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        if (inv.isPaid) {
                                            Surface(
                                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(text = "تم السداد بنجاح بتاريخ ${inv.paidDate} عبر ${inv.paymentMethod}", fontSize = 11.sp, color = EmeraldPrimary)
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = { viewModel.payInvoice(inv) },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("pay_invoice_button_${inv.id}")
                                            ) {
                                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("سداد الفاتورة الآن (مدى / Apple Pay) 💳", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: Digital Certificates & PDF Export
                        if (studentCerts.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد شهادات رقمية صادرة حتى الآن", color = Color.Gray)
                                }
                            }
                        } else {
                            items(studentCerts, key = { it.id }) { cert ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = cert.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldDark)
                                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = cert.reason, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "تاريخ الإصدار: ${cert.issueDate} • ${cert.teacherName}", fontSize = 11.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.exportCertificatePdf(context, cert) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("تحميل الشهادة بصيغة PDF 📜", fontSize = 11.sp)
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
