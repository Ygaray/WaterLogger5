package com.example.waterlogger5.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.waterlogger5.data.local.entity.DailySummary
import com.example.waterlogger5.data.local.entity.WaterEntry

/**
 * Room database for Water Logger app.
 * Contains water entries and daily summaries.
 */
@Database(
    entities = [WaterEntry::class, DailySummary::class],
    version = 1,
    exportSchema = false
)
abstract class WaterDatabase : RoomDatabase() {

    /**
     * Provides access to the WaterDao for database operations.
     */
    abstract fun waterDao(): WaterDao

    companion object {
        const val DATABASE_NAME = "water_logger_db"

        /**
         * Singleton instance for use outside of Hilt context (e.g., widgets).
         * Uses double-check locking for thread safety.
         */
        @Volatile
        private var INSTANCE: WaterDatabase? = null

        fun getDatabase(context: Context): WaterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaterDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
