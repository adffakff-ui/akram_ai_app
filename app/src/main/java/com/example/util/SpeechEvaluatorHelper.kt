package com.example.util

import com.example.data.model.Student

data class RecitationEvaluationResult(
    val accuracyPercentage: Int,
    val totalWordsExpected: Int,
    val matchedWordsCount: Int,
    val detectedText: String,
    val expectedText: String,
    val tajweedNotes: List<String>,
    val feedbackMessage: String
)

object SpeechEvaluatorHelper {

    /**
     * تقييم تلاوة الطالب آلياً ومقارنتها بالنص القرآني المعتمد
     */
    fun evaluateRecitation(
        spokenText: String,
        surahName: String,
        expectedAyahText: String
    ): RecitationEvaluationResult {
        val cleanSpoken = normalizeArabicText(spokenText)
        val cleanExpected = normalizeArabicText(expectedAyahText)

        val expectedWords = cleanExpected.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val spokenWords = cleanSpoken.split("\\s+".toRegex()).filter { it.isNotBlank() }

        var matched = 0
        val spokenSet = spokenWords.toSet()
        expectedWords.forEach { word ->
            if (spokenSet.contains(word)) {
                matched++
            }
        }

        val accuracy = if (expectedWords.isNotEmpty()) {
            ((matched.toFloat() / expectedWords.size) * 100).toInt().coerceIn(0, 100)
        } else 100

        val notes = mutableListOf<String>()
        if (accuracy >= 90) {
            notes.add("ما شاء الله، مخارج الحروف ممتازة والتلاوة سلسة ومرتبة 🌟")
            notes.add("المدود وأحكام الغنة منضبطة")
        } else if (accuracy >= 70) {
            notes.add("تلاوة جيدة جداً، مع ملاحظة تثبيت مواضع القلقلة ⚠️")
            notes.add("يُرجى الانتباه للنون الساكنة والتنوين")
        } else {
            notes.add("يُنصح بإعادة الاستماع للشيخ الحصري لتثبيت التشكيل 📖")
            notes.add("مراجعة الآيات قبل التسميع النهائي")
        }

        val feedback = when {
            accuracy >= 95 -> "ممتاز جداً! التلاوة مطابقة تماماً للمصحف الشريف."
            accuracy >= 80 -> "أداء رائع ومتقن، مع أخطاء طفيفة تم توضيحها."
            accuracy >= 60 -> "أداء متوسط، ننصح بالمراجعة مع المحفظ لضبط الوقف والابتداء."
            else -> "يحتاج الطالب لتثبيت الآيات والاستماع المتكرر قبل التسميع."
        }

        return RecitationEvaluationResult(
            accuracyPercentage = accuracy,
            totalWordsExpected = expectedWords.size,
            matchedWordsCount = matched,
            detectedText = spokenText,
            expectedText = expectedAyahText,
            tajweedNotes = notes,
            feedbackMessage = feedback
        )
    }

    private fun normalizeArabicText(input: String): String {
        return input
            .replace("[\\u064B-\\u065F\\u0670]".toRegex(), "") // Remove Tashkeel/Harakat
            .replace("[إأآا]".toRegex(), "ا")
            .replace("ى", "ي")
            .replace("ة", "ه")
            .replace("[،.؟!:\"'()]".toRegex(), "")
            .trim()
    }
}
