package com.example.waterlogger5.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a daily summary of water intake.
 * Stores aggregated data for each day for efficient history queries.
 */
@Entity(tableName = "daily_summary")
data class DailySummary(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD

    @ColumnInfo(name = "total_ml")
    val totalMl: Int,

    @ColumnInfo(name = "entry_count")
    val entryCount: Int,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
