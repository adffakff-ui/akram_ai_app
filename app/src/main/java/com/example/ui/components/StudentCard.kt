package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.data.model.Gender
import com.example.data.model.Student
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@Composable
fun StudentCard(
    student: Student,
    halaqahName: String,
    onRecordClick: () -> Unit,
    onSendPushClick: () -> Unit,
    onChallengeClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    onTtsPlayClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar + Name + Level Badge + Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (student.gender == Gender.MALE) EmeraldPrimary.copy(alpha = 0.15f) else Color(0xFFF43F5E).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (student.gender == Gender.MALE) Icons.Default.Face else Icons.Default.Face4,
                        contentDescription = null,
                        tint = if (student.gender == Gender.MALE) EmeraldPrimary else Color(0xFFE11D48),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Calculated Level Badge
                        Surface(
                            color = GoldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = student.calculatedLevel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$halaqahName • الجزء ${student.currentJuz} (${student.currentSurah})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Points Badge
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${student.totalPoints}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = EmeraldPrimary
                        )
                    }
                }

                // More Menu
                Box {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "المزيد")
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("تعديل بيانات الطالب") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                onEditClick()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("سجل الحضور والغياب 📅") },
                            leadingIcon = { Icon(Icons.Default.EventAvailable, contentDescription = null) },
                            onClick = {
                                onAttendanceClick()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("استخراج تقرير أداء PDF 📄") },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                            onClick = {
                                onExportPdfClick()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("تحدي الحفظ اليومي ⏱️") },
                            leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                            onClick = {
                                onChallengeClick()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("ملخص صوتي ذكي (TTS) 🔊") },
                            leadingIcon = { Icon(Icons.Default.VolumeUp, contentDescription = null) },
                            onClick = {
                                onTtsPlayClick()
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("مراسلة الولي عبر واتساب 📲") },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null) },
                            onClick = {
                                val message = "السلام عليكم ورحمة الله، مرحباً بولي أمر الطالب ${student.name}. نود مشاركتكم تقدم الطالب في حلقة القرآن الكريم."
                                val url = "https://api.whatsapp.com/send?phone=${student.parentPhone}&text=${Uri.encode(message)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                                showMoreMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("حذف الطالب من الحلقة", color = Color.Red) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                            onClick = {
                                onDeleteClick()
                                showMoreMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Interactive Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Record Hifz Button (Primary action)
                Button(
                    onClick = onRecordClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f).testTag("record_hifz_student_${student.id}")
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("رَصْدُ التَّسْمِيعِ 📖", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Send Personalized Push Alert
                OutlinedButton(
                    onClick = onSendPushClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("send_push_student_${student.id}")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تنبيه فوري 🔔", fontSize = 11.sp)
                }

                // Quick PDF Export
                FilledTonalIconButton(
                    onClick = onExportPdfClick,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "تقرير PDF", tint = EmeraldPrimary)
                }
            }
        }
    }
}
