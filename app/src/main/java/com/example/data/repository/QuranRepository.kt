package com.example.data.repository

import android.content.Context
import com.example.data.db.QuranDao
import com.example.data.model.*
import com.example.sync.OfflineSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranRepository(
    private val dao: QuranDao,
    context: Context
) {
    val syncManager = OfflineSyncManager(context, dao)

    val allHalaqat: Flow<List<Halaqah>> = dao.getAllHalaqat()
    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    val allDailyRecords: Flow<List<DailyRecord>> = dao.getAllDailyRecords()
    val allParentNotes: Flow<List<ParentNote>> = dao.getAllParentNotes()
    val allCertificates: Flow<List<Certificate>> = dao.getAllCertificates()
    val allSettings: Flow<List<DeveloperSetting>> = dao.getAllSettings()
    val allPushNotifications: Flow<List<PushNotification>> = dao.getAllPushNotifications()
    val allScheduleEvents: Flow<List<ScheduleEvent>> = dao.getAllScheduleEvents()
    val allInvoices: Flow<List<InvoiceRecord>> = dao.getAllInvoices()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateIfEmpty()
        }
    }

    private suspend fun prepopulateIfEmpty() {
        val existingHalaqat = allHalaqat.first()
        if (existingHalaqat.isNotEmpty()) return

        // Populate Halaqat
        val h1Id = dao.insertHalaqah(
            Halaqah(
                name = "حلقة الإمام عاصم (حفظ مكثف)",
                studentsLevel = "المستوى المتقدم",
                responsibleSheikh = "الشيخ عبد الرحمن السديس",
                category = HalaqahCategory.HIFZ,
                teacherPhone = "0551234567",
                meetingTime = "بعد صلاة العصر - المسجد الكبير",
                description = "حلقة مخصصة للحفظ المتقن مع أحكام التجويد ومخارج الحروف"
            )
        )

        val h2Id = dao.insertHalaqah(
            Halaqah(
                name = "حلقة الإمام نافع (المراجعة والتثبيت)",
                studentsLevel = "المستوى المتوسط",
                responsibleSheikh = "الشيخ ماهر المعيقلي",
                category = HalaqahCategory.MURAJAAH,
                teacherPhone = "0559876543",
                meetingTime = "بعد صلاة المغرب - قاعة التحفيظ 2",
                description = "تثبيت المحفوظ ومراجعة خمسة أجزاء أسبوعياً"
            )
        )

        val h3Id = dao.insertHalaqah(
            Halaqah(
                name = "حلقة البراعم والأشبال (جزء عم)",
                studentsLevel = "المستوى التمهيدي",
                responsibleSheikh = "الشيخ فيصل بن جميل",
                category = HalaqahCategory.KIDS,
                teacherPhone = "0554433221",
                meetingTime = "العصر - قاعة الصغار",
                description = "تلقين قصار السور وغرس الآداب والأخلاق القرآنية"
            )
        )

        // Populate Students
        val s1 = dao.insertStudent(
            Student(
                name = "عبد الله بن أحمد المنصور",
                gender = Gender.MALE,
                halaqahId = h1Id,
                parentName = "أحمد المنصور",
                parentPhone = "0501112233",
                parentCode = "1001",
                currentJuz = 3,
                currentSurah = "آل عمران",
                level = "المستوى المتقدم",
                totalPoints = 145,
                notes = "ما شاء الله متقن لأحكام التجويد ومخارج الحروف",
                memorizationGoalPagesPerDay = 2.0f,
                revisionGoalPagesPerDay = 10.0f
            )
        )

        val s2 = dao.insertStudent(
            Student(
                name = "محمد بن خالد العتيبي",
                gender = Gender.MALE,
                halaqahId = h1Id,
                parentName = "خالد العتيبي",
                parentPhone = "0502223344",
                parentCode = "1002",
                currentJuz = 7,
                currentSurah = "الأنعام",
                level = "المستوى المتقدم",
                totalPoints = 120,
                notes = "مواظب على الحضور ومتفوق في سرعة الحفظ",
                memorizationGoalPagesPerDay = 1.5f,
                revisionGoalPagesPerDay = 8.0f
            )
        )

        val s3 = dao.insertStudent(
            Student(
                name = "عمر بن سليمان الحربي",
                gender = Gender.MALE,
                halaqahId = h2Id,
                parentName = "سليمان الحربي",
                parentPhone = "0503334455",
                parentCode = "1003",
                currentJuz = 15,
                currentSurah = "الإسراء",
                level = "المستوى المتوسط",
                totalPoints = 85,
                notes = "يحتاج مزيداً من التركيز في أحكام المدود",
                memorizationGoalPagesPerDay = 1.0f,
                revisionGoalPagesPerDay = 5.0f
            )
        )

        val s4 = dao.insertStudent(
            Student(
                name = "سارة بنت فهد القحطاني",
                gender = Gender.FEMALE,
                halaqahId = h2Id,
                parentName = "فهد القحطاني",
                parentPhone = "0504445566",
                parentCode = "1004",
                currentJuz = 20,
                currentSurah = "طه",
                level = "المستوى المتوسط",
                totalPoints = 95,
                notes = "صوت جميل وترتيل متقن وسرعة بديهة",
                memorizationGoalPagesPerDay = 1.0f,
                revisionGoalPagesPerDay = 5.0f
            )
        )

        val s5 = dao.insertStudent(
            Student(
                name = "يوسف بن إبراهيم الدوسري",
                gender = Gender.MALE,
                halaqahId = h3Id,
                parentName = "إبراهيم الدوسري",
                parentPhone = "0505556677",
                parentCode = "1005",
                currentJuz = 30,
                currentSurah = "النبأ",
                level = "المستوى التمهيدي",
                totalPoints = 50,
                notes = "براعم موهوب في حفظ قصار السور",
                memorizationGoalPagesPerDay = 0.5f,
                revisionGoalPagesPerDay = 2.0f
            )
        )

        // Populate Daily Records for charts & evaluation
        dao.insertDailyRecord(
            DailyRecord(
                studentId = s1,
                date = "2026-10-06",
                attendanceStatus = AttendanceStatus.PRESENT,
                newHifzSurah = "البقرة",
                newHifzFromAyah = 250,
                newHifzToAyah = 260,
                newHifzPages = 2.0f,
                newHifzRating = EvaluationRating.EXCELLENT,
                murajaahSurah = "الفاتحة والبقرة",
                murajaahRating = EvaluationRating.EXCELLENT,
                tajweedRating = EvaluationRating.EXCELLENT,
                pointsEarned = 15,
                teacherNotes = "تسميع ممتاز بدون أي خطأ مع إتقان الغنن"
            )
        )

        dao.insertDailyRecord(
            DailyRecord(
                studentId = s2,
                date = "2026-10-06",
                attendanceStatus = AttendanceStatus.PRESENT,
                newHifzSurah = "الأنعام",
                newHifzFromAyah = 1,
                newHifzToAyah = 20,
                newHifzPages = 1.5f,
                newHifzRating = EvaluationRating.VERY_GOOD,
                murajaahSurah = "المائدة",
                murajaahRating = EvaluationRating.VERY_GOOD,
                tajweedRating = EvaluationRating.EXCELLENT,
                pointsEarned = 12,
                teacherNotes = "أداء طيب وحفظ سليم"
            )
        )

        dao.insertDailyRecord(
            DailyRecord(
                studentId = s3,
                date = "2026-10-06",
                attendanceStatus = AttendanceStatus.PRESENT,
                newHifzSurah = "الإسراء",
                newHifzFromAyah = 1,
                newHifzToAyah = 15,
                newHifzPages = 1.0f,
                newHifzRating = EvaluationRating.GOOD,
                murajaahSurah = "النحل",
                murajaahRating = EvaluationRating.GOOD,
                tajweedRating = EvaluationRating.GOOD,
                pointsEarned = 8,
                teacherNotes = "يُرجى مراجعة أحكام النون الساكنة"
            )
        )

        // Prepopulate Schedule Events (Interactive Calendar)
        dao.insertScheduleEvent(
            ScheduleEvent(
                title = "حلقة التحفيظ والتثبيت المسائية",
                eventType = ScheduleEventType.HALAQAH_SESSION,
                halaqahId = h1Id,
                halaqahName = "حلقة الإمام عاصم",
                date = "2026-10-08",
                startTime = "16:30",
                endTime = "18:00",
                location = "المسجد الكبير - الرواق الشرقي",
                examinerName = "الشيخ عبد الرحمن",
                notes = "تسميع الوجه الأول من سورة آل عمران ومراجعة سورة البقرة"
            )
        )

        dao.insertScheduleEvent(
            ScheduleEvent(
                title = "امتحان تقييم الجزء الخامس عشر (الإسراء والكهف)",
                eventType = ScheduleEventType.EXAM,
                halaqahId = h2Id,
                halaqahName = "حلقة الإمام نافع",
                date = "2026-10-10",
                startTime = "17:00",
                endTime = "18:30",
                location = "قاعة الاختبارات والتقييم 1",
                examinerName = "لجنة الاختبارات المركزية",
                targetSurahOrJuz = "الجزء الخامس عشر كامل",
                notes = "اختبار رسمي مؤهل لشهادة الإتقان، الدرجة الصغرى 85"
            )
        )

        dao.insertScheduleEvent(
            ScheduleEvent(
                title = "جلسة مراجعة الختمة الكبرى",
                eventType = ScheduleEventType.MAJOR_REVISION,
                halaqahId = h1Id,
                halaqahName = "حلقة الإمام عاصم",
                date = "2026-10-12",
                startTime = "16:00",
                endTime = "18:00",
                location = "المسجد الكبير - المحراب",
                examinerName = "الشيخ المشرف",
                targetSurahOrJuz = "الأجزاء 1 إلى 5",
                notes = "جلسة سرد وتثبيت مستمر"
            )
        )

        // Prepopulate Certificate
        dao.insertCertificate(
            Certificate(
                studentId = s1,
                studentName = "عبد الله بن أحمد المنصور",
                title = "شهادة إتقان وتفوق في حفظ سورة البقرة",
                reason = "إتمام حفظ سورة البقرة وتسميعها غيباً بنسبة إتقان 98%",
                pointsThreshold = 100,
                issueDate = "2026-10-01",
                halaqahName = "حلقة الإمام عاصم",
                teacherName = "الشيخ عبد الرحمن السديس"
            )
        )

        // Prepopulate Push Notification
        dao.insertPushNotification(
            PushNotification(
                studentId = s1,
                studentName = "عبد الله بن أحمد المنصور",
                halaqahId = h1Id,
                halaqahName = "حلقة الإمام عاصم",
                title = "مبارك يا عبد الله حصولك على وسام الإتقان 🌟",
                message = "حصلت اليوم على 15 نقطة إضافية لإتقانك سورة البقرة بدون تردد. استمر في التميز!",
                notificationType = NotificationType.HIFZ_ENCOURAGEMENT,
                senderTeacherName = "الشيخ عبد الرحمن السديس",
                isSystemNotificationSent = true,
                isRead = false
            )
        )

        // Prepopulate Invoice for Parent
        dao.insertInvoice(
            InvoiceRecord(
                studentId = s1,
                studentName = "عبد الله بن أحمد المنصور",
                title = "رسوم الفصل الدراسي الأول لحلقات التحفيظ",
                amount = 150.0,
                dueDate = "2026-10-25",
                isPaid = false
            )
        )

        // Prepopulate Student Todos
        dao.insertTodo(
            StudentTodo(
                studentId = s1,
                title = "حفظ الوجهين 51 و 52 من سورة آل عمران",
                isCompleted = false,
                dueDate = "اليوم",
                priority = "عالي"
            )
        )
        dao.insertTodo(
            StudentTodo(
                studentId = s1,
                title = "مراجعة الربع الأول من سورة البقرة مع الوالد",
                isCompleted = true,
                dueDate = "أمس",
                priority = "متوسط"
            )
        )

        // Prepopulate Registered Device
        dao.insertRegisteredDevice(
            RegisteredDevice(
                deviceName = "هاتف المعلم الرئيسي (Pixel 9 Pro)",
                deviceModel = "Google Pixel",
                lastActiveDate = "نشط الآن",
                isCurrentDevice = true,
                isBiometricEnabled = true
            )
        )

        // Prepopulate Subscriber Registrations (Pending & Approved for Identity Verification CUJ)
        dao.insertSubscriber(
            SubscriberRegistration(
                fullName = "إبراهيم بن صالح العسيري",
                nationalIdOrPassport = "1098765432",
                phone = "0551239874",
                requestedRole = UserRole.STUDENT,
                halaqahId = h1Id,
                halaqahName = "حلقة الإمام عاصم (رواية حفص)",
                identityDocumentType = "بطاقة الهوية الوطنية",
                identityDocumentNumber = "SA-1098765432",
                identityVerificationNotes = "تم رفع صورة الهوية الوطنية سارية المفعول وبانتظار موافقة المشرف",
                approvalStatus = SubscriberApprovalStatus.PENDING,
                registrationDate = "2026-10-07"
            )
        )
        dao.insertSubscriber(
            SubscriberRegistration(
                fullName = "الشيخ عبد الله بن منصور التميمي",
                nationalIdOrPassport = "1023456789",
                phone = "0559988776",
                requestedRole = UserRole.TEACHER,
                halaqahId = h2Id,
                halaqahName = "حلقة الإمام نافع (رواية ورش)",
                identityDocumentType = "شهادة إجازة وإثبات هوية رسمية",
                identityDocumentNumber = "IJAZAH-2024-88",
                identityVerificationNotes = "معلم متطوع ومجاز بالقراءات، وثائق الهوية والإجازة مرفقة",
                approvalStatus = SubscriberApprovalStatus.PENDING,
                registrationDate = "2026-10-08"
            )
        )
        dao.insertSubscriber(
            SubscriberRegistration(
                fullName = "يوسف بن محمد الدوسري",
                nationalIdOrPassport = "1087654321",
                phone = "0554433221",
                requestedRole = UserRole.STUDENT,
                halaqahId = h1Id,
                halaqahName = "حلقة الإمام عاصم (رواية حفص)",
                identityDocumentType = "بطاقة الهوية الوطنية",
                identityDocumentNumber = "SA-1087654321",
                identityVerificationNotes = "تمت مراجعة الوثيقة ومطابقة البيانات الشخصية",
                approvalStatus = SubscriberApprovalStatus.APPROVED,
                approvedBy = "المشرف العام (الشيخ عبد الرحمن)",
                approvalDate = "2026-10-06",
                registrationDate = "2026-10-05"
            )
        )
    }

    // CRUD Methods
    suspend fun insertHalaqah(halaqah: Halaqah): Long {
        val id = dao.insertHalaqah(halaqah)
        syncManager.queueSyncAction("CREATE", "HALAQAH", "{\"id\":$id,\"name\":\"${halaqah.name}\"}")
        return id
    }

    suspend fun updateHalaqah(halaqah: Halaqah) {
        dao.updateHalaqah(halaqah)
        syncManager.queueSyncAction("UPDATE", "HALAQAH", "{\"id\":${halaqah.id},\"name\":\"${halaqah.name}\"}")
    }

    suspend fun deleteHalaqah(halaqah: Halaqah) {
        dao.deleteHalaqah(halaqah)
        syncManager.queueSyncAction("DELETE", "HALAQAH", "{\"id\":${halaqah.id}}")
    }

    fun getStudentsByHalaqah(halaqahId: Long): Flow<List<Student>> =
        dao.getStudentsByHalaqah(halaqahId)

    fun searchStudents(query: String): Flow<List<Student>> =
        dao.searchStudents(query)

    suspend fun insertStudent(student: Student): Long {
        val id = dao.insertStudent(student)
        syncManager.queueSyncAction("CREATE", "STUDENT", "{\"id\":$id,\"name\":\"${student.name}\"}")
        return id
    }

    suspend fun updateStudent(student: Student) {
        dao.updateStudent(student)
        syncManager.queueSyncAction("UPDATE", "STUDENT", "{\"id\":${student.id}}")
    }

    suspend fun deleteStudent(student: Student) {
        dao.deleteStudent(student)
        syncManager.queueSyncAction("DELETE", "STUDENT", "{\"id\":${student.id}}")
    }

    suspend fun insertDailyRecord(record: DailyRecord): Long {
        val id = dao.insertDailyRecord(record)
        syncManager.queueSyncAction("CREATE", "DAILY_RECORD", "{\"id\":$id,\"studentId\":${record.studentId}}")
        return id
    }

    suspend fun updateDailyRecord(record: DailyRecord) {
        dao.updateDailyRecord(record)
        syncManager.queueSyncAction("UPDATE", "DAILY_RECORD", "{\"id\":${record.id}}")
    }

    fun getRecordsForStudent(studentId: Long): Flow<List<DailyRecord>> =
        dao.getRecordsForStudent(studentId)

    fun getMessagesForStudent(studentId: Long): Flow<List<ChatMessage>> =
        dao.getMessagesForStudent(studentId)

    suspend fun insertChatMessage(message: ChatMessage): Long =
        dao.insertChatMessage(message)

    suspend fun insertParentNote(note: ParentNote): Long =
        dao.insertParentNote(note)

    suspend fun updateParentNote(note: ParentNote) =
        dao.updateParentNote(note)

    fun getPushNotificationsForStudent(studentId: Long): Flow<List<PushNotification>> =
        dao.getPushNotificationsForStudent(studentId)

    suspend fun insertPushNotification(notification: PushNotification): Long {
        val id = dao.insertPushNotification(notification)
        syncManager.queueSyncAction("CREATE", "PUSH_NOTIFICATION", "{\"id\":$id,\"title\":\"${notification.title}\"}")
        return id
    }

    suspend fun markPushNotificationAsRead(id: Long) =
        dao.markPushNotificationAsRead(id)

    suspend fun deletePushNotification(id: Long) =
        dao.deletePushNotification(id)

    // Schedule Events
    fun getScheduleEventsForDate(date: String): Flow<List<ScheduleEvent>> =
        dao.getScheduleEventsForDate(date)

    suspend fun insertScheduleEvent(event: ScheduleEvent): Long {
        val id = dao.insertScheduleEvent(event)
        syncManager.queueSyncAction("CREATE", "SCHEDULE_EVENT", "{\"id\":$id,\"title\":\"${event.title}\"}")
        return id
    }

    suspend fun updateScheduleEvent(event: ScheduleEvent) =
        dao.updateScheduleEvent(event)

    suspend fun deleteScheduleEvent(id: Long) =
        dao.deleteScheduleEvent(id)

    suspend fun setScheduleEventCompleted(id: Long, completed: Boolean) =
        dao.setScheduleEventCompleted(id, completed)

    // Invoices
    fun getInvoicesForStudent(studentId: Long): Flow<List<InvoiceRecord>> =
        dao.getInvoicesForStudent(studentId)

    suspend fun markInvoiceAsPaid(id: Long, paidDate: String) =
        dao.markInvoiceAsPaid(id, paidDate)

    // Todos
    fun getTodosForStudent(studentId: Long): Flow<List<StudentTodo>> =
        dao.getTodosForStudent(studentId)

    suspend fun insertTodo(todo: StudentTodo): Long =
        dao.insertTodo(todo)

    suspend fun updateTodo(todo: StudentTodo) =
        dao.updateTodo(todo)

    suspend fun deleteTodo(id: Long) =
        dao.deleteTodo(id)

    // Devices & Permissions
    fun getAllRegisteredDevices(): Flow<List<RegisteredDevice>> =
        dao.getAllRegisteredDevices()

    suspend fun insertRegisteredDevice(device: RegisteredDevice): Long =
        dao.insertRegisteredDevice(device)

    suspend fun deleteRegisteredDevice(device: RegisteredDevice) =
        dao.deleteRegisteredDevice(device)

    // Subscriber Registrations & Identity Verification Approval
    fun getAllSubscribers(): Flow<List<SubscriberRegistration>> =
        dao.getAllSubscribers()

    fun getSubscribersByStatus(status: SubscriberApprovalStatus): Flow<List<SubscriberRegistration>> =
        dao.getSubscribersByStatus(status)

    suspend fun insertSubscriber(subscriber: SubscriberRegistration): Long {
        val id = dao.insertSubscriber(subscriber)
        syncManager.queueSyncAction("CREATE", "SUBSCRIBER", "{\"id\":$id,\"name\":\"${subscriber.fullName}\"}")
        return id
    }

    suspend fun updateSubscriber(subscriber: SubscriberRegistration) {
        dao.updateSubscriber(subscriber)
        syncManager.queueSyncAction("UPDATE", "SUBSCRIBER", "{\"id\":${subscriber.id},\"status\":\"${subscriber.approvalStatus.name}\"}")
    }

    suspend fun deleteSubscriber(subscriber: SubscriberRegistration) {
        dao.deleteSubscriber(subscriber)
        syncManager.queueSyncAction("DELETE", "SUBSCRIBER", "{\"id\":${subscriber.id}}")
    }
}
