package com.example.waterlogger5.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing a single water intake entry.
 * Stores individual water consumption records with timestamps.
 */
@Entity(
    tableName = "water_entries",
    indices = [Index(value = ["date"])] // Index for faster date-based queries
)
data class WaterEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "amount_ml")
    val amountMl: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "date")
    val date: String, // Format: YYYY-MM-DD for easy querying

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
