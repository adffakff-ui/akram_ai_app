package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.QuranDatabase
import com.example.data.model.*
import com.example.data.repository.QuranRepository
import com.example.sync.SyncStatus
import com.example.util.PdfReportGenerator
import com.example.util.PushNotificationHelper
import com.example.util.RecitationEvaluationResult
import com.example.util.RoomBackupManager
import com.example.util.SpeechEvaluatorHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QuranDatabase.getDatabase(application)
    val repository = QuranRepository(database.quranDao(), application)

    // Roles & Settings
    private val _currentRole = MutableStateFlow(UserRole.TEACHER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Authentication Gate & Protection Screen State
    private val _isAuthGatePassed = MutableStateFlow(false)
    val isAuthGatePassed: StateFlow<Boolean> = _isAuthGatePassed.asStateFlow()

    private val _authenticatedSubscriber = MutableStateFlow<SubscriberRegistration?>(null)
    val authenticatedSubscriber: StateFlow<SubscriberRegistration?> = _authenticatedSubscriber.asStateFlow()

    // Fullscreen Mode
    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    // Active Teacher Assigned Halaqah (Strict Role Isolation)
    private val _currentTeacherHalaqahId = MutableStateFlow<Long?>(null)
    val currentTeacherHalaqahId: StateFlow<Long?> = _currentTeacherHalaqahId.asStateFlow()

    private val _currentLanguage = MutableStateFlow("ar") // "ar" or "en"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _isNightMode = MutableStateFlow(false)
    val isNightMode: StateFlow<Boolean> = _isNightMode.asStateFlow()

    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    // Filters
    private val _selectedHalaqahId = MutableStateFlow<Long?>(null)
    val selectedHalaqahId: StateFlow<Long?> = _selectedHalaqahId.asStateFlow()

    private val _genderFilter = MutableStateFlow<Gender?>(null)
    val genderFilter: StateFlow<Gender?> = _genderFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    )
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    private val _selectedChatStudentId = MutableStateFlow<Long?>(null)
    val selectedChatStudentId: StateFlow<Long?> = _selectedChatStudentId.asStateFlow()

    // Toast Messages
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Data Flows from Repository
    val allHalaqat: StateFlow<List<Halaqah>> = repository.allHalaqat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDailyRecords: StateFlow<List<DailyRecord>> = repository.allDailyRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParentNotes: StateFlow<List<ParentNote>> = repository.allParentNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCertificates: StateFlow<List<Certificate>> = repository.allCertificates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPushNotifications: StateFlow<List<PushNotification>> = repository.allPushNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScheduleEvents: StateFlow<List<ScheduleEvent>> = repository.allScheduleEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<InvoiceRecord>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val registeredDevices: StateFlow<List<RegisteredDevice>> = repository.getAllRegisteredDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subscriber Registrations & Identity Verification Approval Flow
    val allSubscribers: StateFlow<List<SubscriberRegistration>> = repository.getAllSubscribers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSubscribersCount: StateFlow<Int> = allSubscribers
        .map { list -> list.count { it.approvalStatus == SubscriberApprovalStatus.PENDING } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Sync Flows
    val syncStatus: StateFlow<SyncStatus> = repository.syncManager.syncStatus
    val lastSyncTime: StateFlow<String> = repository.syncManager.lastSyncTime
    val pendingSyncCount: StateFlow<Int> = repository.syncManager.pendingSyncCount
    val isOnline: StateFlow<Boolean> = repository.syncManager.isOnline

    // Halaqat visible to current role (Teachers see only their assigned halaqah; Supervisor & Developer see all)
    val halaqatForCurrentRole: StateFlow<List<Halaqah>> = combine(
        allHalaqat,
        _currentRole,
        _currentTeacherHalaqahId
    ) { halaqat, role, teacherHalaqahId ->
        if (role == UserRole.TEACHER) {
            val effectiveId = teacherHalaqahId ?: halaqat.firstOrNull()?.id
            if (effectiveId != null) halaqat.filter { it.id == effectiveId } else halaqat
        } else {
            halaqat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Students (Enforces strict privacy: Teacher only sees their own students)
    val filteredStudents: StateFlow<List<Student>> = combine(
        allStudents,
        _currentRole,
        _currentTeacherHalaqahId,
        _selectedHalaqahId,
        _genderFilter,
        _searchQuery
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val students = args[0] as List<Student>
        val role = args[1] as UserRole
        val teacherHalaqahId = args[2] as Long?
        val selectedHalaqah = args[3] as Long?
        val gender = args[4] as Gender?
        val query = args[5] as String

        students.filter { s ->
            val halaqahMatch = if (role == UserRole.TEACHER) {
                val effectiveHalaqahId = teacherHalaqahId ?: allHalaqat.value.firstOrNull()?.id
                effectiveHalaqahId == null || s.halaqahId == effectiveHalaqahId
            } else {
                selectedHalaqah == null || s.halaqahId == selectedHalaqah
            }

            halaqahMatch &&
            (gender == null || s.gender == gender) &&
            (query.isBlank() || s.name.contains(query, ignoreCase = true) ||
                    s.notes.contains(query, ignoreCase = true) ||
                    s.currentSurah.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
        viewModelScope.launch {
            _toastMessage.emit(if (_isFullscreen.value) "تم تفعيل ملء الشاشة ⛶" else "تم إيقاف ملء الشاشة")
        }
    }

    fun setFullscreen(enabled: Boolean) {
        _isFullscreen.value = enabled
    }

    fun setTeacherHalaqahId(id: Long) {
        _currentTeacherHalaqahId.value = id
        val hName = allHalaqat.value.find { it.id == id }?.name ?: ""
        viewModelScope.launch {
            _toastMessage.emit("تم تثبيت حلقتك كمعلم: $hName (صلاحية خاصة بحلقتك فقط) 🔒")
        }
    }

    // Halaqah Management (Full CRUD for Supervisor & Developer)
    fun addHalaqah(halaqah: Halaqah) {
        viewModelScope.launch {
            repository.insertHalaqah(halaqah)
            _toastMessage.emit("تم إضافة الحلقة (${halaqah.name}) بنجاح 🕌")
        }
    }

    fun updateHalaqah(halaqah: Halaqah) {
        viewModelScope.launch {
            repository.updateHalaqah(halaqah)
            _toastMessage.emit("تم تحديث بيانات الحلقة ✅")
        }
    }

    fun deleteHalaqah(halaqah: Halaqah) {
        viewModelScope.launch {
            repository.deleteHalaqah(halaqah)
            _toastMessage.emit("تم حذف الحلقة من المنظومة")
        }
    }

    // Subscriber Approval & Identity Verification Actions (Exclusively for Supervisor & Developer)
    fun approveSubscriber(
        subscriber: SubscriberRegistration,
        approverName: String = "المشرف العام"
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
            val updated = subscriber.copy(
                approvalStatus = SubscriberApprovalStatus.APPROVED,
                approvedBy = approverName,
                approvalDate = dateStr,
                identityVerificationNotes = subscriber.identityVerificationNotes.ifBlank { "تم التحقق من إثبات الهوية والموافقة على الاشتراك رسمياً" }
            )
            repository.updateSubscriber(updated)

            // If approved as a student and halaqah is assigned, automatically enroll as a student
            if (subscriber.requestedRole == UserRole.STUDENT && subscriber.halaqahId != null) {
                val newStudent = Student(
                    name = subscriber.fullName,
                    gender = Gender.MALE,
                    halaqahId = subscriber.halaqahId,
                    parentName = "ولي أمر ${subscriber.fullName}",
                    parentPhone = subscriber.phone,
                    parentCode = "${(1000..9999).random()}",
                    currentJuz = 30,
                    currentSurah = "النبأ",
                    notes = "مشترك تم التحقق من هويته واعتماده رسمياً (${subscriber.identityDocumentType}: ${subscriber.nationalIdOrPassport})"
                )
                repository.insertStudent(newStudent)
            }

            _toastMessage.emit("تم اعتماد المشترك (${subscriber.fullName}) وإثبات هويته بنجاح ✅")
        }
    }

    fun rejectSubscriber(subscriber: SubscriberRegistration, reason: String) {
        viewModelScope.launch {
            val updated = subscriber.copy(
                approvalStatus = SubscriberApprovalStatus.REJECTED,
                rejectionReason = reason.ifBlank { "لم يستوفِ شروط إثبات الهوية المطلوبة" }
            )
            repository.updateSubscriber(updated)
            _toastMessage.emit("تم رفض طلب الاشتراك للمشترك (${subscriber.fullName})")
        }
    }

    fun submitNewSubscriberApplication(
        fullName: String,
        nationalId: String,
        phone: String,
        requestedRole: UserRole,
        halaqahId: Long?,
        halaqahName: String,
        documentType: String,
        documentNumber: String,
        verificationNotes: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
            val application = SubscriberRegistration(
                fullName = fullName,
                nationalIdOrPassport = nationalId,
                phone = phone,
                requestedRole = requestedRole,
                halaqahId = halaqahId,
                halaqahName = halaqahName,
                identityDocumentType = documentType,
                identityDocumentNumber = documentNumber.ifBlank { nationalId },
                identityVerificationNotes = verificationNotes,
                approvalStatus = SubscriberApprovalStatus.PENDING,
                registrationDate = dateStr
            )
            repository.insertSubscriber(application)
            _toastMessage.emit("تم إرسال طلب الاشتراك وإثبات الهوية بنجاح، بانتظار موافقة المشرف أو المطور ⏳")
        }
    }

    fun deleteSubscriber(subscriber: SubscriberRegistration) {
        viewModelScope.launch {
            repository.deleteSubscriber(subscriber)
            _toastMessage.emit("تم حذف سجل المشترك")
        }
    }

    // Authentication Gate & Protection Screen Actions
    fun authenticateSubscriber(subscriber: SubscriberRegistration): Boolean {
        _authenticatedSubscriber.value = subscriber
        return when (subscriber.approvalStatus) {
            SubscriberApprovalStatus.APPROVED -> {
                _currentRole.value = subscriber.requestedRole
                _isAuthGatePassed.value = true
                viewModelScope.launch {
                    _toastMessage.emit("مرحباً بك يا ${subscriber.fullName}، تم التحقق من هويتك بنجاح ✅")
                }
                true
            }
            SubscriberApprovalStatus.PENDING -> {
                _isAuthGatePassed.value = false
                viewModelScope.launch {
                    _toastMessage.emit("حسابك معلق (Pending) ⏳ قيد تدقيق الهوية والموافقة من قِبل المشرف أو المطور")
                }
                false
            }
            SubscriberApprovalStatus.REJECTED -> {
                _isAuthGatePassed.value = false
                viewModelScope.launch {
                    _toastMessage.emit("طلب الاشتراك مرفوض ❌: ${subscriber.rejectionReason.ifBlank { "لم يتم استيفاء شروط الهوية" }}")
                }
                false
            }
        }
    }

    fun authenticateAsAdmin(role: UserRole) {
        _authenticatedSubscriber.value = null
        _currentRole.value = role
        _isAuthGatePassed.value = true
        viewModelScope.launch {
            _toastMessage.emit("تم تسجيل الدخول بصلاحية: ${role.arabicTitle} 🛡️")
        }
    }

    fun passAuthGateAsGuest() {
        _authenticatedSubscriber.value = null
        _isAuthGatePassed.value = true
        viewModelScope.launch {
            _toastMessage.emit("تم الدخول في وضع الاطلاع العام (الزائر) 👁️")
        }
    }

    fun lockToAuthGate() {
        _isAuthGatePassed.value = false
        _authenticatedSubscriber.value = null
        viewModelScope.launch {
            _toastMessage.emit("تم قفل الشاشة والعودة لبوابة المصادقة والحماية 🔒")
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        viewModelScope.launch {
            _toastMessage.emit("تم التبديل إلى دور: ${role.arabicTitle}")
        }
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == "ar") "en" else "ar"
    }

    fun toggleNightMode() {
        _isNightMode.value = !_isNightMode.value
    }

    fun toggleHighContrast() {
        _isHighContrast.value = !_isHighContrast.value
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
    }

    fun setSelectedHalaqah(id: Long?) {
        _selectedHalaqahId.value = id
    }

    fun setGenderFilter(gender: Gender?) {
        _genderFilter.value = gender
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCalendarDate(date: String) {
        _selectedCalendarDate.value = date
    }

    fun setSelectedChatStudentId(studentId: Long?) {
        _selectedChatStudentId.value = studentId
    }

    // Student CRUD
    fun addStudent(student: Student) {
        viewModelScope.launch {
            repository.insertStudent(student)
            _toastMessage.emit("تم إضافة الطالب ${student.name} بنجاح ✅")
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _toastMessage.emit("تم تحديث بيانات الطالب ✅")
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _toastMessage.emit("تم حذف الطالب من الحلقة")
        }
    }

    // Daily Record
    fun recordDailyEvaluation(record: DailyRecord, student: Student) {
        viewModelScope.launch {
            repository.insertDailyRecord(record)
            val updatedStudent = student.copy(
                totalPoints = student.totalPoints + record.pointsEarned
            )
            repository.updateStudent(updatedStudent)
            _toastMessage.emit("تم رصد الحفظ والتسميع للطالب ${student.name} (+${record.pointsEarned} نقطة) 🌟")
        }
    }

    // Push Notifications
    fun sendPushNotificationToStudent(
        student: Student,
        title: String,
        message: String,
        type: NotificationType,
        postSystemNotification: Boolean = true
    ) {
        viewModelScope.launch {
            val halaqah = allHalaqat.value.find { it.id == student.halaqahId }
            val notification = PushNotification(
                studentId = student.id,
                studentName = student.name,
                halaqahId = student.halaqahId,
                halaqahName = halaqah?.name ?: "حلقة التحفيظ",
                title = title,
                message = message,
                notificationType = type,
                senderTeacherName = halaqah?.responsibleSheikh ?: "أستاذ الحلقة",
                isSystemNotificationSent = postSystemNotification,
                isRead = false
            )
            repository.insertPushNotification(notification)

            if (postSystemNotification) {
                PushNotificationHelper.sendStudentNotification(
                    getApplication(),
                    notification
                )
            }
            _toastMessage.emit("تم إرسال التنبيه الفوري للطالب ${student.name} بنجاح 🔔")
        }
    }

    fun deletePushNotification(id: Long) {
        viewModelScope.launch {
            repository.deletePushNotification(id)
            _toastMessage.emit("تم حذف التنبيه")
        }
    }

    // Schedule Events
    fun addScheduleEvent(event: ScheduleEvent) {
        viewModelScope.launch {
            repository.insertScheduleEvent(event)
            _toastMessage.emit("تمت جدولة الموعد في التقويم 📅: ${event.title}")
        }
    }

    fun deleteScheduleEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteScheduleEvent(id)
            _toastMessage.emit("تم حذف الموعد من التقويم")
        }
    }

    fun toggleEventCompleted(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setScheduleEventCompleted(id, isCompleted)
            _toastMessage.emit(if (isCompleted) "تم إنجاز الموعد بنجاح ✅" else "تم إلغاء حالة الإنجاز")
        }
    }

    // Sync Action
    fun triggerSyncNow() {
        viewModelScope.launch {
            val success = repository.syncManager.syncPendingChanges()
            if (success) {
                _toastMessage.emit("تمت المزامنة بنجاح مع السيرفر 🟢")
            } else {
                _toastMessage.emit("تعذرت المزامنة - تأكد من اتصال الإنترنت ⚠️")
            }
        }
    }

    // Export & Backup
    fun exportBackupJson(context: Context) {
        viewModelScope.launch {
            try {
                val file = RoomBackupManager.exportDatabaseToJson(
                    context,
                    allHalaqat.value,
                    allStudents.value,
                    allDailyRecords.value,
                    allCertificates.value,
                    allScheduleEvents.value
                )
                RoomBackupManager.shareFile(
                    context,
                    file,
                    "application/json",
                    "نسخة احتياطية من قاعدة بيانات منارة القرآن"
                )
                _toastMessage.emit("تم إنشاء النسخة الاحتياطية بنجاح 💾")
            } catch (e: Exception) {
                _toastMessage.emit("حدث خطأ أثناء النسخ: ${e.message}")
            }
        }
    }

    fun exportHifzCsv(context: Context) {
        viewModelScope.launch {
            try {
                val file = RoomBackupManager.exportStudentsToCsv(
                    context,
                    allStudents.value,
                    allHalaqat.value
                )
                RoomBackupManager.shareFile(
                    context,
                    file,
                    "text/csv",
                    "تقرير طلاب الحلقات والحفظ"
                )
                _toastMessage.emit("تم تصدير ملف CSV بنجاح 📊")
            } catch (e: Exception) {
                _toastMessage.emit("حدث خطأ أثناء التصدير: ${e.message}")
            }
        }
    }

    fun exportStudentReportPdf(context: Context, student: Student) {
        viewModelScope.launch {
            try {
                val halaqah = allHalaqat.value.find { it.id == student.halaqahId }
                val records = allDailyRecords.value.filter { it.studentId == student.id }
                val file = PdfReportGenerator.generateStudentReportPdf(
                    context,
                    student,
                    halaqah?.name ?: "الحلقة القرآنية",
                    records
                )
                PdfReportGenerator.openOrSharePdf(
                    context,
                    file,
                    "تقرير أداء الطالب ${student.name} (PDF)"
                )
                _toastMessage.emit("تم استخراج تقرير PDF للطالب بنجاح 📄")
            } catch (e: Exception) {
                _toastMessage.emit("حدث خطأ أثناء استخراج PDF: ${e.message}")
            }
        }
    }

    fun exportCertificatePdf(context: Context, certificate: Certificate) {
        viewModelScope.launch {
            try {
                val file = PdfReportGenerator.generateCertificatePdf(context, certificate)
                PdfReportGenerator.openOrSharePdf(
                    context,
                    file,
                    "شهادة شكر وتقدير - ${certificate.studentName}"
                )
                _toastMessage.emit("تم استخراج الشهادة الرقمية بنجاح 📜")
            } catch (e: Exception) {
                _toastMessage.emit("حدث خطأ أثناء استخراج الشهادة: ${e.message}")
            }
        }
    }

    // Invoices & Payment
    fun payInvoice(invoice: InvoiceRecord) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
            repository.markInvoiceAsPaid(invoice.id, dateStr)
            _toastMessage.emit("تم سداد الفاتورة (${invoice.title}) بنجاح عبر بوابة الدفع الآمنة 💳")
        }
    }

    // Todos
    fun toggleTodo(todo: StudentTodo) {
        viewModelScope.launch {
            repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun addTodo(studentId: Long, title: String) {
        viewModelScope.launch {
            repository.insertTodo(
                StudentTodo(
                    studentId = studentId,
                    title = title,
                    dueDate = "اليوم",
                    priority = "عالي"
                )
            )
            _toastMessage.emit("تمت إضافة الهدف لقائمة المهام ✅")
        }
    }

    fun deleteTodo(id: Long) {
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }

    // Recitation Evaluation
    fun evaluateRecitation(
        spokenText: String,
        surahName: String,
        expectedAyahText: String
    ): RecitationEvaluationResult {
        return SpeechEvaluatorHelper.evaluateRecitation(
            spokenText,
            surahName,
            expectedAyahText
        )
    }
}
