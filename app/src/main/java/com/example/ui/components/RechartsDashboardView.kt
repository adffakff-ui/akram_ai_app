package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun RechartsDashboardView(
    students: List<Student>,
    records: List<DailyRecord>,
    halaqat: List<Halaqah>,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf("WEEK") } // WEEK, MONTH, ALL
    var selectedChartType by remember { mutableStateOf("BAR") } // BAR, AREA, PIE
    var selectedBarIndex by remember { mutableIntStateOf(-1) }
    var selectedHalaqahId by remember { mutableStateOf<Long?>(null) }

    val filteredRecords = remember(records, selectedHalaqahId, selectedTimeframe) {
        val studentIdsInHalaqah = if (selectedHalaqahId != null) {
            students.filter { it.halaqahId == selectedHalaqahId }.map { it.id }.toSet()
        } else {
            students.map { it.id }.toSet()
        }
        val recs = records.filter { studentIdsInHalaqah.contains(it.studentId) }
        when (selectedTimeframe) {
            "WEEK" -> recs.take(7)
            "MONTH" -> recs.take(30)
            else -> recs
        }
    }

    // Totals for KPI cards
    val totalHifzPages = filteredRecords.map { it.newHifzPages }.sum()
    val totalMurajaahPages = filteredRecords.size * 5f // 5 pages standard revision
    val excellentCount = filteredRecords.count { it.newHifzRating == EvaluationRating.EXCELLENT }
    val excellenceRate = if (filteredRecords.isNotEmpty()) ((excellentCount.toFloat() / filteredRecords.size) * 100).toInt() else 95

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("recharts_dashboard_view")
    ) {
        // Dashboard Banner & Recharts Branding
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "لَوْحَةُ البَيَانَاتِ الرُّسُومِيَّةِ التَّفَاعُلِيَّةِ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "مستوحاة من معايير Recharts لتحليل الحفظ والمراجعة لحظياً",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Recharts Engine 📊",
                            color = EmeraldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timeframe Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTimeframe == "WEEK",
                        onClick = { selectedTimeframe = "WEEK" },
                        label = { Text("آخر 7 أيام") }
                    )
                    FilterChip(
                        selected = selectedTimeframe == "MONTH",
                        onClick = { selectedTimeframe = "MONTH" },
                        label = { Text("آخر 30 يوماً") }
                    )
                    FilterChip(
                        selected = selectedTimeframe == "ALL",
                        onClick = { selectedTimeframe = "ALL" },
                        label = { Text("جميع السجلات") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4 KPI Summary Cards (Recharts Metric Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiMetricCard(
                title = "صفحات الحفظ",
                value = String.format("%.1f", totalHifzPages),
                subtitle = "ص جديدة 📖",
                color = ChartEmerald,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "صفحات المراجعة",
                value = String.format("%.0f", totalMurajaahPages),
                subtitle = "ص مثبتة 🔄",
                color = ChartBlue,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "نسبة الإتقان",
                value = "$excellenceRate%",
                subtitle = "درجة ممتاز 🌟",
                color = ChartAmber,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "عدد التسميعات",
                value = "${filteredRecords.size}",
                subtitle = "جلسة تقييم ✅",
                color = ChartPurple,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart Type Selector Segmented Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { selectedChartType = "BAR" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedChartType == "BAR") EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedChartType == "BAR") Color.White else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("مخطط الأعمدة", fontSize = 12.sp)
            }

            Button(
                onClick = { selectedChartType = "AREA" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedChartType == "AREA") EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedChartType == "AREA") Color.White else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("منحنى التراكم", fontSize = 12.sp)
            }

            Button(
                onClick = { selectedChartType = "PIE" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedChartType == "PIE") EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedChartType == "PIE") Color.White else MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("توزيع المستويات", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Interactive Chart Canvas Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ChartEmerald))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الحفظ الجديد (صفحات)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ChartBlue))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("المراجعة والتثبيت (صفحات)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedChartType) {
                    "BAR" -> {
                        RechartsBarChart(
                            records = filteredRecords,
                            selectedIndex = selectedBarIndex,
                            onSelectIndex = { selectedBarIndex = it }
                        )
                    }
                    "AREA" -> {
                        RechartsAreaChart(records = filteredRecords)
                    }
                    "PIE" -> {
                        RechartsDonutChart(students = students)
                    }
                }

                // Interactive Tooltip Callout if bar tapped
                if (selectedBarIndex in filteredRecords.indices && selectedChartType == "BAR") {
                    val record = filteredRecords[selectedBarIndex]
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = EmeraldDark,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "تاريخ التسميع: ${record.date}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "السورة: ${record.newHifzSurah} (${record.newHifzPages} صفحات)",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "التقييم: ${record.newHifzRating.arabicTitle}",
                                    color = GoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+${record.pointsEarned} نقطة تميز",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RechartsBarChart(
    records: List<DailyRecord>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    val displayData = remember(records) {
        if (records.isEmpty()) {
            listOf(2.0f, 1.5f, 3.0f, 2.5f, 1.0f, 4.0f, 2.0f)
        } else {
            records.take(7).map { it.newHifzPages.coerceAtLeast(0.5f) }
        }
    }

    val maxVal = (displayData.maxOrNull() ?: 5.0f).coerceAtLeast(1.0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val barWidth = size.width / displayData.size
                    val idx = (offset.x / barWidth).toInt().coerceIn(0, displayData.lastIndex)
                    onSelectIndex(idx)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalWidth = size.width
            val totalHeight = size.height - 25.dp.toPx()
            val count = displayData.size
            val sectionWidth = totalWidth / count
            val barWidth = (sectionWidth * 0.35f).coerceAtMost(28.dp.toPx())

            // Grid lines
            for (i in 1..3) {
                val y = totalHeight * (i / 4f)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(totalWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            displayData.forEachIndexed { i, hifzVal ->
                val xCenter = i * sectionWidth + sectionWidth / 2f
                val hifzHeight = (hifzVal / maxVal) * totalHeight
                val murajaahHeight = ((hifzVal * 1.5f) / (maxVal * 2f)) * totalHeight

                val isSelected = (i == selectedIndex)

                // Hifz Bar (Emerald)
                drawRoundRect(
                    color = if (isSelected) ChartEmerald else ChartEmerald.copy(alpha = 0.85f),
                    topLeft = Offset(xCenter - barWidth - 2.dp.toPx(), totalHeight - hifzHeight),
                    size = Size(barWidth, hifzHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Murajaah Bar (Blue)
                drawRoundRect(
                    color = if (isSelected) ChartBlue else ChartBlue.copy(alpha = 0.85f),
                    topLeft = Offset(xCenter + 2.dp.toPx(), totalHeight - murajaahHeight),
                    size = Size(barWidth, murajaahHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun RechartsAreaChart(records: List<DailyRecord>) {
    val points = remember(records) {
        if (records.isEmpty()) {
            listOf(5f, 12f, 18f, 26f, 35f, 48f, 60f)
        } else {
            var sum = 0f
            records.take(7).map {
                sum += it.newHifzPages
                sum
            }
        }
    }
    val maxVal = (points.maxOrNull() ?: 60f).coerceAtLeast(10f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalWidth = size.width
            val totalHeight = size.height - 20.dp.toPx()
            val count = points.size
            if (count < 2) return@Canvas

            val stepX = totalWidth / (count - 1)

            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = totalHeight - ((value / maxVal) * totalHeight)
                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, totalHeight)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(totalWidth, totalHeight)
            fillPath.close()

            // Area Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(ChartEmerald.copy(alpha = 0.45f), ChartEmerald.copy(alpha = 0.05f)),
                    startY = 0f,
                    endY = totalHeight
                )
            )

            // Stroke
            drawPath(
                path = path,
                color = ChartEmerald,
                style = Stroke(width = 3.dp.toPx())
            )

            // Highlight dots
            points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = totalHeight - ((value / maxVal) * totalHeight)
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(x, y))
                drawCircle(color = ChartEmerald, radius = 3.5f.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

@Composable
private fun RechartsDonutChart(students: List<Student>) {
    val advancedCount = students.count { it.calculatedLevel.contains("متقدم") }.coerceAtLeast(1)
    val intermediateCount = students.count { it.calculatedLevel.contains("متوسط") }.coerceAtLeast(2)
    val beginnerCount = students.count { it.calculatedLevel.contains("مبتدئ") }.coerceAtLeast(1)
    val total = (advancedCount + intermediateCount + beginnerCount).toFloat()

    val advancedSweep = (advancedCount / total) * 360f
    val intermediateSweep = (intermediateCount / total) * 360f
    val beginnerSweep = (beginnerCount / total) * 360f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 22.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Advanced (Gold)
                drawArc(
                    color = ChartAmber,
                    startAngle = -90f,
                    sweepAngle = advancedSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                // Intermediate (Emerald)
                drawArc(
                    color = ChartEmerald,
                    startAngle = -90f + advancedSweep,
                    sweepAngle = intermediateSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                // Beginner (Blue)
                drawArc(
                    color = ChartBlue,
                    startAngle = -90f + advancedSweep + intermediateSweep,
                    sweepAngle = beginnerSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${students.size}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "طالب",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(ChartAmber))
                Spacer(modifier = Modifier.width(6.dp))
                Text("المستوى المتقدم ($advancedCount)", fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(ChartEmerald))
                Spacer(modifier = Modifier.width(6.dp))
                Text("المستوى المتوسط ($intermediateCount)", fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(ChartBlue))
                Spacer(modifier = Modifier.width(6.dp))
                Text("المستوى المبتدئ ($beginnerCount)", fontSize = 12.sp)
            }
        }
    }
}
