package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleEvent
import com.example.data.model.ScheduleEventType
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InteractiveCalendarView(
    events: List<ScheduleEvent>,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    onAddEventClick: () -> Unit,
    onDeleteEvent: (Long) -> Unit,
    onToggleCompleted: (Long, Boolean) -> Unit,
    onSendEventReminder: (ScheduleEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var calendarYear by remember { mutableIntStateOf(2026) }
    var calendarMonth by remember { mutableIntStateOf(10) } // 10 = October

    val monthNamesArabic = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    val daysOfWeek = listOf("سبت", "أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة")

    // Events mapped by date YYYY-MM-DD
    val eventsByDate = remember(events) {
        events.groupBy { it.date }
    }

    // Selected day events
    val selectedDayEvents = remember(events, selectedDate) {
        events.filter { it.date == selectedDate }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("interactive_calendar_view")
    ) {
        // Month Navigation Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            if (calendarMonth > 1) calendarMonth--
                            else {
                                calendarMonth = 12
                                calendarYear--
                            }
                        }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "الشهر السابق")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${monthNamesArabic[calendarMonth - 1]} $calendarYear",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            if (calendarMonth < 12) calendarMonth++
                            else {
                                calendarMonth = 1
                                calendarYear++
                            }
                        }
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "الشهر القادم")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Days of week header
                Row(modifier = Modifier.fillMaxWidth()) {
                    daysOfWeek.forEach { dayName ->
                        Text(
                            text = dayName,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Days Grid (35 cells: 5 rows x 7 days)
                val daysInMonth = 31
                val firstDayOffset = 4 // Arbitrary offset for October 2026

                for (row in 0..4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0..6) {
                            val dayNumber = (row * 7 + col) - firstDayOffset + 1
                            if (dayNumber in 1..daysInMonth) {
                                val dayStr = String.format("%04d-%02d-%02d", calendarYear, calendarMonth, dayNumber)
                                val isSelected = (dayStr == selectedDate)
                                val dayEvents = eventsByDate[dayStr].orEmpty()
                                val hasExam = dayEvents.any { it.eventType == ScheduleEventType.EXAM }
                                val hasSession = dayEvents.any { it.eventType == ScheduleEventType.HALAQAH_SESSION }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) EmeraldPrimary
                                            else if (dayEvents.isNotEmpty()) MaterialTheme.colorScheme.surfaceVariant
                                            else Color.Transparent
                                        )
                                        .clickable { onSelectDate(dayStr) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$dayNumber",
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected || dayEvents.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )

                                        // Event Indicator Dots
                                        if (dayEvents.isNotEmpty()) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                if (hasSession) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White else EmeraldPrimary)
                                                    )
                                                }
                                                if (hasExam) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) GoldPrimary else Color(0xFFDC2626))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).height(44.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Day Header & Add Event Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مواعيد يوم: $selectedDate",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onAddEventClick,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_schedule_event_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("جدولة موعد / امتحان", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Day Events List
        if (selectedDayEvents.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد مواعيد أو امتحانات مجدولة في هذا اليوم",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "اضغط على زر (جدولة موعد) لإضافة حلقة أو اختبار",
                            fontSize = 11.sp,
                            color = EmeraldPrimary
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(selectedDayEvents, key = { it.id }) { event ->
                    ScheduleEventCard(
                        event = event,
                        onDelete = { onDeleteEvent(event.id) },
                        onToggleCompleted = { onToggleCompleted(event.id, !event.isCompleted) },
                        onSendReminder = { onSendEventReminder(event) }
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleEventCard(
    event: ScheduleEvent,
    onDelete: () -> Unit,
    onToggleCompleted: () -> Unit,
    onSendReminder: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.isCompleted) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("schedule_event_card_${event.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Event Type Tag
                Surface(
                    color = when (event.eventType) {
                        ScheduleEventType.EXAM -> Color(0xFFDC2626).copy(alpha = 0.15f)
                        ScheduleEventType.HALAQAH_SESSION -> EmeraldPrimary.copy(alpha = 0.15f)
                        ScheduleEventType.MAJOR_REVISION -> Color(0xFF2563EB).copy(alpha = 0.15f)
                        else -> GoldPrimary.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = event.eventType.arabicTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (event.eventType) {
                            ScheduleEventType.EXAM -> Color(0xFFDC2626)
                            ScheduleEventType.HALAQAH_SESSION -> EmeraldPrimary
                            ScheduleEventType.MAJOR_REVISION -> Color(0xFF2563EB)
                            else -> GoldPrimary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Time Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.startTime} - ${event.endTime}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = event.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Location & Examiner
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📍 ${event.location}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (event.examinerName.isNotBlank()) {
                    Text(
                        text = "👤 ${event.examinerName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (event.targetSurahOrJuz.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🎯 المقرر: ${event.targetSurahOrJuz}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = EmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Completed Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onToggleCompleted() }
                ) {
                    Checkbox(
                        checked = event.isCompleted,
                        onCheckedChange = { onToggleCompleted() }
                    )
                    Text(
                        text = if (event.isCompleted) "تم الإنجاز ✅" else "قيد الانتظار",
                        fontSize = 12.sp,
                        color = if (event.isCompleted) EmeraldPrimary else Color.Gray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Send Push Reminder
                    OutlinedButton(
                        onClick = onSendReminder,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp), tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إشعار فوري 🔔", fontSize = 11.sp)
                    }

                    // Delete
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
