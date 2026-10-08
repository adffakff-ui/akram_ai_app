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
import com.example.data.model.ChatMessage
import com.example.data.model.UserRole
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.QuranViewModel
import kotlinx.coroutines.launch

@Composable
fun MessagesScreen(
    viewModel: QuranViewModel
) {
    val students by viewModel.allStudents.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val selectedChatStudentId by viewModel.selectedChatStudentId.collectAsState()

    val activeStudent = remember(students, selectedChatStudentId) {
        students.find { it.id == selectedChatStudentId } ?: students.firstOrNull()
    }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(1, 1, UserRole.TEACHER, "الشيخ عبد الرحمن", "السلام عليكم ورحمة الله، تم اليوم تسميع سورة البقرة بإتقان ممتاز.", System.currentTimeMillis() - 3600000, true),
            ChatMessage(2, 1, UserRole.PARENT, "أحمد المنصور", "وعليكم السلام ورحمة الله وبركاته، جزاكم الله خيراً يا شيخ وبارك في جهودكم.", System.currentTimeMillis() - 1800000, true)
        )
    }

    var messageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("messages_screen")
    ) {
        // Chat Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Forum, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "محادثة خاصة: ${activeStudent?.name ?: "الطالب"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "بين المعلم وولي الأمر مباشرة",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Message List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { msg ->
                val isMe = (msg.senderRole == currentRole)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (isMe) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp),
                        tonalElevation = 2.dp,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.senderName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) Color.White.copy(alpha = 0.8f) else EmeraldDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.content,
                                fontSize = 13.sp,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("اكتب رسالة...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f).testTag("chat_message_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (messageText.isNotBlank() && activeStudent != null) {
                        messages.add(
                            ChatMessage(
                                id = System.currentTimeMillis(),
                                studentId = activeStudent.id,
                                senderRole = currentRole,
                                senderName = if (currentRole == UserRole.TEACHER) "المعلم" else "ولي الأمر",
                                content = messageText,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(EmeraldPrimary, RoundedCornerShape(23.dp))
                    .testTag("send_chat_message_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.White)
            }
        }
    }
}
