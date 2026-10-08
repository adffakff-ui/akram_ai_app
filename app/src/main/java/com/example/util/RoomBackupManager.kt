package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.db.QuranDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object RoomBackupManager {

    private const val BACKUP_DIR_NAME = "quran_room_backups"

    fun getBackupDirectory(context: Context): File {
        val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, BACKUP_DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun exportDatabaseToJson(
        context: Context,
        halaqat: List<Halaqah>,
        students: List<Student>,
        dailyRecords: List<DailyRecord>,
        certificates: List<Certificate>,
        scheduleEvents: List<ScheduleEvent>
    ): File = withContext(Dispatchers.IO) {
        val rootJson = JSONObject()
        rootJson.put("app", "منارة القرآن")
        rootJson.put("version", "4.0")
        rootJson.put("exportTimestamp", System.currentTimeMillis())
        rootJson.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

        // Halaqat
        val halaqatArray = JSONArray()
        halaqat.forEach { h ->
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            obj.put("studentsLevel", h.studentsLevel)
            obj.put("responsibleSheikh", h.responsibleSheikh)
            obj.put("category", h.category.name)
            obj.put("meetingTime", h.meetingTime)
            obj.put("description", h.description)
            halaqatArray.put(obj)
        }
        rootJson.put("halaqat", halaqatArray)

        // Students
        val studentsArray = JSONArray()
        students.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("gender", s.gender.name)
            obj.put("halaqahId", s.halaqahId)
            obj.put("parentName", s.parentName)
            obj.put("parentPhone", s.parentPhone)
            obj.put("parentCode", s.parentCode)
            obj.put("currentJuz", s.currentJuz)
            obj.put("currentSurah", s.currentSurah)
            obj.put("totalPoints", s.totalPoints)
            obj.put("notes", s.notes)
            studentsArray.put(obj)
        }
        rootJson.put("students", studentsArray)

        // Daily Records
        val recordsArray = JSONArray()
        dailyRecords.forEach { r ->
            val obj = JSONObject()
            obj.put("studentId", r.studentId)
            obj.put("date", r.date)
            obj.put("attendanceStatus", r.attendanceStatus.name)
            obj.put("newHifzSurah", r.newHifzSurah)
            obj.put("newHifzPages", r.newHifzPages)
            obj.put("newHifzRating", r.newHifzRating.name)
            obj.put("murajaahSurah", r.murajaahSurah)
            obj.put("murajaahRating", r.murajaahRating.name)
            obj.put("pointsEarned", r.pointsEarned)
            obj.put("teacherNotes", r.teacherNotes)
            recordsArray.put(obj)
        }
        rootJson.put("dailyRecords", recordsArray)

        // Schedule Events
        val eventsArray = JSONArray()
        scheduleEvents.forEach { e ->
            val obj = JSONObject()
            obj.put("title", e.title)
            obj.put("eventType", e.eventType.name)
            obj.put("halaqahId", e.halaqahId ?: 0)
            obj.put("date", e.date)
            obj.put("startTime", e.startTime)
            obj.put("endTime", e.endTime)
            obj.put("location", e.location)
            obj.put("notes", e.notes)
            eventsArray.put(obj)
        }
        rootJson.put("scheduleEvents", eventsArray)

        val file = File(getBackupDirectory(context), "quran_backup_${System.currentTimeMillis()}.json")
        file.writeText(rootJson.toString(2), Charsets.UTF_8)
        file
    }

    suspend fun exportStudentsToCsv(
        context: Context,
        students: List<Student>,
        halaqat: List<Halaqah>
    ): File = withContext(Dispatchers.IO) {
        val halaqahMap = halaqat.associateBy { it.id }
        val sb = StringBuilder()
        sb.append("م,اسم الطالب,الجنس,الحلقة,الجزء الحالي,السورة الحالية,النقاط,كود ولي الأمر,رقم هاتف الولي\n")

        students.forEachIndexed { index, s ->
            val hName = halaqahMap[s.halaqahId]?.name ?: "بدون حلقة"
            val genderName = if (s.gender == Gender.MALE) "بنين" else "بنات"
            sb.append("${index + 1},\"${s.name}\",\"$genderName\",\"$hName\",${s.currentJuz},\"${s.currentSurah}\",${s.totalPoints},\"${s.parentCode}\",\"${s.parentPhone}\"\n")
        }

        val file = File(getBackupDirectory(context), "students_report_${System.currentTimeMillis()}.csv")
        file.writeText(sb.toString(), Charsets.UTF_8)
        file
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            // Fallback plain share
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "نسخة احتياطية من تطبيق منارة القرآن: ${file.name}")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, title))
        }
    }
}
