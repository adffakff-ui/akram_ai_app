package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.Certificate
import com.example.data.model.DailyRecord
import com.example.data.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    /**
     * إنشاء تقرير أداء حفظ ومراجعة بصيغة PDF للطالب
     */
    suspend fun generateStudentReportPdf(
        context: Context,
        student: Student,
        halaqahName: String,
        records: List<DailyRecord>
    ): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Background
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRect(0f, 0f, 595f, 842f, paint)

        // Header Banner (Emerald Green)
        paint.color = Color.rgb(4, 120, 87)
        canvas.drawRect(0f, 0f, 595f, 110f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("مَنَارَةُ القُرْآنِ الكَرِيمِ", 595f / 2, 45f, paint)

        paint.textSize = 14f
        canvas.drawText("تقرير المتابعة الدورية للحفظ والمراجعة", 595f / 2, 75f, paint)

        paint.textSize = 10f
        val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
        canvas.drawText("تاريخ الإصدار: $dateStr", 595f / 2, 95f, paint)

        // Student Info Card
        paint.color = Color.WHITE
        paint.setShadowLayer(4f, 0f, 2f, Color.argb(30, 0, 0, 0))
        canvas.drawRoundRect(30f, 130f, 565f, 230f, 12f, 12f, paint)
        paint.clearShadowLayer()

        paint.color = Color.rgb(15, 23, 42)
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 15f
        canvas.drawText("اسم الطالب: ${student.name}", 545f, 160f, paint)
        canvas.drawText("الحلقة: $halaqahName", 545f, 185f, paint)
        canvas.drawText("الجزء الحالي: ${student.currentJuz} (${student.currentSurah})", 545f, 210f, paint)

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("المستوى: ${student.calculatedLevel}", 50f, 160f, paint)
        canvas.drawText("مجموع النقاط: ${student.totalPoints} نقطة", 50f, 185f, paint)
        canvas.drawText("ولي الأمر: ${student.parentName}", 50f, 210f, paint)

        // Table Header
        paint.color = Color.rgb(4, 120, 87)
        canvas.drawRoundRect(30f, 250f, 565f, 280f, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("التاريخ", 515f, 269f, paint)
        canvas.drawText("الحفظ الجديد", 410f, 269f, paint)
        canvas.drawText("التقييم", 310f, 269f, paint)
        canvas.drawText("المراجعة", 200f, 269f, paint)
        canvas.drawText("النقاط", 70f, 269f, paint)

        // Records Rows
        var currentY = 305f
        paint.color = Color.rgb(51, 65, 85)
        paint.textSize = 10f

        val displayRecords = records.take(12)
        displayRecords.forEachIndexed { idx, record ->
            // Row background tint for alternate rows
            if (idx % 2 == 1) {
                val rowBg = Paint().apply { color = Color.rgb(241, 245, 249) }
                canvas.drawRect(30f, currentY - 16f, 565f, currentY + 8f, rowBg)
            }

            canvas.drawText(record.date, 515f, currentY, paint)
            canvas.drawText("${record.newHifzSurah} (${record.newHifzPages} ص)", 410f, currentY, paint)
            canvas.drawText(record.newHifzRating.arabicTitle, 310f, currentY, paint)
            canvas.drawText(record.murajaahSurah.ifBlank { "—" }, 200f, currentY, paint)
            canvas.drawText("+${record.pointsEarned}", 70f, currentY, paint)

            currentY += 28f
        }

        // Footer / Stamp
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("تم استخراج هذا التقرير تلقائياً عبر منظومة منارة القرآن الكريم", 595f / 2, 790f, paint)
        canvas.drawText("مع تحيات إدارة المجمع القرآني والمحفظ المشرف", 595f / 2, 805f, paint)

        document.finishPage(page)

        val dir = File(context.cacheDir, "pdf_reports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "report_${student.id}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        file
    }

    /**
     * إنشاء شهادة تقدير رقمية بصيغة PDF
     */
    suspend fun generateCertificatePdf(
        context: Context,
        certificate: Certificate
    ): File = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create() // Landscape A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Background
        paint.color = Color.rgb(255, 251, 235) // Warm parchment gold tint
        canvas.drawRect(0f, 0f, 842f, 595f, paint)

        // Borders
        paint.color = Color.rgb(217, 119, 6) // Gold border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawRect(20f, 20f, 822f, 575f, paint)

        paint.color = Color.rgb(4, 120, 87) // Inner emerald border
        paint.strokeWidth = 2f
        canvas.drawRect(30f, 30f, 812f, 565f, paint)
        paint.style = Paint.Style.FILL

        // Header Title
        paint.color = Color.rgb(4, 120, 87)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 28f
        canvas.drawText("شَهَادَةُ شُكْرٍ وَتَقْدِيرٍ وَإِتْقَانٍ قُرْآنِيّ", 842f / 2, 90f, paint)

        paint.color = Color.rgb(217, 119, 6)
        paint.textSize = 14f
        canvas.drawText("«خَيْرُكُمْ مَنْ تَعَلَّمَ القُرْآنَ وَعَلَّمَهُ»", 842f / 2, 125f, paint)

        // Body
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 16f
        canvas.drawText("تَشْهَدُ إِدَارَةُ مَجْمَعِ حَلَقَاتِ مَنَارَةِ القُرْآنِ الكَرِيمِ بِأَنَّ الطَّالِبَ الْمُتَمَيِّزَ:", 842f / 2, 190f, paint)

        // Student Name Highlight
        paint.color = Color.rgb(4, 120, 87)
        paint.textSize = 30f
        canvas.drawText(certificate.studentName, 842f / 2, 245f, paint)

        // Reason & Halaqah
        paint.color = Color.rgb(51, 65, 85)
        paint.textSize = 16f
        canvas.drawText("الْمُنْتَسِبَ إِلَى: ${certificate.halaqahName}", 842f / 2, 290f, paint)
        canvas.drawText("قَدْ تَمَيَّزَ فِي: ${certificate.title}", 842f / 2, 325f, paint)
        canvas.drawText(certificate.reason, 842f / 2, 360f, paint)

        // Points
        paint.color = Color.rgb(217, 119, 6)
        paint.textSize = 15f
        canvas.drawText("وذلك لتحقيقه رصيد تميز بلغ (${certificate.pointsThreshold}) نقطة إتقان", 842f / 2, 400f, paint)

        // Signatures
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 13f
        canvas.drawText("المحفظ المشرف: ${certificate.teacherName}", 200f, 480f, paint)
        canvas.drawText("المشرف العام: ${certificate.supervisorName}", 642f, 480f, paint)
        canvas.drawText("تاريخ الإصدار: ${certificate.issueDate}", 842f / 2, 530f, paint)

        document.finishPage(page)

        val dir = File(context.cacheDir, "pdf_certificates")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "cert_${certificate.id}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        file
    }

    fun openOrSharePdf(context: Context, file: File, title: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
        }
    }
}
