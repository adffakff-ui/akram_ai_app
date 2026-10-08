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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.QuranData
import com.example.data.model.Student
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.delay

@Composable
fun DailyChallengeDialog(
    student: Student,
    onDismiss: () -> Unit,
    onChallengeCompleted: (bonusPoints: Int) -> Unit
) {
    var selectedSurah by remember { mutableStateOf(QuranData.surahs.find { it.name == student.currentSurah } ?: QuranData.surahs.first()) }
    var totalSeconds by remember(selectedSurah) {
        val baseSeconds = (selectedSurah.versesCount * 8).coerceIn(60, 900)
        mutableIntStateOf(baseSeconds)
    }
    var remainingSeconds by remember(totalSeconds) { mutableIntStateOf(totalSeconds) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, remainingSeconds) {
        if (isTimerRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        } else if (remainingSeconds == 0 && isTimerRunning) {
            isTimerRunning = false
            isCompleted = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("daily_challenge_dialog")
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
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "تَحَدِّي الحِفْظِ اليَوْمِيّ ⏱️🏆",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "للطالب: ${student.name}",
                            fontSize = 12.sp,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Surah Selector
                Text(
                    text = "السورة المستهدفة: سورة ${selectedSurah.name} (${selectedSurah.versesCount} آية)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Timer Visual Circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val minutes = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, secs),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingSeconds < 30) Color.Red else EmeraldPrimary
                        )
                        Text(
                            text = if (isTimerRunning) "المؤقت جارٍ..." else if (isCompleted) "انتهى الوقت!" else "جاهز للبدء",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isTimerRunning = !isTimerRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTimerRunning) Color(0xFFF59E0B) else EmeraldPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTimerRunning) "إيقاف مؤقت" else "بدء التحدي")
                    }

                    Button(
                        onClick = {
                            isTimerRunning = false
                            onChallengeCompleted(20)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تم الإتقان (+20)")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("إغلاق التحدي")
                }
            }
        }
    }
}
