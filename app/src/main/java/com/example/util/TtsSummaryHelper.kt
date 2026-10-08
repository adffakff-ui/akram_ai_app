package com.example.util

import com.example.data.model.DailyRecord
import com.example.data.model.Student

object TtsSummaryHelper {

    fun generateDailySummaryArabic(student: Student, todayRecord: DailyRecord?): String {
        val completedPages = todayRecord?.newHifzPages ?: 0f
        val goalPages = student.memorizationGoalPagesPerDay
        val progressPercent = if (goalPages > 0) ((completedPages / goalPages) * 100).toInt().coerceAtMost(100) else 0

        val statusText = if (todayRecord != null) {
            "لقد سمعت اليوم سورة ${todayRecord.newHifzSurah} وحصلت على تقييم ${todayRecord.newHifzRating.arabicTitle} و ربحت ${todayRecord.pointsEarned} نقطة جديدة."
        } else {
            "لم يتم التسميع بعد اليوم، ننتظر حضورك المتميز في الحلقة لتسميع سورة ${student.currentSurah}."
        }

        return "السلام عليكم ورحمة الله وبركاته يا ${student.name}. " +
                "هدفك اليوم هو حفظ $goalPages صفحة ومراجعة ${student.revisionGoalPagesPerDay.toInt()} صفحات. " +
                "نسبة إنجازك الحالية هي $progressPercent بالمائة، ومجموع نقاطك التراكمية هو ${student.totalPoints} نقطة. " +
                statusText + " " +
                "وفقك الله ونفع بك والديك والقرآن الكريم."
    }

    fun generateDailySummaryEnglish(student: Student, todayRecord: DailyRecord?): String {
        val completedPages = todayRecord?.newHifzPages ?: 0f
        val goalPages = student.memorizationGoalPagesPerDay
        val progressPercent = if (goalPages > 0) ((completedPages / goalPages) * 100).toInt().coerceAtMost(100) else 0

        return "Hello ${student.name}! Your daily target is $goalPages pages of memorization and ${student.revisionGoalPagesPerDay.toInt()} pages of revision in Surah ${student.currentSurah}. " +
                "Your daily completion rate is $progressPercent percent with a total of ${student.totalPoints} excellence points. " +
                (if (todayRecord != null) "Great job completing today's recitation with rating ${todayRecord.newHifzRating.name}!" else "Keep up your blessed Quran journey!")
    }
}
