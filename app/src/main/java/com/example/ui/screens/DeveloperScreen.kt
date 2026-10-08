package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.components.InstallApkModalBottomSheet
import com.example.ui.components.ManageHalaqatDialog
import com.example.ui.components.SubscriberApprovalManagementDialog
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun DeveloperScreen(
    viewModel: QuranViewModel
) {
    val context = LocalContext.current

    val halaqat by viewModel.allHalaqat.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val records by viewModel.allDailyRecords.collectAsState()
    val events by viewModel.allScheduleEvents.collectAsState()
    val subscribers by viewModel.allSubscribers.collectAsState()
    val pendingSubscribersCount by viewModel.pendingSubscribersCount.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()

    var showManageHalaqatDialog by remember { mutableStateOf(false) }
    var showSubscriberApprovalDialog by remember { mutableStateOf(false) }
    var showInstallApkSheet by remember { mutableStateOf(false) }

    var canTeacherExportPdf by remember { mutableStateOf(true) }
    var canTeacherSendPush by remember { mutableStateOf(true) }
    var canTeacherScheduleCalendar by remember { mutableStateOf(true) }
    var canParentPayInvoices by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("developer_screen")
    ) {
        // Developer Banner
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "لَوْحَةُ تَحَكُّمِ الإِدَارَةِ وَالمُطَوِّرِ (RBAC) 🛡️",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "إدارة صلاحيات المستخدمين، فحص قاعدة بيانات Room، ومراقبة المزامنة الخلفية",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // DB Stats Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "إحصائيات قاعدة بيانات Room 📊", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${halaqat.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                Text(text = "حلقات", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${students.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                Text(text = "طلاب", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${records.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                Text(text = "تسميعات", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${events.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                Text(text = "مواعيد", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showManageHalaqatDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("manage_halaqat_dev_button")
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إدارة الحلقات والكوادر (إضافة، تعديل، حذف) 🏛️", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Subscriber Registration & Identity Verification Approval Card (Supervisor & Developer Authority)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("subscribers_approval_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "موافقة المشتركين وإثبات الهوية 🆔👑",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            if (pendingSubscribersCount > 0) {
                                Surface(
                                    color = Color(0xFFEF4444),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$pendingSubscribersCount قيد المراجعة ⏳",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "نظام التحقق المسبق: لا يستطيع أي مشترك الدخول أو استخدام التطبيق دون إثبات هوية رسمي وموافقة المشرف أو المطور.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showSubscriberApprovalDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("open_subscribers_approval_btn")
                        ) {
                            Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح لوحة تدقيق واعتماد المشتركين (${subscribers.size}) 📋", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // Direct Server APK Package Card
            item {
                val apkUrl = "https://ais-pre-hyedq3aw5ugaoci7a65pzg-567265308769.europe-west3.run.app/app-debug.apk"
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "حزمة التثبيت وروابط السيرفر المباشرة (APK) 📦",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Surface(
                                color = EmeraldPrimary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "جاهز 22 MB",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ملف APK مرفوع على السيرفر ومتاح للتحميل والتثبيت المباشر على جميع الأجهزة دون قيود.",
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
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تحميل APK 📥", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("APK Link", apkUrl)
                                    clipboard.setPrimaryClip(clip)
                                    showInstallApkSheet = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("مشاركة / نسخ 📋", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // RBAC Permissions Config Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "نظام إدارة الصلاحيات المتقدم (RBAC) 🔑",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("سماح للمعلمين بتصدير تقارير PDF", fontSize = 12.sp)
                            Switch(checked = canTeacherExportPdf, onCheckedChange = { canTeacherExportPdf = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("سماح بإرسال تنبيهات فورية (Push Notifications)", fontSize = 12.sp)
                            Switch(checked = canTeacherSendPush, onCheckedChange = { canTeacherSendPush = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("سماح للمعلمين بجدولة مواعيد وامتحانات", fontSize = 12.sp)
                            Switch(checked = canTeacherScheduleCalendar, onCheckedChange = { canTeacherScheduleCalendar = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تفعيل بوابة سداد الرسوم لأولياء الأمور", fontSize = 12.sp)
                            Switch(checked = canParentPayInvoices, onCheckedChange = { canParentPayInvoices = it })
                        }
                    }
                }
            }

            // Offline Sync Queue Details
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "حالة محرك المزامنة الخلفية (Offline Sync Queue) 🔄", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "الحالة الراهنة: ${syncStatus.arabicLabel}", fontSize = 12.sp, color = EmeraldDark)
                        Text(text = "العمليات المعلقة في قائمة الانتظار: $pendingSyncCount", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.triggerSyncNow() },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("مزامنة قسرية الآن ⚡")
                        }
                    }
                }
            }
        }
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

    if (showInstallApkSheet) {
        InstallApkModalBottomSheet(
            onDismiss = { showInstallApkSheet = false }
        )
    }
}
