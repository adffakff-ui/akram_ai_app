package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.QuranDatabase
import com.example.data.model.*
import com.example.data.repository.QuranRepository
import com.example.util.PushNotificationHelper
import com.example.util.SpeechEvaluatorHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: QuranDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, QuranDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `verify app name resource`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("منارة القرآن", appName)
    }

    @Test
    fun `verify room database student and halaqah operations`() = runBlocking {
        val dao = database.quranDao()

        val halaqahId = dao.insertHalaqah(
            Halaqah(
                name = "حلقة الإمام عاصم",
                studentsLevel = "المستوى المتقدم",
                responsibleSheikh = "الشيخ عبد الرحمن"
            )
        )
        assertTrue(halaqahId > 0)

        val studentId = dao.insertStudent(
            Student(
                name = "عبد الله المنصور",
                gender = Gender.MALE,
                halaqahId = halaqahId,
                parentName = "أحمد",
                parentPhone = "0501112233",
                parentCode = "1001",
                totalPoints = 50
            )
        )
        assertTrue(studentId > 0)

        val students = dao.getAllStudents().first()
        assertEquals(1, students.size)
        assertEquals("عبد الله المنصور", students[0].name)
    }

    @Test
    fun `verify interactive calendar schedule event creation`() = runBlocking {
        val dao = database.quranDao()

        val eventId = dao.insertScheduleEvent(
            ScheduleEvent(
                title = "امتحان جزء عم النهائي",
                eventType = ScheduleEventType.EXAM,
                date = "2026-10-15",
                startTime = "17:00",
                endTime = "18:30",
                location = "قاعة الاختبارات",
                examinerName = "الشيخ فهد"
            )
        )
        assertTrue(eventId > 0)

        val events = dao.getAllScheduleEvents().first()
        assertEquals(1, events.size)
        assertEquals(ScheduleEventType.EXAM, events[0].eventType)
        assertEquals("2026-10-15", events[0].date)
    }

    @Test
    fun `verify push notification templates generator`() {
        val student = Student(
            id = 1,
            name = "عمر الحربي",
            gender = Gender.MALE,
            halaqahId = 1,
            parentName = "سليمان",
            parentPhone = "0501234567",
            parentCode = "1003",
            currentSurah = "الإسراء"
        )

        val templates = PushNotificationHelper.getTemplatesForStudent(student)
        assertTrue(templates.isNotEmpty())
        assertTrue(templates.any { it.type == NotificationType.HIFZ_ENCOURAGEMENT })
        assertTrue(templates.any { it.type == NotificationType.ATTENDANCE })
        assertTrue(templates[0].defaultMessageTemplate.contains("عمر الحربي") || templates[0].defaultTitle.contains("عمر الحربي"))
    }

    @Test
    fun `verify speech recitation evaluation logic`() {
        val result = SpeechEvaluatorHelper.evaluateRecitation(
            spokenText = "عم يتساءلون عن النبا العظيم",
            surahName = "النبأ",
            expectedAyahText = "عَمَّ يَتَسَاءَلُونَ عَنِ النَّبَإِ الْعَظِيمِ"
        )

        assertTrue(result.accuracyPercentage >= 90)
        assertTrue(result.matchedWordsCount > 0)
    }

    @Test
    fun `verify supervisor halaqat full CRUD and teacher halaqah isolation`() = runBlocking {
        val dao = database.quranDao()

        // 1. Supervisor creates 2 halaqat
        val h1Id = dao.insertHalaqah(
            Halaqah(name = "حلقة الإمام عاصم", responsibleSheikh = "الشيخ عبد الرحمن")
        )
        val h2Id = dao.insertHalaqah(
            Halaqah(name = "حلقة الإمام نافع", responsibleSheikh = "الشيخ ماهر")
        )

        dao.insertStudent(
            Student(name = "طالب 1", halaqahId = h1Id, parentName = "أحمد", parentPhone = "0501", parentCode = "101")
        )
        dao.insertStudent(
            Student(name = "طالب 2", halaqahId = h2Id, parentName = "خالد", parentPhone = "0502", parentCode = "102")
        )

        // Teacher of Halaqah 1 sees ONLY their students
        val teacherH1Students = dao.getStudentsByHalaqah(h1Id).first()
        assertEquals(1, teacherH1Students.size)
        assertEquals("طالب 1", teacherH1Students[0].name)

        // Teacher of Halaqah 2 sees ONLY their students
        val teacherH2Students = dao.getStudentsByHalaqah(h2Id).first()
        assertEquals(1, teacherH2Students.size)
        assertEquals("طالب 2", teacherH2Students[0].name)

        // Supervisor / Developer sees ALL students across all halaqat
        val allStudents = dao.getAllStudents().first()
        assertEquals(2, allStudents.size)

        // Supervisor updates Halaqah 1
        val halaqah1 = dao.getHalaqahById(h1Id)!!
        dao.updateHalaqah(halaqah1.copy(name = "حلقة الإمام عاصم المحدثة"))
        val updatedH1 = dao.getHalaqahById(h1Id)!!
        assertEquals("حلقة الإمام عاصم المحدثة", updatedH1.name)

        // Verify User Roles enum has SUPERVISOR
        assertTrue(UserRole.entries.contains(UserRole.SUPERVISOR))
        assertTrue(UserRole.entries.contains(UserRole.DEVELOPER))
        assertTrue(UserRole.entries.contains(UserRole.TEACHER))
    }
}
