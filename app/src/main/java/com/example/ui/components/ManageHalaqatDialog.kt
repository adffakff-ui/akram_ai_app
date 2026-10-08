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
import com.example.data.model.HalaqahCategory
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHalaqatDialog(
    halaqat: List<Halaqah>,
    onDismiss: () -> Unit,
    onAddHalaqah: (Halaqah) -> Unit,
    onUpdateHalaqah: (Halaqah) -> Unit,
    onDeleteHalaqah: (Halaqah) -> Unit
) {
    var isCreatingOrEditing by remember { mutableStateOf(false) }
    var halaqahToEdit by remember { mutableStateOf<Halaqah?>(null) }

    // Form fields
    var name by remember { mutableStateOf("") }
    var sheikh by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("المستوى المتوسط") }
    var category by remember { mutableStateOf(HalaqahCategory.GENERAL) }
    var meetingTime by remember { mutableStateOf("بعد صلاة العصر") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    fun openEdit(h: Halaqah) {
        halaqahToEdit = h
        name = h.name
        sheikh = h.responsibleSheikh
        level = h.studentsLevel
        category = h.category
        meetingTime = h.meetingTime
        phone = h.teacherPhone
        description = h.description
        isCreatingOrEditing = true
    }

    fun openCreate() {
        halaqahToEdit = null
        name = ""
        sheikh = ""
        level = "المستوى المتوسط"
        category = HalaqahCategory.GENERAL
        meetingTime = "بعد صلاة العصر"
        phone = ""
        description = ""
        isCreatingOrEditing = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("manage_halaqat_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "إِدَارَةُ الحَلَقَاتِ وَالكِوَادِرِ القُرْآنِيَّةِ 🏛️",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "صلاحية المشرف العام والمطور: إضافة، تعديل، وحذف الحلقات",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (isCreatingOrEditing) {
                    // Form Mode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = if (halaqahToEdit == null) "إضافة حلقة جديدة ➕" else "تعديل بيانات الحلقة ✏️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmeraldDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("اسم الحلقة (مثال: حلقة الإمام نافع)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = sheikh,
                            onValueChange = { sheikh = it },
                            label = { Text("الشيخ المحفظ / المعلم المشرف") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = level,
                            onValueChange = { level = it },
                            label = { Text("مستوى الحلقة (مثال: متقدم، متوسط، براعم)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = meetingTime,
                            onValueChange = { meetingTime = it },
                            label = { Text("موعد ومكان الحلقة") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم هاتف المعلم للتواصل") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isCreatingOrEditing = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إلغاء")
                            }

                            Button(
                                onClick = {
                                    if (name.isNotBlank()) {
                                        val halaqah = halaqahToEdit?.copy(
                                            name = name,
                                            responsibleSheikh = sheikh,
                                            studentsLevel = level,
                                            category = category,
                                            meetingTime = meetingTime,
                                            teacherPhone = phone,
                                            description = description
                                        ) ?: Halaqah(
                                            name = name,
                                            responsibleSheikh = sheikh,
                                            studentsLevel = level,
                                            category = category,
                                            meetingTime = meetingTime,
                                            teacherPhone = phone,
                                            description = description
                                        )

                                        if (halaqahToEdit == null) {
                                            onAddHalaqah(halaqah)
                                        } else {
                                            onUpdateHalaqah(halaqah)
                                        }
                                        isCreatingOrEditing = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (halaqahToEdit == null) "حفظ الحلقة 💾" else "تحديث البيانات ✅")
                            }
                        }
                    }
                } else {
                    // List Mode
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الحلقات المسجلة (${halaqat.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Button(
                                onClick = { openCreate() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة حلقة جديدة", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(halaqat, key = { it.id }) { h ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                                            Text(
                                                text = h.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "المعلم: ${h.responsibleSheikh} | ${h.studentsLevel}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "الموعد: ${h.meetingTime}",
                                                fontSize = 10.sp,
                                                color = EmeraldPrimary
                                            )
                                        }

                                        Row {
                                            IconButton(onClick = { openEdit(h) }) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "تعديل",
                                                    tint = EmeraldPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            IconButton(onClick = { onDeleteHalaqah(h) }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "حذف",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(20.dp)
                                                )
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
}
