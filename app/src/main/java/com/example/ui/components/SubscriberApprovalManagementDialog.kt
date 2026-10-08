package com.example.ui.components

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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Halaqah
import com.example.data.model.SubscriberApprovalStatus
import com.example.data.model.SubscriberRegistration
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriberApprovalManagementDialog(
    subscribers: List<SubscriberRegistration>,
    halaqat: List<Halaqah>,
    currentRole: UserRole, // SUPERVISOR or DEVELOPER
    onDismiss: () -> Unit,
    onApprove: (SubscriberRegistration, String) -> Unit,
    onReject: (SubscriberRegistration, String) -> Unit,
    onDelete: (SubscriberRegistration) -> Unit,
    onAddNewSubscriber: (
        fullName: String,
        nationalId: String,
        phone: String,
        requestedRole: UserRole,
        halaqahId: Long?,
        halaqahName: String,
        documentType: String,
        documentNumber: String,
        verificationNotes: String
    ) -> Unit
) {
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0 = الكل, 1 = قيد الانتظار, 2 = معتمد, 3 = مرفوض
    var showAddSubscriberSheet by remember { mutableStateOf(false) }
    var selectedSubscriberForDetails by remember { mutableStateOf<SubscriberRegistration?>(null) }
    var rejectTargetSubscriber by remember { mutableStateOf<SubscriberRegistration?>(null) }
    var rejectReasonInput by remember { mutableStateOf("") }

    val filteredList = remember(subscribers, selectedFilterTab) {
        when (selectedFilterTab) {
            1 -> subscribers.filter { it.approvalStatus == SubscriberApprovalStatus.PENDING }
            2 -> subscribers.filter { it.approvalStatus == SubscriberApprovalStatus.APPROVED }
            3 -> subscribers.filter { it.approvalStatus == SubscriberApprovalStatus.REJECTED }
            else -> subscribers
        }
    }

    val pendingCount = remember(subscribers) {
        subscribers.count { it.approvalStatus == SubscriberApprovalStatus.PENDING }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("subscriber_approval_management_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Banner
                Surface(
                    color = if (currentRole == UserRole.SUPERVISOR) EmeraldDark else Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "اعتماد المشتركين وإثبات الهوية 🆔👑",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentRole == UserRole.SUPERVISOR)
                                        "صلاحية المشرف العام | فحص الوثائق والموافقة على الانضمام"
                                    else
                                        "صلاحية المطور التقني | إدارة حسابات المشتركين وتدقيق الهويات",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }
                }

                // Status Tabs & Quick Action
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Filter Tabs
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = selectedFilterTab == 0,
                                onClick = { selectedFilterTab = 0 },
                                label = { Text("الكل (${subscribers.size})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedFilterTab == 1,
                                onClick = { selectedFilterTab = 1 },
                                leadingIcon = {
                                    if (pendingCount > 0) {
                                        Badge(containerColor = Color(0xFFEF4444)) {
                                            Text("$pendingCount", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                },
                                label = { Text("قيد التدقيق ⏳", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedFilterTab == 2,
                                onClick = { selectedFilterTab = 2 },
                                label = { Text("معتمد ✅", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedFilterTab == 3,
                                onClick = { selectedFilterTab = 3 },
                                label = { Text("مرفوض ❌", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Button to manually submit / simulate new subscriber with ID proof
                    Button(
                        onClick = { showAddSubscriberSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_subscriber_application_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تقديم طلب اشتراك جديد مع إثبات الهوية 📝", fontSize = 12.sp)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Subscribers List
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AssignmentLate,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("لا توجد طلبات اشتراك في هذا القسم حالياً", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredList, key = { it.id }) { subscriber ->
                            SubscriberCardItem(
                                subscriber = subscriber,
                                currentRole = currentRole,
                                onApprove = {
                                    val approver = if (currentRole == UserRole.SUPERVISOR) "المشرف العام" else "المطور التقني"
                                    onApprove(subscriber, approver)
                                },
                                onRejectClick = {
                                    rejectTargetSubscriber = subscriber
                                    rejectReasonInput = ""
                                },
                                onDelete = { onDelete(subscriber) },
                                onViewDetails = { selectedSubscriberForDetails = subscriber }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet to Add / Register a New Subscriber with ID Proof
    if (showAddSubscriberSheet) {
        AddNewSubscriberDialog(
            halaqat = halaqat,
            onDismiss = { showAddSubscriberSheet = false },
            onSubmit = { fullName, nationalId, phone, role, halaqahId, halaqahName, docType, docNum, notes ->
                onAddNewSubscriber(fullName, nationalId, phone, role, halaqahId, halaqahName, docType, docNum, notes)
                showAddSubscriberSheet = false
            }
        )
    }

    // Modal to view details and ID verification proof
    selectedSubscriberForDetails?.let { subscriber ->
        SubscriberDetailsDialog(
            subscriber = subscriber,
            onDismiss = { selectedSubscriberForDetails = null },
            onApprove = {
                val approver = if (currentRole == UserRole.SUPERVISOR) "المشرف العام" else "المطور التقني"
                onApprove(subscriber, approver)
                selectedSubscriberForDetails = null
            }
        )
    }

    // Reject Dialog
    rejectTargetSubscriber?.let { subscriber ->
        AlertDialog(
            onDismissRequest = { rejectTargetSubscriber = null },
            title = {
                Text("رفض طلب الاشتراك ❌", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column {
                    Text(
                        text = "يرجى تحديد سبب رفض اشتراك (${subscriber.fullName}) لعدم اكتمال إثبات الهوية:",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReasonInput,
                        onValueChange = { rejectReasonInput = it },
                        placeholder = { Text("مثال: صورة بطاقة الهوية الوطنية غير واضحة / رقم الهوية غير مطابق") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(subscriber, rejectReasonInput)
                        rejectTargetSubscriber = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("تأكيد الرفض ❌")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTargetSubscriber = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SubscriberCardItem(
    subscriber: SubscriberRegistration,
    currentRole: UserRole,
    onApprove: () -> Unit,
    onRejectClick: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit
) {
    val statusColor = when (subscriber.approvalStatus) {
        SubscriberApprovalStatus.APPROVED -> EmeraldPrimary
        SubscriberApprovalStatus.PENDING -> Color(0xFFF59E0B)
        SubscriberApprovalStatus.REJECTED -> Color(0xFFEF4444)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subscriber_item_${subscriber.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (subscriber.requestedRole) {
                            UserRole.TEACHER -> Icons.Default.School
                            UserRole.PARENT -> Icons.Default.FamilyRestroom
                            else -> Icons.Default.Person
                        },
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subscriber.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = subscriber.approvalStatus.arabicTitle,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Identity Document Badge & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "إثبات الهوية: ${subscriber.identityDocumentType} (${subscriber.nationalIdOrPassport})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (subscriber.halaqahName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mosque, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الحلقة المطلوبة: ${subscriber.halaqahName}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            if (subscriber.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الجوال: ${subscriber.phone}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            if (subscriber.approvalStatus == SubscriberApprovalStatus.APPROVED && subscriber.approvedBy.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "اعتمد بواسطة: ${subscriber.approvedBy} (${subscriber.approvalDate})",
                        fontSize = 10.sp,
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (subscriber.approvalStatus == SubscriberApprovalStatus.REJECTED && subscriber.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFEF4444).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "سبب الرفض: ${subscriber.rejectionReason}",
                        fontSize = 10.sp,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فحص الوثائق 📄", fontSize = 11.sp)
                }

                if (subscriber.approvalStatus == SubscriberApprovalStatus.PENDING) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).testTag("approve_subscriber_${subscriber.id}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("موافقة واعتماد ✅", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onRejectClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("رفض ❌", fontSize = 11.sp)
                    }
                } else {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddNewSubscriberDialog(
    halaqat: List<Halaqah>,
    onDismiss: () -> Unit,
    onSubmit: (
        fullName: String,
        nationalId: String,
        phone: String,
        requestedRole: UserRole,
        halaqahId: Long?,
        halaqahName: String,
        documentType: String,
        documentNumber: String,
        notes: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var requestedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var selectedHalaqahId by remember { mutableStateOf<Long?>(halaqat.firstOrNull()?.id) }
    var documentType by remember { mutableStateOf("بطاقة الهوية الوطنية") }
    var documentNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("وثيقة الهوية الرسمية سارية المفعول ومطابقة للاسم الثلاثي") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("طلب اشتراك وإثبات هوية جديد 📝🆔", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("الاسم الكامل للمشترك") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = nationalId,
                        onValueChange = {
                            nationalId = it
                            if (documentNumber.isEmpty()) documentNumber = it
                        },
                        label = { Text("رقم الهوية الوطنية / الإقامة / جواز السفر") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الجوال للتواصل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("نوع إثبات الهوية المرفوع:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("بطاقة الهوية الوطنية", "الإقامة النظامية", "جواز السفر").forEach { type ->
                            FilterChip(
                                selected = documentType == type,
                                onClick = { documentType = type },
                                label = { Text(type, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                item {
                    Text("الدور المطلوب بالتطبيق:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(UserRole.STUDENT, UserRole.TEACHER, UserRole.PARENT).forEach { role ->
                            FilterChip(
                                selected = requestedRole == role,
                                onClick = { requestedRole = role },
                                label = { Text(role.arabicTitle.split(" ").first(), fontSize = 10.sp) }
                            )
                        }
                    }
                }

                if (halaqat.isNotEmpty()) {
                    item {
                        Text("الحلقة المراد الالتحاق بها:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            halaqat.take(3).forEach { h ->
                                FilterChip(
                                    selected = selectedHalaqahId == h.id,
                                    onClick = { selectedHalaqahId = h.id },
                                    label = { Text(h.name, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات تدقيق الهوية وتوثيق المشترك") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && nationalId.isNotBlank()) {
                        val chosenHalaqah = halaqat.find { it.id == selectedHalaqahId }
                        onSubmit(
                            fullName,
                            nationalId,
                            phone,
                            requestedRole,
                            selectedHalaqahId,
                            chosenHalaqah?.name ?: "",
                            documentType,
                            documentNumber,
                            notes
                        )
                    }
                },
                enabled = fullName.isNotBlank() && nationalId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("إرسال الطلب للاعتماد 📤")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun SubscriberDetailsDialog(
    subscriber: SubscriberRegistration,
    onDismiss: () -> Unit,
    onApprove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = GoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ملف تدقيق هوية المشترك 🆔", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "الاسم: ${subscriber.fullName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "الدور: ${subscriber.requestedRole.arabicTitle}", fontSize = 12.sp, color = EmeraldDark)
                        Text(text = "رقم الهوية: ${subscriber.nationalIdOrPassport}", fontSize = 12.sp)
                        Text(text = "نوع الوثيقة: ${subscriber.identityDocumentType}", fontSize = 12.sp)
                        Text(text = "رقم الوثيقة المثبتة: ${subscriber.identityDocumentNumber}", fontSize = 12.sp)
                        Text(text = "الجوال: ${subscriber.phone}", fontSize = 12.sp)
                        if (subscriber.halaqahName.isNotBlank()) {
                            Text(text = "الحلقة: ${subscriber.halaqahName}", fontSize = 12.sp)
                        }
                    }
                }

                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "إثبات الهوية المعتمد 🛡️", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subscriber.identityVerificationNotes.ifBlank { "تم فحص الهوية والتحقق من صلاحيتها رسميّاً." },
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (subscriber.approvalStatus == SubscriberApprovalStatus.PENDING) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("اعتماد وموافقة فورية ✅")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("إغلاق") }
            }
        },
        dismissButton = {
            if (subscriber.approvalStatus == SubscriberApprovalStatus.PENDING) {
                TextButton(onClick = onDismiss) { Text("إغلاق") }
            }
        }
    )
}
