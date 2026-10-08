package com.example.data.db

import androidx.room.*
import com.example.data.model.Halaqah
import kotlinx.coroutines.flow.Flow

/**
 * واجهة الوصول لبيانات حلقات تحفيظ القرآن الكريم (Halaqah DAO)
 * تدعم كافة عمليات CRUD الأساسية والاستعلامات المتقدمة
 */
@Dao
interface HalaqahDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHalaqah(halaqah: Halaqah): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHalaqat(halaqat: List<Halaqah>): List<Long>

    @Update
    suspend fun updateHalaqah(halaqah: Halaqah)

    @Delete
    suspend fun deleteHalaqah(halaqah: Halaqah)

    @Query("SELECT * FROM halaqat ORDER BY id ASC")
    fun getAllHalaqat(): Flow<List<Halaqah>>

    @Query("SELECT * FROM halaqat WHERE id = :id LIMIT 1")
    suspend fun getHalaqahById(id: Long): Halaqah?

    @Query("SELECT * FROM halaqat WHERE responsibleSheikh LIKE '%' || :sheikh || '%'")
    fun getHalaqatByResponsibleSheikh(sheikh: String): Flow<List<Halaqah>>

    @Query("SELECT * FROM halaqat WHERE studentsLevel = :level")
    fun getHalaqatByStudentsLevel(level: String): Flow<List<Halaqah>>

    @Query("DELETE FROM halaqat")
    suspend fun clearAllHalaqat()
}
