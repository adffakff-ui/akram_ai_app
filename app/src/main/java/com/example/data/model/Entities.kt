package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HalaqahCategory(val arabicTitle: String) {
    GENERAL("حلقة عامة"),
    HIFZ("حلقة الحفظ المكثف"),
    MURAJAAH("حلقة المراجعة والتثبيت"),
    TAJWEED("حلقة الإتقان والتجويد"),
    KIDS("حلقة البراعم والأشبال")
}

enum class Gender(val arabicTitle: String) {
    MALE("طالب (بنين)"),
    FEMALE("طالبة (بنات)")
}

enum class AttendanceStatus(val arabicTitle: String) {
    PRESENT("حاضر"),
    ABSENT("غائب"),
    EXCUSED("مستأذن"),
    LATE("متأخر")
}

enum class EvaluationRating(val arabicTitle: String, val score: Int, val points: Int) {
    EXCELLENT("ممتاز 🌟", 100, 10),
    VERY_GOOD("جيد جداً ✨", 85, 7),
    GOOD("جيد 👍", 70, 5),
    ACCEPTABLE("مقبول ⚠️", 50, 2),
    NEEDS_PRACTICE("يحتاج متابعة 🔄", 30, 0),
    NONE("لم يُسمّع", 0, 0)
}

enum class UserRole(val arabicTitle: String) {
    SUPERVISOR("المشرف العام (صلاحية شاملة)"),
    DEVELOPER("المطور التقني (إدارة النظام)"),
    TEACHER("المعلم / المحفظ (صلاحية الحلقة فقط)"),
    PARENT("ولي الأمر"),
    STUDENT("الطالب")
}

@Entity(tableName = "halaqat")
data class Halaqah(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val studentsLevel: String = "المستوى المتوسط",
    val responsibleSheikh: String = "الشيخ المشرف",
    val category: HalaqahCategory = HalaqahCategory.GENERAL,
    val teacherPhone: String = "",
    val meetingTime: String = "العصر - المغرب",
    val description: String = ""
) {
    val teacherName: String get() = responsibleSheikh.ifBlank { "الشيخ المشرف" }
}

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: Gender = Gender.MALE,
    val halaqahId: Long,
    val parentName: String = "ولي الأمر",
    val parentPhone: String = "",
    val parentCode: String = "1234",
    val currentJuz: Int = 30,
    val currentSurah: String = "النبأ",
    val level: String = "المستوى المتوسط",
    val totalPoints: Int = 0,
    val isActive: Boolean = true,
    val notes: String = "",
    val memorizationGoalPagesPerDay: Float = 1.0f,
    val revisionGoalPagesPerDay: Float = 5.0f
) {
    val calculatedLevel: String
        get() = when {
            totalPoints >= 100 || currentJuz <= 5 -> "متقدم 🌟"
            totalPoints >= 40 || currentJuz <= 15 -> "متوسط ✨"
            else -> "مبتدئ 📖"
        }
}

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val date: String, // yyyy-MM-dd
    val attendanceStatus: AttendanceStatus = AttendanceStatus.PRESENT,
    val newHifzSurah: String = "",
    val newHifzFromAyah: Int = 1,
    val newHifzToAyah: Int = 1,
    val newHifzPages: Float = 1.0f,
    val newHifzRating: EvaluationRating = EvaluationRating.EXCELLENT,
    val newHifzMistakes: Int = 0,
    val murajaahSurah: String = "",
    val murajaahFromAyah: Int = 1,
    val murajaahToAyah: Int = 1,
    val murajaahRating: EvaluationRating = EvaluationRating.VERY_GOOD,
    val tajweedRating: EvaluationRating = EvaluationRating.EXCELLENT,
    val nextAssignment: String = "",
    val pointsEarned: Int = 10,
    val teacherNotes: String = "",
    val notifiedParent: Boolean = false,
    val audioNotePath: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val senderRole: UserRole,
    val senderName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "parent_notes")
data class ParentNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val parentName: String,
    val noteTitle: String,
    val noteContent: String,
    val date: String,
    val replyTeacherName: String = "",
    val replyContent: String = "",
    val replyDate: String = "",
    val isReplied: Boolean = false
)

@Entity(tableName = "certificates")
data class Certificate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val title: String,
    val reason: String,
    val pointsThreshold: Int,
    val issueDate: String,
    val halaqahName: String,
    val teacherName: String,
    val supervisorName: String = "إدارة مجمع حلقات منارة القرآن الكريم"
)

@Entity(tableName = "developer_settings")
data class DeveloperSetting(
    @PrimaryKey val key: String,
    val value: String
)

enum class NotificationType(
    val arabicTitle: String,
    val iconName: String,
    val colorHex: Long
) {
    HIFZ_ENCOURAGEMENT("تشجيع على الإتقان 🌟", "star", 0xFF10B981),
    HOMEWORK("واجب الحفظ والمراجعة 📖", "book", 0xFF3B82F6),
    ATTENDANCE("تنبيه الحضور والغياب ⏰", "clock", 0xFFF59E0B),
    PARENT_REMINDER("تذكير لولي الأمر 👨‍👧‍👦", "family", 0xFF8B5CF6),
    EXAM_PREPARATION("استعداد لاختبار جزء 🎯", "target", 0xFFEC4899),
    GENERAL("تنبيه إداري عام 📢", "bell", 0xFF0D9488)
}

@Entity(tableName = "push_notifications")
data class PushNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val halaqahId: Long,
    val halaqahName: String = "",
    val title: String,
    val message: String,
    val notificationType: NotificationType = NotificationType.GENERAL,
    val senderTeacherName: String = "أستاذ الحلقة",
    val timestamp: Long = System.currentTimeMillis(),
    val isSystemNotificationSent: Boolean = true,
    val isRead: Boolean = false
)

enum class ScheduleEventType(
    val arabicTitle: String,
    val iconName: String,
    val colorHex: Long
) {
    HALAQAH_SESSION("حلقة قرآنية دورية 🕌", "school", 0xFF059669),
    EXAM("امتحان وتقييم تجويد وحفظ 🎯", "assignment", 0xFFDC2626),
    MAJOR_REVISION("جلسة مراجعة كبرى 📖", "menu_book", 0xFF2563EB),
    COMPETITION("مسابقة قرآنية 🏆", "emoji_events", 0xFFD97706),
    PARENTS_MEETING("لقاء أولياء الأمور 👨‍👧‍👦", "groups", 0xFF7C3AED),
    GENERAL_EVENT("نشاط أو حفل قرآني 🌟", "celebration", 0xFF0891B2)
}

@Entity(tableName = "schedule_events")
data class ScheduleEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val eventType: ScheduleEventType = ScheduleEventType.HALAQAH_SESSION,
    val halaqahId: Long? = null,
    val halaqahName: String = "",
    val studentId: Long? = null,
    val studentName: String = "",
    val date: String, // yyyy-MM-dd
    val startTime: String = "16:30",
    val endTime: String = "18:00",
    val location: String = "مسجد الحلقة - القاعة الرئيسية",
    val examinerName: String = "المحفظ المشرف",
    val targetSurahOrJuz: String = "",
    val notes: String = "",
    val isCompleted: Boolean = false,
    val reminderMinutes: Int = 30,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_sync_queue")
data class OfflineSyncQueueItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // CREATE, UPDATE, DELETE
    val entityType: String, // DAILY_RECORD, ATTENDANCE, PUSH_NOTIFICATION, EVENT
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val isSynced: Boolean = false
)

@Entity(tableName = "invoices")
data class InvoiceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val title: String,
    val amount: Double,
    val dueDate: String,
    val paidDate: String = "",
    val isPaid: Boolean = false,
    val paymentMethod: String = "بطاقة مدى"
)

@Entity(tableName = "student_todos")
data class StudentTodo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val priority: String = "متوسط"
)

@Entity(tableName = "registered_devices")
data class RegisteredDevice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceName: String,
    val deviceModel: String,
    val lastActiveDate: String,
    val isCurrentDevice: Boolean = false,
    val isBiometricEnabled: Boolean = true
)

@Entity(tableName = "user_permissions")
data class UserPermission(
    @PrimaryKey val role: String,
    val canEditRecords: Boolean = true,
    val canExportPdf: Boolean = true,
    val canSendPush: Boolean = true,
    val canScheduleCalendar: Boolean = true,
    val canManageUsers: Boolean = false
)

enum class SubscriberApprovalStatus(val arabicTitle: String) {
    PENDING("قيد المراجعة والتدقيق ⏳"),
    APPROVED("تمت الموافقة والاعتماد ✅"),
    REJECTED("مرفوض ❌")
}

@Entity(tableName = "subscribers")
data class SubscriberRegistration(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val nationalIdOrPassport: String, // رقم الهوية الوطنية أو الإقامة أو جواز السفر
    val phone: String,
    val requestedRole: UserRole = UserRole.STUDENT,
    val halaqahId: Long? = null,
    val halaqahName: String = "",
    val identityDocumentType: String = "هوية وطنية / إقامة", // نوع إثبات الهوية
    val identityDocumentNumber: String = "", // رقم الوثيقة المثبتة
    val identityVerificationNotes: String = "", // ملاحظات تدقيق الهوية
    val approvalStatus: SubscriberApprovalStatus = SubscriberApprovalStatus.PENDING,
    val approvedBy: String = "", // تم الاعتماد بواسطة (المشرف العام أو المطور)
    val approvalDate: String = "",
    val registrationDate: String = "",
    val rejectionReason: String = ""
)

data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val versesCount: Int,
    val juz: Int,
    val revelationType: String,
    val audioUrl: String = ""
)

object QuranData {
    val surahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", 7, 1, "مكية"),
        Surah(2, "البقرة", "Al-Baqarah", 286, 1, "مدنية"),
        Surah(3, "آل عمران", "Aal-Imran", 200, 3, "مدنية"),
        Surah(4, "النساء", "An-Nisa", 176, 4, "مدنية"),
        Surah(5, "المائدة", "Al-Ma'idah", 120, 6, "مدنية"),
        Surah(6, "الأنعام", "Al-An'am", 165, 7, "مكية"),
        Surah(7, "الأعراف", "Al-A'raf", 206, 8, "مكية"),
        Surah(8, "الأنفال", "Al-Anfal", 75, 9, "مدنية"),
        Surah(9, "التوبة", "At-Tawbah", 129, 10, "مدنية"),
        Surah(10, "يونس", "Yunus", 109, 11, "مكية"),
        Surah(11, "هود", "Hud", 123, 11, "مكية"),
        Surah(12, "يوسف", "Yusuf", 111, 12, "مكية"),
        Surah(13, "الرعد", "Ar-Ra'd", 43, 13, "مدنية"),
        Surah(14, "إبراهيم", "Ibrahim", 52, 13, "مكية"),
        Surah(15, "الحجر", "Al-Hijr", 99, 14, "مكية"),
        Surah(16, "النحل", "An-Nahl", 128, 14, "مكية"),
        Surah(17, "الإسراء", "Al-Isra", 111, 15, "مكية"),
        Surah(18, "الكهف", "Al-Kahf", 110, 15, "مكية"),
        Surah(19, "مريم", "Maryam", 98, 16, "مكية"),
        Surah(20, "طه", "Ta-Ha", 135, 16, "مكية"),
        Surah(36, "يس", "Ya-Sin", 83, 22, "مكية"),
        Surah(55, "الرحمن", "Ar-Rahman", 78, 27, "مدنية"),
        Surah(56, "الواقعة", "Al-Waqi'ah", 96, 27, "مكية"),
        Surah(67, "الملك", "Al-Mulk", 30, 29, "مكية"),
        Surah(78, "النبأ", "An-Naba", 40, 30, "مكية"),
        Surah(79, "النازعات", "An-Nazi'at", 46, 30, "مكية"),
        Surah(80, "عبس", "Abasa", 42, 30, "مكية"),
        Surah(81, "التكوير", "At-Takwir", 29, 30, "مكية"),
        Surah(82, "الانفطار", "Al-Infitar", 19, 30, "مكية"),
        Surah(83, "المطففين", "Al-Mutaffifin", 36, 30, "مكية"),
        Surah(84, "الانشقاق", "Al-Inshiqaq", 25, 30, "مكية"),
        Surah(85, "البروج", "Al-Buruj", 22, 30, "مكية"),
        Surah(86, "الطارق", "At-Tariq", 17, 30, "مكية"),
        Surah(87, "الأعلى", "Al-A'la", 19, 30, "مكية"),
        Surah(88, "الغاشية", "Al-Ghashiyah", 26, 30, "مكية"),
        Surah(89, "الفجر", "Al-Fajr", 30, 30, "مكية"),
        Surah(90, "البلد", "Al-Balad", 20, 30, "مكية"),
        Surah(91, "الشمس", "Ash-Shams", 15, 30, "مكية"),
        Surah(92, "الليل", "Al-Layl", 21, 30, "مكية"),
        Surah(93, "الضحى", "Ad-Duha", 11, 30, "مكية"),
        Surah(94, "الشرح", "Ash-Sharh", 8, 30, "مكية"),
        Surah(95, "التين", "At-Tin", 8, 30, "مكية"),
        Surah(96, "العلق", "Al-Alaq", 19, 30, "مكية"),
        Surah(97, "القدر", "Al-Qadr", 5, 30, "مكية"),
        Surah(98, "البينة", "Al-Bayyinah", 8, 30, "مدنية"),
        Surah(99, "الزلزلة", "Az-Zalzalah", 8, 30, "مدنية"),
        Surah(100, "العاديات", "Al-Adiyat", 11, 30, "مكية"),
        Surah(101, "القارعة", "Al-Qari'ah", 11, 30, "مكية"),
        Surah(102, "التكاثر", "At-Takathur", 8, 30, "مكية"),
        Surah(103, "العصر", "Al-Asr", 3, 30, "مكية"),
        Surah(104, "الهمزة", "Al-Humazah", 9, 30, "مكية"),
        Surah(105, "الفيل", "Al-Fil", 5, 30, "مكية"),
        Surah(106, "قريش", "Quraysh", 4, 30, "مكية"),
        Surah(107, "الماعون", "Al-Ma'un", 7, 30, "مكية"),
        Surah(108, "الكوثر", "Al-Kawthar", 3, 30, "مكية"),
        Surah(109, "الكافرون", "Al-Kafirun", 6, 30, "مكية"),
        Surah(110, "النصر", "An-Nasr", 3, 30, "مدنية"),
        Surah(111, "المسد", "Al-Masad", 5, 30, "مكية"),
        Surah(112, "الإخلاص", "Al-Ikhlas", 4, 30, "مكية"),
        Surah(113, "الفلق", "Al-Falaq", 5, 30, "مكية"),
        Surah(114, "الناس", "An-Nas", 6, 30, "مكية")
    )
}
