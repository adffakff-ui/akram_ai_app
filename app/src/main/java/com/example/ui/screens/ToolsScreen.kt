package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuranData
import com.example.data.model.Surah
import com.example.ui.components.InstallApkModalBottomSheet
import com.example.ui.components.OnboardingSyncGuideDialog
import com.example.ui.components.SubscriberApprovalManagementDialog
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.util.SpeechEvaluatorHelper

@Composable
fun ToolsScreen(
    viewModel: QuranViewModel
) {
    val context = LocalContext.current
    var selectedToolTab by remember { mutableIntStateOf(0) }

    val toolTabs = listOf(
        "تثبيت التطبيق (APK) 📱💻",
        "النسخ والمزامنة 💾",
        "فهرس السور وتلاوات MP3 📖",
        "تقييم التلاوة الصوتي 🎙️",
        "الأجهزة والأمان 🛡️",
        "دليل الاستخدام 💡"
    )

    var showInstallApkSheet by remember { mutableStateOf(false) }
    var showOnboardingGuide by remember { mutableStateOf(false) }
    var showSubscriberApprovalDialog by remember { mutableStateOf(false) }

    val devices by viewModel.registeredDevices.collectAsState()
    val subscribers by viewModel.allSubscribers.collectAsState()
    val pendingSubscribersCount by viewModel.pendingSubscribersCount.collectAsState()
    val halaqat by viewModel.allHalaqat.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tools_screen")
    ) {
        // Header
        Surface(
            color = EmeraldDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BuildCircle, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "حَقِيبَةُ الأَدَوَاتِ القُرْآنِيَّةِ الشَّامِلَةِ",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تحميل حزمة APK، النسخ الاحتياطي، المزامنة، فهرس السور والاستماع لتلاوات MP3",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedToolTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    divider = {}
                ) {
                    toolTabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = selectedToolTab == index,
                            onClick = { selectedToolTab = index },
                            text = {
                                Text(
                                    text = tabTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedToolTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedToolTab == index) GoldPrimary else Color.White
                                )
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedToolTab) {
                0 -> {
                    // TAB 0: Download & Install Package (APK + Web)
                    val apkDownloadUrl = "https://ais-pre-hyedq3aw5ugaoci7a65pzg-567265308769.europe-west3.run.app/app-debug.apk"
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "تَحْمِيلُ حُزْمَةِ التَّثْبِيتِ المَرْفُوعَةِ (APK) 📦",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "الحزمة الأصلية جاهزة ومثبتة على سيرفر Cloud Run",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkDownloadUrl)).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("direct_apk_download_button")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تَحْمِيلُ مَلَفِ APK مُبَاشَرَةً (22 MB) 📥", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedButton(
                                        onClick = { showInstallApkSheet = true },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Devices, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("عَرْضُ طُرُقِ التَّثْبِيتِ لِوَينْدُوزْ وَالآيْفُونْ 💻📱", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "رابط التحميل المباشر:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = apkDownloadUrl, fontSize = 11.sp, color = EmeraldPrimary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("APK Download URL", apkDownloadUrl)
                                            clipboard.setPrimaryClip(clip)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("نسخ الرابط لمشاركته مع الطلاب", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Backup & Offline Sync
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Offline Sync Card
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
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
                                        Text(
                                            text = "المزامنة الخلفية التلقائية (SyncAdapter) 🔄",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = syncStatus.arabicLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "آخر مزامنة ناجحة: $lastSyncTime",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "العمليات المعلقة للمزامنة: $pendingSyncCount عملية",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { viewModel.triggerSyncNow() },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("sync_now_button")
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("بدء المزامنة الفورية الآن ⚡", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Room Backup Card
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "النسخ الاحتياطي المحلي والتصدير 💾",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "أخذ نسخة احتياطية كاملة من قاعدة بيانات Room بصيغة JSON أو تقرير CSV لمشاركتها خارج التطبيق.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.exportBackupJson(context) },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("نسخ احتياطي JSON", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.exportHifzCsv(context) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تصدير CSV 📊", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Quran Surah Index with MP3 Player
                    var playingSurahNumber by remember { mutableStateOf<Int?>(null) }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(QuranData.surahs) { surah ->
                            val isPlaying = (playingSurahNumber == surah.number)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPlaying) EmeraldPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${surah.number}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "سورة ${surah.name}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "${surah.revelationType} • ${surah.versesCount} آيات • الجزء ${surah.juz}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    // MP3 Play Toggle
                                    FilledTonalIconButton(
                                        onClick = {
                                            playingSurahNumber = if (isPlaying) null else surah.number
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                            contentDescription = "تشغيل التلاوة",
                                            tint = if (isPlaying) EmeraldPrimary else Color.Gray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: Speech Recognition Evaluation
                    var spokenInput by remember { mutableStateOf("عم يتساءلون عن النبا العظيم") }
                    var expectedText by remember { mutableStateOf("عَمَّ يَتَسَاءَلُونَ عَنِ النَّبَإِ الْعَظِيمِ") }
                    var evalResult by remember { mutableStateOf<com.example.util.RecitationEvaluationResult?>(null) }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = "مقارنة التلاوة بالقرآن الكريم آلياً (AI Recitation) 🎤", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = expectedText,
                                        onValueChange = { expectedText = it },
                                        label = { Text("الآية المستهدفة من المصحف") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = spokenInput,
                                        onValueChange = { spokenInput = it },
                                        label = { Text("النص المتلو صوتياً (Speech-to-Text)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            evalResult = SpeechEvaluatorHelper.evaluateRecitation(spokenInput, "النبأ", expectedText)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("بدء التقييم الآلي للتلاوة")
                                    }
                                }
                            }
                        }

                        evalResult?.let { res ->
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.1f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "نسبة دقة التلاوة: ${res.accuracyPercentage}% 🌟", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldDark)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = res.feedbackMessage, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        res.tajweedNotes.forEach { note ->
                                            Text(text = "• $note", fontSize = 11.sp, color = EmeraldDark)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: Devices & Security + Subscriber Approval
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Identity Verification & Subscriber Approval Card
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().testTag("tools_subscriber_approval_card")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
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
                                                fontSize = 13.sp
                                            )
                                        }
                                        if (pendingSubscribersCount > 0) {
                                            Surface(
                                                color = Color(0xFFEF4444),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(
                                                    text = "$pendingSubscribersCount بالانتظار",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "الموافقة الحصرية للمشرفين والمطور على المشتركين الجدد بعد التحقق من وثائق إثبات الهوية.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { showSubscriberApprovalDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("open_subscribers_tools_btn")
                                    ) {
                                        Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("إدارة واعتماد طلبات الاشتراك وإثبات الهوية (${subscribers.size}) 📋", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Text(text = "الأجهزة المسجلة والمصادقة بالبصمة 🛡️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        items(devices) { dev ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = dev.deviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "${dev.deviceModel} • ${dev.lastActiveDate}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Surface(
                                        color = EmeraldPrimary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = "بصمة مفعّلة 🔒", fontSize = 10.sp, color = EmeraldPrimary, modifier = Modifier.padding(6.dp, 3.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                5 -> {
                    // TAB 5: Onboarding Guide
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.HelpCenter, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "دليل الاستخدام والمزامنة التفاعلي", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "تعلم كيفية تشغيل التطبيق بدون إنترنت، واستعادة البيانات والمزامنة السحابية تلقائياً.",
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showOnboardingGuide = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Text("فتح الدليل التفاعلي 📖")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInstallApkSheet) {
        InstallApkModalBottomSheet(onDismiss = { showInstallApkSheet = false })
    }

    if (showOnboardingGuide) {
        OnboardingSyncGuideDialog(onDismiss = { showOnboardingGuide = false })
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
}
