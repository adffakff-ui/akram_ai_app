package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.util.TtsSummaryHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HalaqatScreen(
    viewModel: QuranViewModel,
    onNavigateToChat: (Long) -> Unit = {}
) {
    val context = LocalContext.current

    val currentRole by viewModel.currentRole.collectAsState()
    val halaqat by viewModel.allHalaqat.collectAsState()
    val visibleHalaqat by viewModel.halaqatForCurrentRole.collectAsState()
    val currentTeacherHalaqahId by viewModel.currentTeacherHalaqahId.collectAsState()
    val students by viewModel.filteredStudents.collectAsState()
    val dailyRecords by viewModel.allDailyRecords.collectAsState()
    val scheduleEvents by viewModel.allScheduleEvents.collectAsState()
    val subscribers by viewModel.allSubscribers.collectAsState()
    val pendingSubscribersCount by viewModel.pendingSubscribersCount.collectAsState()

    val selectedHalaqahId by viewModel.selectedHalaqahId.collectAsState()
    val genderFilter by viewModel.genderFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsState()

    // Sub-view: 0 = Students & Halaqat, 1 = Recharts Dashboard, 2 = Calendar
    var currentSubView by remember { mutableIntStateOf(0) }

    // Dialog States
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var activeStudentForRecord by remember { mutableStateOf<Student?>(null) }
    var activeStudentForPush by remember { mutableStateOf<Student?>(null) }
    var activeStudentForAttendance by remember { mutableStateOf<Student?>(null) }
    var activeStudentForChallenge by remember { mutableStateOf<Student?>(null) }
    var showScheduleEventDialog by remember { mutableStateOf(false) }
    var showInstallApkSheet by remember { mutableStateOf(false) }
    var showManageHalaqatDialog by remember { mutableStateOf(false) }
    var showSubscriberApprovalDialog by remember { mutableStateOf(false) }
    var showSwitchTeacherDialog by remember { mutableStateOf(false) }

    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayRecords = remember(dailyRecords) { dailyRecords.filter { it.date == todayDateStr } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("halaqat_screen")
    ) {
        // Teacher Sub-View Segmented Tabs
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tab 1: Students & Halaqat
                FilterChip(
                    selected = currentSubView == 0,
                    onClick = { currentSubView = 0 },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("قائمة الحلقات والطلاب", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f).testTag("tab_students_list")
                )

                // Tab 2: Recharts Analytics Dashboard
                FilterChip(
                    selected = currentSubView == 1,
                    onClick = { currentSubView = 1 },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (currentSubView == 1) EmeraldPrimary else Color.Gray
                        )
                    },
                    label = { Text("لوحة الرسوم البيانية 📊", fontSize = 12.sp) },
                    modifier = Modifier.weight(1.1f).testTag("tab_recharts_dashboard")
                )

                // Tab 3: Interactive Calendar
                FilterChip(
                    selected = currentSubView == 2,
                    onClick = { currentSubView = 2 },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (currentSubView == 2) EmeraldPrimary else Color.Gray
                        )
                    },
                    label = { Text("التقويم والمواعيد 📅", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f).testTag("tab_interactive_calendar")
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (currentSubView) {
                0 -> {
                    // TAB 0: Students & Halaqat List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Role Permission & Governance Banner (Supervisor vs Teacher Isolation)
                        item {
                            val activeTeacherHalaqah = halaqat.find {
                                it.id == (currentTeacherHalaqahId ?: halaqat.firstOrNull()?.id)
                            }

                            if (currentRole == UserRole.TEACHER) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = EmeraldDark),
                                    modifier = Modifier.fillMaxWidth().testTag("teacher_role_isolated_banner")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "صلاحية المعلم الخاصة 🔒: ${activeTeacherHalaqah?.name ?: "حلقة التحفيظ"}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "المحفظ: ${activeTeacherHalaqah?.responsibleSheikh ?: "أنت"} | محصورة بحلقتك فقط ولا تطلع على الحلقات الأخرى",
                                                    fontSize = 11.sp,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                        }

                                        if (halaqat.size > 1) {
                                            TextButton(
                                                onClick = { showSwitchTeacherDialog = true },
                                                colors = ButtonDefaults.textButtonColors(contentColor = GoldPrimary)
                                            ) {
                                                Text("تبديل المعلم", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            } else if (currentRole == UserRole.SUPERVISOR || currentRole == UserRole.DEVELOPER) {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier.fillMaxWidth().testTag("supervisor_role_banner")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = if (currentRole == UserRole.SUPERVISOR) "صلاحية المشرف العام (إشراف شامل) 👑" else "صلاحية المطور التقني (إدارة كاملة) 🛡️",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "اطلاع كامل على كافة الحلقات (${halaqat.size}) وكافة الطلاب مع صلاحية الإضافة والحذف والتعديل",
                                                    fontSize = 11.sp,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { showSubscriberApprovalDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.testTag("halaqat_approval_btn")
                                            ) {
                                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (pendingSubscribersCount > 0) "الموافقات ($pendingSubscribersCount) ⏳" else "المشتركين 🆔",
                                                    fontSize = 11.sp,
                                                    color = EmeraldDark,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Button(
                                                onClick = { showManageHalaqatDialog = true },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("إدارة الحلقات 🏛️", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Direct APK Download & Package Banner (Server hosted APK)
                        item {
                            val apkUrl = "https://ais-pre-hyedq3aw5ugaoci7a65pzg-567265308769.europe-west3.run.app/app-debug.apk"
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(22.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "حُزْمَةُ التَّثْبِيتِ وَالتَّحْمِيلِ المَرْفُوعَةِ بِالسِّيرْفَرِ 📦📱",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Surface(
                                            color = EmeraldPrimary,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "APK 22 MB",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "التطبيق مرفوع وجاهز للتحميل والتثبيت المباشر على جميع الهواتف والأجهزة اللوحية والكمبيوتر.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f).testTag("direct_apk_quick_button")
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تحميل APK المباشر 📥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("APK Download Link", apkUrl)
                                                clipboard.setPrimaryClip(clip)
                                                showInstallApkSheet = true
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp), tint = EmeraldPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("نسخ الرابط / التفاصيل 📋", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Daily Status Snapshot Widget
                        item {
                            DailyStatusSnapshotWidget(
                                students = students,
                                todayRecords = todayRecords,
                                onSendFollowUpAlerts = { pendingList ->
                                    pendingList.forEach { s ->
                                        viewModel.sendPushNotificationToStudent(
                                            student = s,
                                            title = "تذكير بحلقة القرآن الكريم اليوم ⏰",
                                            message = "السلام عليكم ${s.name}، ننتظر حضورك المتميز وتسميع وردك القرآني اليوم.",
                                            type = NotificationType.ATTENDANCE,
                                            postSystemNotification = true
                                        )
                                    }
                                }
                            )
                        }

                        // Search & Filters Row
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    placeholder = { Text("بحث عن طالب، سورة، أو ملاحظة...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon = {
                                        if (searchQuery.isNotBlank()) {
                                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("student_search_input")
                                )

                                // Halaqat Chips (Restricted for Teacher, Global for Supervisor/Developer)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (currentRole == UserRole.TEACHER) {
                                        // Teacher sees only their own assigned halaqah
                                        items(visibleHalaqat) { h ->
                                            FilterChip(
                                                selected = true,
                                                onClick = { },
                                                label = { Text("حلقتك: ${h.name} (${students.size}) 🔒") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                                                }
                                            )
                                        }
                                    } else {
                                        // Supervisor / Developer see all halaqat
                                        item {
                                            FilterChip(
                                                selected = selectedHalaqahId == null,
                                                onClick = { viewModel.setSelectedHalaqah(null) },
                                                label = { Text("جميع الحلقات (${students.size})") }
                                            )
                                        }
                                        items(halaqat) { h ->
                                            FilterChip(
                                                selected = selectedHalaqahId == h.id,
                                                onClick = { viewModel.setSelectedHalaqah(h.id) },
                                                label = { Text(h.name) }
                                            )
                                        }
                                    }
                                }

                                // Actions Bar (Add Student + Export CSV + Download APK)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { showAddStudentDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("add_student_button")
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("إضافة طالب", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.exportHifzCsv(context) },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تصدير CSV 📊", fontSize = 11.sp)
                                        }
                                    }

                                    // Direct APK button
                                    FilledTonalButton(
                                        onClick = { showInstallApkSheet = true },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("install_apk_button_main")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تثبيت التطبيق 📱", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Students Cards
                        if (students.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("لا يوجد طلاب مطابقين للبحث", color = Color.Gray, fontSize = 14.sp)
                                    }
                                }
                            }
                        } else {
                            items(students, key = { it.id }) { student ->
                                val hName = halaqat.find { it.id == student.halaqahId }?.name ?: "الحلقة القرآنية"
                                StudentCard(
                                    student = student,
                                    halaqahName = hName,
                                    onRecordClick = { activeStudentForRecord = student },
                                    onSendPushClick = { activeStudentForPush = student },
                                    onChallengeClick = { activeStudentForChallenge = student },
                                    onAttendanceClick = { activeStudentForAttendance = student },
                                    onExportPdfClick = { viewModel.exportStudentReportPdf(context, student) },
                                    onTtsPlayClick = {
                                        val studentToday = todayRecords.find { it.studentId == student.id }
                                        val summary = TtsSummaryHelper.generateDailySummaryArabic(student, studentToday)
                                        viewModel.sendPushNotificationToStudent(
                                            student = student,
                                            title = "الملخص الصوتي اليومي 🔊",
                                            message = summary,
                                            type = NotificationType.HIFZ_ENCOURAGEMENT,
                                            postSystemNotification = true
                                        )
                                    },
                                    onEditClick = { editingStudent = student },
                                    onDeleteClick = { viewModel.deleteStudent(student) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Recharts Interactive Graphical Dashboard
                    RechartsDashboardView(
                        students = students,
                        records = dailyRecords,
                        halaqat = visibleHalaqat
                    )
                }

                2 -> {
                    // TAB 2: Interactive Compose Calendar
                    InteractiveCalendarView(
                        events = scheduleEvents,
                        selectedDate = selectedCalendarDate,
                        onSelectDate = { viewModel.setSelectedCalendarDate(it) },
                        onAddEventClick = { showScheduleEventDialog = true },
                        onDeleteEvent = { viewModel.deleteScheduleEvent(it) },
                        onToggleCompleted = { id, comp -> viewModel.toggleEventCompleted(id, comp) },
                        onSendEventReminder = { event ->
                            students.forEach { s ->
                                if (event.halaqahId == null || s.halaqahId == event.halaqahId) {
                                    viewModel.sendPushNotificationToStudent(
                                        student = s,
                                        title = "تذكير بموعد: ${event.title} 📅",
                                        message = "نذكركم بموعد ${event.eventType.arabicTitle} بتاريخ ${event.date} الساعة ${event.startTime} في ${event.location}. ${if (event.targetSurahOrJuz.isNotBlank()) "المقرر: " + event.targetSurahOrJuz else ""}",
                                        type = if (event.eventType == ScheduleEventType.EXAM) NotificationType.EXAM_PREPARATION else NotificationType.GENERAL,
                                        postSystemNotification = true
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddStudentDialog) {
        StudentDialog(
            halaqat = visibleHalaqat,
            onDismiss = { showAddStudentDialog = false },
            onSaveStudent = { viewModel.addStudent(it) }
        )
    }

    editingStudent?.let { s ->
        StudentDialog(
            initialStudent = s,
            halaqat = visibleHalaqat,
            onDismiss = { editingStudent = null },
            onSaveStudent = { viewModel.updateStudent(it) }
        )
    }

    activeStudentForRecord?.let { s ->
        DailyRecordDialog(
            student = s,
            onDismiss = { activeStudentForRecord = null },
            onSaveRecord = { viewModel.recordDailyEvaluation(it, s) }
        )
    }

    activeStudentForPush?.let { s ->
        SendPushNotificationDialog(
            student = s,
            onDismiss = { activeStudentForPush = null },
            onSendNotification = { title, msg, type, postSys ->
                viewModel.sendPushNotificationToStudent(s, title, msg, type, postSys)
            }
        )
    }

    activeStudentForChallenge?.let { s ->
        DailyChallengeDialog(
            student = s,
            onDismiss = { activeStudentForChallenge = null },
            onChallengeCompleted = { bonus ->
                val updated = s.copy(totalPoints = s.totalPoints + bonus)
                viewModel.updateStudent(updated)
            }
        )
    }

    activeStudentForAttendance?.let { s ->
        AlertDialog(
            onDismissRequest = { activeStudentForAttendance = null },
            confirmButton = {
                TextButton(onClick = { activeStudentForAttendance = null }) {
                    Text("إغلاق")
                }
            },
            text = {
                HistoricalAttendanceCalendar(
                    student = s,
                    records = dailyRecords
                )
            }
        )
    }

    if (showScheduleEventDialog) {
        ScheduleEventDialog(
            initialDate = selectedCalendarDate,
            halaqat = halaqat,
            onDismiss = { showScheduleEventDialog = false },
            onSaveEvent = { viewModel.addScheduleEvent(it) }
        )
    }

    if (showManageHalaqatDialog) {
        ManageHalaqatDialog(
            halaqat = halaqat,
            onDismiss = { showManageHalaqatDialog = false },
            onAddHalaqah = { viewModel.addHalaqah(it) },
            onUpdateHalaqah = { viewModel.updateHalaqah(it) },
            onDeleteHalaqah = { viewModel.deleteHalaqah(it) }
        )
    }

    if (showSubscriberApprovalDialog) {
        SubscriberApprovalManagementDialog(
            subscribers = subscribers,
            halaqat = halaqat,
            currentRole = currentRole,
            onDismiss = { showSubscriberApprovalDialog = false },
            onApprove = { subscriber, approver ->
                viewModel.approveSubscriber(subscriber, approver)
            },
            onReject = { subscriber, reason ->
                viewModel.rejectSubscriber(subscriber, reason)
            },
            onDelete = { subscriber ->
                viewModel.deleteSubscriber(subscriber)
            },
            onAddNewSubscriber = { fullName, nationalId, phone, requestedRole, halaqahId, halaqahName, docType, docNum, notes ->
                viewModel.submitNewSubscriberApplication(
                    fullName, nationalId, phone, requestedRole, halaqahId, halaqahName, docType, docNum, notes
                )
            }
        )
    }

    if (showSwitchTeacherDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchTeacherDialog = false },
            title = {
                Text("تحديد هوية المعلم وحلقته 🔒", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "اختر حلقتك لتسجيل الدخول كمعلم. ستكون صلاحيتك محصورة بهذه الحلقة فقط:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    halaqat.forEach { h ->
                        OutlinedButton(
                            onClick = {
                                viewModel.setTeacherHalaqahId(h.id)
                                showSwitchTeacherDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                Text(h.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldDark)
                                Text("المحفظ: ${h.responsibleSheikh}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchTeacherDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showInstallApkSheet) {
        InstallApkModalBottomSheet(
            onDismiss = { showInstallApkSheet = false }
        )
    }
}
