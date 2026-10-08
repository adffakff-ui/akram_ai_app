package com.example.data.db

import androidx.room.*
import com.example.data.model.DailyRecord
import kotlinx.coroutines.flow.Flow

/**
 * واجهة الوصول لبيانات سجلات الحفظ والمراجعة والحضور اليومي (DailyRecord DAO)
 * تدعم عمليات CRUD الأساسية والاستعلام بحسب الطالب، التاريخ، والتقييم
 */
@Dao
interface DailyRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRecord(record: DailyRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDailyRecords(records: List<DailyRecord>): List<Long>

    @Update
    suspend fun updateDailyRecord(record: DailyRecord)

    @Delete
    suspend fun deleteDailyRecord(record: DailyRecord)

    @Query("SELECT * FROM daily_records ORDER BY date DESC, id DESC")
    fun getAllDailyRecords(): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getRecordsForStudent(studentId: Long): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getRecordForStudentAndDate(studentId: Long, date: String): DailyRecord?

    @Query("SELECT * FROM daily_records WHERE date = :date ORDER BY id DESC")
    fun getRecordsForDate(date: String): Flow<List<DailyRecord>>

    @Query("DELETE FROM daily_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM daily_records")
    suspend fun clearAllDailyRecords()
}
