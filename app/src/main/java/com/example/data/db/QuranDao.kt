package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    // Halaqat
    @Query("SELECT * FROM halaqat ORDER BY id ASC")
    fun getAllHalaqat(): Flow<List<Halaqah>>

    @Query("SELECT * FROM halaqat WHERE id = :id LIMIT 1")
    suspend fun getHalaqahById(id: Long): Halaqah?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHalaqah(halaqah: Halaqah): Long

    @Update
    suspend fun updateHalaqah(halaqah: Halaqah)

    @Delete
    suspend fun deleteHalaqah(halaqah: Halaqah)

    // Students
    @Query("SELECT * FROM students ORDER BY totalPoints DESC, name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE halaqahId = :halaqahId ORDER BY name ASC")
    fun getStudentsByHalaqah(halaqahId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE parentCode = :code LIMIT 1")
    suspend fun getStudentByParentCode(code: String): Student?

    @Query("""
        SELECT * FROM students 
        WHERE name LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%' 
           OR currentSurah LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchStudents(query: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    // Daily Records
    @Query("SELECT * FROM daily_records ORDER BY date DESC, id DESC")
    fun getAllDailyRecords(): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE studentId = :studentId ORDER BY date DESC, id DESC")
    fun getRecordsForStudent(studentId: Long): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getRecordForStudentOnDate(studentId: Long, date: String): DailyRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRecord(record: DailyRecord): Long

    @Update
    suspend fun updateDailyRecord(record: DailyRecord)

    @Delete
    suspend fun deleteDailyRecord(record: DailyRecord)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE studentId = :studentId ORDER BY timestamp ASC")
    fun getMessagesForStudent(studentId: Long): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage): Long

    // Parent Notes
    @Query("SELECT * FROM parent_notes ORDER BY date DESC")
    fun getAllParentNotes(): Flow<List<ParentNote>>

    @Query("SELECT * FROM parent_notes WHERE studentId = :studentId ORDER BY date DESC")
    fun getNotesForStudent(studentId: Long): Flow<List<ParentNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParentNote(note: ParentNote): Long

    @Update
    suspend fun updateParentNote(note: ParentNote)

    // Certificates
    @Query("SELECT * FROM certificates ORDER BY issueDate DESC")
    fun getAllCertificates(): Flow<List<Certificate>>

    @Query("SELECT * FROM certificates WHERE studentId = :studentId ORDER BY issueDate DESC")
    fun getCertificatesForStudent(studentId: Long): Flow<List<Certificate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(cert: Certificate): Long

    // Developer Settings
    @Query("SELECT * FROM developer_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): DeveloperSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: DeveloperSetting)

    @Query("SELECT * FROM developer_settings")
    fun getAllSettings(): Flow<List<DeveloperSetting>>

    // Push Notifications
    @Query("SELECT * FROM push_notifications ORDER BY timestamp DESC")
    fun getAllPushNotifications(): Flow<List<PushNotification>>

    @Query("SELECT * FROM push_notifications WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getPushNotificationsForStudent(studentId: Long): Flow<List<PushNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPushNotification(notification: PushNotification): Long

    @Query("UPDATE push_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markPushNotificationAsRead(id: Long)

    @Query("DELETE FROM push_notifications WHERE id = :id")
    suspend fun deletePushNotification(id: Long)

    // Schedule Events (Calendar)
    @Query("SELECT * FROM schedule_events ORDER BY date ASC, startTime ASC")
    fun getAllScheduleEvents(): Flow<List<ScheduleEvent>>

    @Query("SELECT * FROM schedule_events WHERE date = :date ORDER BY startTime ASC")
    fun getScheduleEventsForDate(date: String): Flow<List<ScheduleEvent>>

    @Query("SELECT * FROM schedule_events WHERE date LIKE :yearMonth || '%' ORDER BY date ASC, startTime ASC")
    fun getScheduleEventsForMonth(yearMonth: String): Flow<List<ScheduleEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleEvent(event: ScheduleEvent): Long

    @Update
    suspend fun updateScheduleEvent(event: ScheduleEvent)

    @Query("DELETE FROM schedule_events WHERE id = :id")
    suspend fun deleteScheduleEvent(id: Long)

    @Query("UPDATE schedule_events SET isCompleted = :completed WHERE id = :id")
    suspend fun setScheduleEventCompleted(id: Long, completed: Boolean)

    // Offline Sync Queue
    @Query("SELECT * FROM offline_sync_queue WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getPendingSyncItems(): Flow<List<OfflineSyncQueueItem>>

    @Query("SELECT COUNT(*) FROM offline_sync_queue WHERE isSynced = 0")
    fun getPendingSyncCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueSyncItem(item: OfflineSyncQueueItem): Long

    @Query("DELETE FROM offline_sync_queue WHERE id = :id")
    suspend fun removeSyncItem(id: Long)

    @Query("DELETE FROM offline_sync_queue WHERE isSynced = 1")
    suspend fun clearSyncedItems()

    // Invoices (Tuition Payment)
    @Query("SELECT * FROM invoices ORDER BY dueDate DESC")
    fun getAllInvoices(): Flow<List<InvoiceRecord>>

    @Query("SELECT * FROM invoices WHERE studentId = :studentId ORDER BY dueDate DESC")
    fun getInvoicesForStudent(studentId: Long): Flow<List<InvoiceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceRecord): Long

    @Query("UPDATE invoices SET isPaid = 1, paidDate = :paidDate WHERE id = :id")
    suspend fun markInvoiceAsPaid(id: Long, paidDate: String)

    // Student Todos
    @Query("SELECT * FROM student_todos WHERE studentId = :studentId ORDER BY isCompleted ASC, id DESC")
    fun getTodosForStudent(studentId: Long): Flow<List<StudentTodo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: StudentTodo): Long

    @Update
    suspend fun updateTodo(todo: StudentTodo)

    @Query("DELETE FROM student_todos WHERE id = :id")
    suspend fun deleteTodo(id: Long)

    // Registered Devices
    @Query("SELECT * FROM registered_devices ORDER BY id ASC")
    fun getAllRegisteredDevices(): Flow<List<RegisteredDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegisteredDevice(device: RegisteredDevice): Long

    @Delete
    suspend fun deleteRegisteredDevice(device: RegisteredDevice)

    // User Permissions
    @Query("SELECT * FROM user_permissions WHERE role = :role LIMIT 1")
    suspend fun getPermissionForRole(role: String): UserPermission?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserPermission(permission: UserPermission)

    // Subscriber Registrations & ID Verification Approval Flow
    @Query("SELECT * FROM subscribers ORDER BY id DESC")
    fun getAllSubscribers(): Flow<List<SubscriberRegistration>>

    @Query("SELECT * FROM subscribers WHERE approvalStatus = :status ORDER BY id DESC")
    fun getSubscribersByStatus(status: SubscriberApprovalStatus): Flow<List<SubscriberRegistration>>

    @Query("SELECT * FROM subscribers WHERE id = :id LIMIT 1")
    suspend fun getSubscriberById(id: Long): SubscriberRegistration?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriber(subscriber: SubscriberRegistration): Long

    @Update
    suspend fun updateSubscriber(subscriber: SubscriberRegistration)

    @Delete
    suspend fun deleteSubscriber(subscriber: SubscriberRegistration)
}
