package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VideoProjectEntity::class,
        GeneratedClipEntity::class,
        ExportHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ViralClipDatabase : RoomDatabase() {
    abstract fun viralClipDao(): ViralClipDao

    companion object {
        @Volatile
        private var INSTANCE: ViralClipDatabase? = null

        fun getInstance(context: Context): ViralClipDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ViralClipDatabase::class.java,
                    "viral_clip_studio.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
