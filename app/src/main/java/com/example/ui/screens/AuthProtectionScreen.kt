package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SubscriberApprovalStatus
import com.example.data.model.SubscriberRegistration
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthProtectionScreen(
    viewModel: QuranViewModel
) {
    val subscribers by viewModel.allSubscribers.collectAsState()
    val halaqat by viewModel.allHalaqat.collectAsState()
    val pendingCount by viewModel.pendingSubscribersCount.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubscriber by remember { mutableStateOf<SubscriberRegistration?>(null) }
    var showNewApplicationDialog by remember { mutableStateOf(false) }
    var showAdminLoginPrompt by remember { mutableStateOf(false) }
    var adminRoleTarget by remember { mutableStateOf(UserRole.SUPERVISOR) }
    var adminPinCode by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("auth_protection_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top Hero Header
            Surface(
                color = EmeraldDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .border(2.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "حماية المنظومة",
                            tint = GoldPrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "بَوَّابَةُ المُصَادَقَةِ وَالحِمَايَةِ الذَّكِيَّةِ 🛡️",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "مَنَارَةُ القُرْآنِ الكَرِيمِ • إدارة حالات المشتركين وإثبات الهوية",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "الحالات المعتمدة: معلق (Pending) ⏳ | مقبول (Approved) ✅",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Subscriber Identity Verification & Status Check Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "التحقق من الهوية وحالة الاشتراك (معلق / مقبول):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "أدخل رقم الهوية الوطنية أو الإقامة أو رقم الجوال للتحقق من اعتماد المشترك:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                                selectedSubscriber = subscribers.find { sub ->
                                    sub.nationalIdOrPassport.contains(query, ignoreCase = true) ||
                                            sub.fullName.contains(query, ignoreCase = true) ||
                                            sub.phone.contains(query, ignoreCase = true)
                                }
                            },
                            label = { Text("رقم الهوية الوطنية / الإقامة / الجوال") },
                            placeholder = { Text("مثال: 1098765432 أو 0501234567") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        selectedSubscriber = null
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_search_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick demo buttons for testing pending vs approved
                        Text(
                            text = "حسابات تجريبية للمعاينة السريعة:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(subscribers.take(5)) { sub ->
                                val isPending = sub.approvalStatus == SubscriberApprovalStatus.PENDING
                                val isApproved = sub.approvalStatus == SubscriberApprovalStatus.APPROVED
                                val chipColor = when {
                                    isPending -> Color(0xFFF59E0B)
                                    isApproved -> EmeraldPrimary
                                    else -> Color(0xFFEF4444)
                                }

                                FilterChip(
                                    selected = selectedSubscriber?.id == sub.id,
                                    onClick = {
                                        selectedSubscriber = sub
                                        searchQuery = sub.nationalIdOrPassport
                                    },
                                    label = {
                                        Text(
                                            text = "${sub.fullName.split(" ").first()} (${sub.approvalStatus.name.lowercase()})",
                                            fontSize = 11.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = chipColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = chipColor.copy(alpha = 0.2f),
                                        selectedLabelColor = chipColor
                                    )
                                )
                            }
                        }

                        // Status Result Card
                        AnimatedVisibility(
                            visible = selectedSubscriber != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            selectedSubscriber?.let { sub ->
                                Column(modifier = Modifier.padding(top = 14.dp)) {
                                    SubscriberStatusResultCard(
                                        subscriber = sub,
                                        onEnterDashboard = {
                                            viewModel.authenticateSubscriber(sub)
                                        },
                                        onRefreshStatus = {
                                            selectedSubscriber = subscribers.find { it.id == sub.id }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Submit New Application Button
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = EmeraldPrimary.copy(alpha = 0.05f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary.copy(alpha = 0.4f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = EmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "مشترك جديد؟ قدّم إثبات الهوية الآن 📝",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = EmeraldDark
                                )
                                Text(
                                    text = "يتم إدراج طلبك كـ (معلق / Pending) حتى اعتماده من الإدارة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { showNewApplicationDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_subscriber_application_button")
                        ) {
                            Icon(Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تقديم طلب اشتراك وإثبات هوية جديد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Section 3: Admin & Developer Super Access
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "دخول المشرف والمطور (إدارة واعتماد الحالات):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (pendingCount > 0) {
                                Surface(
                                    color = Color(0xFFF59E0B),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$pendingCount معلق",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "للمشرفين والمطورين صلاحية اعتماد أو رفض طلبات المشتركين وتوثيق الهويات:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    adminRoleTarget = UserRole.SUPERVISOR
                                    showAdminLoginPrompt = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_supervisor_button")
                            ) {
                                Icon(Icons.Default.SupervisorAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("المشرف العام 👑", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    adminRoleTarget = UserRole.DEVELOPER
                                    showAdminLoginPrompt = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_developer_button")
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("المطور التقني 💻", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section 4: Guest Preview Option
                TextButton(
                    onClick = { viewModel.passAuthGateAsGuest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guest_bypass_button")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الدخول في وضع المعاينة العامة (الزائر) 👁️",
                        color = EmeraldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Modal: New Subscriber Registration
    if (showNewApplicationDialog) {
        NewSubscriberApplicationDialog(
            halaqat = halaqat,
            onDismiss = { showNewApplicationDialog = false },
            onSubmit = { name, nationalId, phone, role, halaqahId, halaqahName, docType, docNum, notes ->
                viewModel.submitNewSubscriberApplication(
                    fullName = name,
                    nationalId = nationalId,
                    phone = phone,
                    requestedRole = role,
                    halaqahId = halaqahId,
                    halaqahName = halaqahName,
                    documentType = docType,
                    documentNumber = docNum,
                    verificationNotes = notes
                )
                showNewApplicationDialog = false
                searchQuery = nationalId
            }
        )
    }

    // Modal: Admin Quick Login Confirmation
    if (showAdminLoginPrompt) {
        AlertDialog(
            onDismissRequest = {
                showAdminLoginPrompt = false
                adminPinCode = ""
                pinError = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (adminRoleTarget == UserRole.SUPERVISOR) Icons.Default.SupervisorAccount else Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (adminRoleTarget == UserRole.SUPERVISOR) "مصادقة المشرف العام" else "مصادقة المطور التقني",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "يتيح هذا الدخول كامل الصلاحيات لإدارة الحلقات واعتماد طلبات المشتركين وتوثيق الهويات.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPinCode,
                        onValueChange = {
                            adminPinCode = it
                            pinError = false
                        },
                        label = { Text("رمز التحقق السريع (اختياري / انقر تأكيد)") },
                        placeholder = { Text("رمز المشرف أو المطور") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.authenticateAsAdmin(adminRoleTarget)
                        showAdminLoginPrompt = false
                        adminPinCode = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (adminRoleTarget == UserRole.SUPERVISOR) EmeraldPrimary else Color(0xFF1E293B)
                    )
                ) {
                    Text("تأكيد الدخول المباشر ✓", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminLoginPrompt = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SubscriberStatusResultCard(
    subscriber: SubscriberRegistration,
    onEnterDashboard: () -> Unit,
    onRefreshStatus: () -> Unit
) {
    val isPending = subscriber.approvalStatus == SubscriberApprovalStatus.PENDING
    val isApproved = subscriber.approvalStatus == SubscriberApprovalStatus.APPROVED
    val isRejected = subscriber.approvalStatus == SubscriberApprovalStatus.REJECTED

    val cardBg = when {
        isApproved -> EmeraldPrimary.copy(alpha = 0.12f)
        isPending -> Color(0xFFF59E0B).copy(alpha = 0.12f)
        else -> Color(0xFFEF4444).copy(alpha = 0.12f)
    }

    val borderColor = when {
        isApproved -> EmeraldPrimary
        isPending -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Surface(
        color = cardBg,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status Header Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(borderColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isApproved -> Icons.Default.Verified
                                isPending -> Icons.Default.HourglassTop
                                else -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            tint = borderColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = when {
                                isApproved -> "حالة الحساب: مَقْبُول وَمُعْتَمَد (Approved) ✅"
                                isPending -> "حالة الحساب: مُعَلَّق (Pending) ⏳"
                                else -> "حالة الحساب: مَرْفُوض (Rejected) ❌"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = borderColor
                        )
                        Text(
                            text = when {
                                isApproved -> "تمت المصادقة واعتماد الهوية بنجاح"
                                isPending -> "قيد تدقيق الهوية ومراجعة المشرف أو المطور"
                                else -> "لم يستوفِ شروط إثبات الهوية"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onRefreshStatus) {
                    Icon(Icons.Default.Refresh, contentDescription = "تحديث الحالة", tint = borderColor)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = borderColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Details List
            DetailRow(label = "الاسم الكامل:", value = subscriber.fullName)
            DetailRow(label = "رقم الهوية الوطنية/الإقامة:", value = subscriber.nationalIdOrPassport)
            DetailRow(label = "نوع إثبات الهوية:", value = subscriber.identityDocumentType)
            DetailRow(label = "الدور المطلوب:", value = subscriber.requestedRole.arabicTitle)
            if (subscriber.halaqahName.isNotBlank()) {
                DetailRow(label = "الحلقة القرآنية:", value = subscriber.halaqahName)
            }
            DetailRow(label = "تاريخ التقديم:", value = subscriber.registrationDate.ifBlank { "اليوم" })

            if (isApproved) {
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow(label = "معتمد بواسطة:", value = subscriber.approvedBy.ifBlank { "المشرف العام" })
                if (subscriber.approvalDate.isNotBlank()) {
                    DetailRow(label = "تاريخ الاعتماد:", value = subscriber.approvalDate)
                }
            }

            if (isRejected && subscriber.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow(label = "سبب الرفض:", value = subscriber.rejectionReason)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            if (isApproved) {
                Button(
                    onClick = onEnterDashboard,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enter_dashboard_approved_button")
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الدخول إلى لوحة المنظومة والحلقات 🚀",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else if (isPending) {
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "طلبك قيد المراجعة؛ سيتم فتح اللوحة تلقائياً فور اعتماد المشرف أو المطور لحسابك.",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSubscriberApplicationDialog(
    halaqat: List<com.example.data.model.Halaqah>,
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        nationalId: String,
        phone: String,
        role: UserRole,
        halaqahId: Long?,
        halaqahName: String,
        docType: String,
        docNum: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var selectedHalaqahId by remember { mutableStateOf<Long?>(halaqat.firstOrNull()?.id) }
    var documentType by remember { mutableStateOf("بطاقة الهوية الوطنية") }
    var documentNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var hasAttachedIdProof by remember { mutableStateOf(false) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    val docTypes = listOf("بطاقة الهوية الوطنية", "الإقامة النظامية", "جواز السفر", "شهادة الميلاد")
    val roles = listOf(UserRole.STUDENT, UserRole.PARENT, UserRole.TEACHER)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("new_subscriber_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Surface(
                    color = EmeraldDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "طلب اشتراك جديد وإثبات هوية 📝",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "املأ البيانات أدناه لتقديم طلب الاشتراك، وستكون الحالة مبدئياً (معلق / Pending) حتى اعتماد المشرف أو المطور:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الرباعي الكامل *") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nationalId,
                        onValueChange = {
                            nationalId = it
                            if (documentNumber.isEmpty()) documentNumber = it
                        },
                        label = { Text("رقم الهوية الوطنية / الإقامة / الجواز *") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الجوال للتواصل *") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Document Type
                    Text("نوع وثيقة إثبات الهوية:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        docTypes.forEach { type ->
                            FilterChip(
                                selected = documentType == type,
                                onClick = { documentType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Requested Role
                    Text("الدور المطلوب في المنظومة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        roles.forEach { role ->
                            FilterChip(
                                selected = selectedRole == role,
                                onClick = { selectedRole = role },
                                label = { Text(role.arabicTitle, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Halaqah Picker if student or teacher
                    if (selectedRole != UserRole.PARENT && halaqat.isNotEmpty()) {
                        Text("الحلقة القرآنية المختارة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(halaqat) { h ->
                                FilterChip(
                                    selected = selectedHalaqahId == h.id,
                                    onClick = { selectedHalaqahId = h.id },
                                    label = { Text(h.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // ID Proof Mock Upload Box
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { hasAttachedIdProof = !hasAttachedIdProof }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (hasAttachedIdProof) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = if (hasAttachedIdProof) EmeraldPrimary else GoldPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (hasAttachedIdProof) "تم إرفاق صورة إثبات الهوية ✓" else "إرفاق صورة إثبات الهوية (اضغط هنا)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (hasAttachedIdProof) "ملف الهوية جاهز للتدقيق" else "PNG, JPG أو PDF لتوثيق الحساب",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = hasAttachedIdProof,
                                onCheckedChange = { hasAttachedIdProof = it }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية للمشرف (اختياري)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (name.isBlank() || nationalId.isBlank() || phone.isBlank()) {
                                errorMsg = "يرجى تعبئة جميع الحقول الإلزامية (*)"
                                return@Button
                            }
                            val halaqahName = halaqat.find { it.id == selectedHalaqahId }?.name ?: "حلقة عامة"
                            val proofNote = if (hasAttachedIdProof) "[تم إرفاق وثيقة الهوية] $notes" else notes
                            onSubmit(
                                name.trim(),
                                nationalId.trim(),
                                phone.trim(),
                                selectedRole,
                                selectedHalaqahId,
                                halaqahName,
                                documentType,
                                documentNumber.ifBlank { nationalId.trim() },
                                proofNote
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_subscriber_application_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إرسال طلب التحقق (حالة معلق ⏳)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
