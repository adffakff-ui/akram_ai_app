package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.NotificationType
import com.example.data.model.Student
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.util.PushNotificationHelper

@Composable
fun SendPushNotificationDialog(
    student: Student,
    onDismiss: () -> Unit,
    onSendNotification: (title: String, message: String, type: NotificationType, postSystem: Boolean) -> Unit
) {
    val templates = remember(student) { PushNotificationHelper.getTemplatesForStudent(student) }
    var selectedType by remember { mutableStateOf(NotificationType.HIFZ_ENCOURAGEMENT) }
    var title by remember { mutableStateOf(templates.first().defaultTitle) }
    var message by remember { mutableStateOf(templates.first().defaultMessageTemplate) }
    var postSystemAlert by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("send_push_notification_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "إِرْسَالُ تَنْبِيهٍ فَوْرِيّ مُخَصَّصٍ 🔔",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "إلى الطالب: ${student.name}",
                            fontSize = 12.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ready Template Selector Chips
                Text(
                    text = "اختر قالباً جاهزاً أو خصص الرسالة:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(templates) { template ->
                        FilterChip(
                            selected = selectedType == template.type,
                            onClick = {
                                selectedType = template.type
                                title = template.defaultTitle
                                message = template.defaultMessageTemplate
                            },
                            label = { Text(template.type.arabicTitle, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان التنبيه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("push_notification_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Message Input
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("نص التنبيه الفوري") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("push_notification_message_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // System Notification Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = postSystemAlert,
                        onCheckedChange = { postSystemAlert = it }
                    )
                    Text(
                        text = "إرسال إشعار فوري في شريط تنبيهات هاتف الطالب وولي الأمر",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

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
                            if (title.isNotBlank() && message.isNotBlank()) {
                                onSendNotification(title, message, selectedType, postSystemAlert)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(1f).testTag("confirm_send_push_notification_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إرسال التنبيه")
                    }
                }
            }
        }
    }
}
