package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        Halaqah::class,
        Student::class,
        DailyRecord::class,
        ChatMessage::class,
        ParentNote::class,
        Certificate::class,
        DeveloperSetting::class,
        PushNotification::class,
        ScheduleEvent::class,
        OfflineSyncQueueItem::class,
        InvoiceRecord::class,
        StudentTodo::class,
        RegisteredDevice::class,
        UserPermission::class,
        SubscriberRegistration::class
    ],
    version = 5,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {

    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getDatabase(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_manara_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
