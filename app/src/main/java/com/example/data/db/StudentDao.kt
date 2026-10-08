package com.example.data.db

import androidx.room.*
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

/**
 * واجهة الوصول لبيانات طلاب وطالبات القرآن الكريم (Student DAO)
 * تدعم كافة عمليات CRUD الأساسية والتصفية بحسب الحلقة والنقاط وأكواد الأولياء
 */
@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStudents(students: List<Student>): List<Long>

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM students ORDER BY totalPoints DESC, name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE halaqahId = :halaqahId ORDER BY name ASC")
    fun getStudentsByHalaqah(halaqahId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE parentCode = :code OR parentPhone = :code LIMIT 1")
    suspend fun findStudentByParentCredential(code: String): Student?

    @Query("UPDATE students SET totalPoints = totalPoints + :points WHERE id = :studentId")
    suspend fun addStudentPoints(studentId: Long, points: Int)

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()
}
