package com.example.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary

@Composable
fun OnboardingSyncGuideDialog(
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }

    val steps = listOf(
        Triple(
            "1. العمل الكامل بدون إنترنت (Offline-First) 📶",
            "صُمم تطبيق منارة القرآن ليعمل بسلاسة مطلقة داخل المساجد والقاعات حتى عند انقطاع الإنترنت أو ضعف الشبكة. يمكنك رصد التسميع، التحضير، وتسجيل الدرجات دون قلق.",
            Icons.Default.CloudOff
        ),
        Triple(
            "2. المزامنة التلقائية الذكية (Auto Sync) 🔄",
            "بمجرد اتصال جهازك بالإنترنت مجدداً، تقوم المنظومة الخلفية بمزامنة جميع التعديلات المحفوظة محلياً تلقائياً مع السيرفر وبوابة أولياء الأمور دون أي تدخل يدوي.",
            Icons.Default.Sync
        ),
        Triple(
            "3. النسخ الاحتياطي وتصدير PDF و CSV 💾",
            "يمكنك في أي وقت أخذ نسخة احتياطية كاملة بصيغة JSON، أو تصدير تقارير التسميع والشهادات الرقمية بصيغة PDF ومشاركتها مع أولياء الأمور عبر واتساب أو طباعتها مباشرة.",
            Icons.Default.PictureAsPdf
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("onboarding_sync_guide_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دَلِيلُ المِيزَاتِ وَالمُزَامَنَةِ السَّحَابِيَّةِ 📖",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step Icon in circle
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = steps[currentStep].third,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step Title
                Text(
                    text = steps[currentStep].first,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = EmeraldDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Step Description
                Text(
                    text = steps[currentStep].second,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Step Dots Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in steps.indices) {
                        Box(
                            modifier = Modifier
                                .size(if (i == currentStep) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (i == currentStep) EmeraldPrimary else Color.LightGray)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("السابق")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < steps.lastIndex) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (currentStep < steps.lastIndex) "التالي" else "فهمت ذلك، تم ✅")
                    }
                }
            }
        }
    }
}
