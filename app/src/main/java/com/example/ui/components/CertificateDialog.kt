package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Certificate
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@Composable
fun CertificateDialog(
    certificate: Certificate,
    onDismiss: () -> Unit,
    onSaveCustomized: (Certificate) -> Unit,
    canEdit: Boolean = true
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }

    var studentName by remember { mutableStateOf(certificate.studentName) }
    var title by remember { mutableStateOf(certificate.title) }
    var reason by remember { mutableStateOf(certificate.reason) }
    var issueDate by remember { mutableStateOf(certificate.issueDate) }
    var halaqahName by remember { mutableStateOf(certificate.halaqahName) }
    var teacherName by remember { mutableStateOf(certificate.teacherName) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("certificate_dialog"),
            color = Color(0xFFFDFBF7),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Outer Certificate Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(3.dp, GoldPrimary, RoundedCornerShape(16.dp))
                        .padding(6.dp)
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFFDF8),
                                    Color(0xFFFAF3E3)
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Basmala
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            color = EmeraldDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "﴿ إِنَّ هَٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ ﴾",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Golden Quran Icon Emblem
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(GoldPrimary.copy(alpha = 0.15f), CircleShape)
                                .border(2.dp, GoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "وسام التميز",
                                tint = GoldPrimary,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Title
                        if (isEditing) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("عنوان الشهادة") },
                                modifier = Modifier.fillMaxWidth().testTag("cert_title_input")
                            )
                        } else {
                            Text(
                                text = title,
                                color = EmeraldPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "تَسُرُّ إدارة الحلقاتِ أَنْ تَمْنَحَ هَذَا التَّقْدِيرَ إِلَى:",
                            fontSize = 13.sp,
                            color = Color(0xFF4A5568),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Student Name
                        if (isEditing) {
                            OutlinedTextField(
                                value = studentName,
                                onValueChange = { studentName = it },
                                label = { Text("اسم الطالب/الطالبة") },
                                modifier = Modifier.fillMaxWidth().testTag("cert_name_input")
                            )
                        } else {
                            Surface(
                                color = GoldPrimary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                            ) {
                                Text(
                                    text = studentName,
                                    color = EmeraldDark,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Reason / Achievement Description
                        if (isEditing) {
                            OutlinedTextField(
                                value = reason,
                                onValueChange = { reason = it },
                                label = { Text("نص التقدير والإنجاز") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth().testTag("cert_reason_input")
                            )
                        } else {
                            Text(
                                text = reason,
                                fontSize = 13.sp,
                                color = Color(0xFF2D3748),
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Signatures & Details Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "الحلقة المباركة",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = halaqahName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "معلم/ة الحلقة: $teacherName",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }

                            // Official Seal Stamp
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(GoldPrimary, CircleShape)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "ختم الاعتماد",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = "مُعْتَمَدٌ رَسْمِيّاً",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "تاريخ الإنجاز",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = issueDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "النقاط: ${certificate.pointsThreshold} ⭐",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Share via WhatsApp / Apps
                    Button(
                        onClick = {
                            shareCertificateText(
                                context = context,
                                studentName = studentName,
                                title = title,
                                reason = reason,
                                halaqah = halaqahName,
                                date = issueDate
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_cert_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة الشهادة", fontSize = 12.sp)
                    }

                    // Customize Button
                    if (canEdit) {
                        OutlinedButton(
                            onClick = {
                                if (isEditing) {
                                    val updated = certificate.copy(
                                        studentName = studentName,
                                        title = title,
                                        reason = reason,
                                        issueDate = issueDate,
                                        halaqahName = halaqahName,
                                        teacherName = teacherName
                                    )
                                    onSaveCustomized(updated)
                                    isEditing = false
                                } else {
                                    isEditing = true
                                }
                            },
                            modifier = Modifier.testTag("edit_cert_button")
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEditing) "حفظ التعديل" else "تخصيص", fontSize = 12.sp)
                        }
                    }

                    // Close Button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_cert_button")
                    ) {
                        Text("إغلاق", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

private fun shareCertificateText(
    context: Context,
    studentName: String,
    title: String,
    reason: String,
    halaqah: String,
    date: String
) {
    val text = """
        📜 شهادة تقدير واعتزاز بحفظ القرآن الكريم 🌟
        
        يسر مجمع حلقات تحفيظ القرآن الكريم أن يمنح:
        الطالب/ة المبارك/ة: $studentName
        
        🏅 $title
        📝 تفاصيل الإنجاز:
        $reason
        
        🕌 الحلقة: $halaqah
        📅 تاريخ الإنجاز: $date
        
        بارك الله في جهوده ونفع به الإسلام والمسلمين وألبس والديه تاج الوقار.
        رابط منظومة منارة القرآن:
        https://ais-pre-hyedq3aw5ugaoci7a65pzg-567265308769.europe-west3.run.app
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "شهادة تقدير قرآنية: $studentName")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة شهادة التقدير عبر"))
}
