package com.example.waterlogger5.data.local

import androidx.room.*
import com.example.waterlogger5.data.local.entity.DailySummary
import com.example.waterlogger5.data.local.entity.WaterEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for water intake database operations.
 * Provides methods for inserting, querying, and managing water entries and daily summaries.
 */
@Dao
interface WaterDao {

    // ==================== INSERT OPERATIONS ====================

    /**
     * Insert a single water entry.
     */
    @Insert
    suspend fun insertEntry(entry: WaterEntry): Long

    /**
     * Insert or replace a daily summary.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(summary: DailySummary)

    // ==================== QUERY OPERATIONS (REACTIVE) ====================

    /**
     * Get today's total water intake as a reactive Flow.
     * Updates automatically when data changes.
     */
    @Query("SELECT COALESCE(SUM(amount_ml), 0) FROM water_entries WHERE date = :date")
    fun getTotalForDate(date: String): Flow<Int>

    /**
     * Get all daily summaries ordered by date (most recent first).
     * Returns as a reactive Flow for UI updates.
     */
    @Query("SELECT * FROM daily_summary ORDER BY date DESC")
    fun getAllDailySummaries(): Flow<List<DailySummary>>

    // ==================== QUERY OPERATIONS (SYNCHRONOUS) ====================

    /**
     * Get total for a specific date synchronously (for use in transactions).
     */
    @Query("SELECT COALESCE(SUM(amount_ml), 0) FROM water_entries WHERE date = :date")
    suspend fun getTotalForDateSync(date: String): Int

    /**
     * Get all entries for a specific date.
     */
    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp DESC")
    suspend fun getEntriesForDate(date: String): List<WaterEntry>

    /**
     * Get count of entries for a specific date.
     */
    @Query("SELECT COUNT(*) FROM water_entries WHERE date = :date")
    suspend fun getEntryCountForDate(date: String): Int

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete a specific water entry.
     */
    @Delete
    suspend fun deleteEntry(entry: WaterEntry)

    /**
     * Delete a specific water entry by ID.
     */
    @Query("DELETE FROM water_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: Long)

    // ==================== TRANSACTION OPERATIONS ====================

    /**
     * Add a water intake entry and update the daily summary in a single transaction.
     * Ensures data consistency between entries and summaries.
     */
    @Transaction
    suspend fun addWaterIntakeTransaction(entry: WaterEntry) {
        // Insert the entry
        insertEntry(entry)

        // Calculate updated totals for the day
        val total = getTotalForDateSync(entry.date)
        val count = getEntryCountForDate(entry.date)

        // Update or create daily summary
        insertDailySummary(
            DailySummary(
                date = entry.date,
                totalMl = total,
                entryCount = count,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    /**
     * Delete an entry and update the daily summary in a single transaction.
     */
    @Transaction
    suspend fun deleteEntryTransaction(entry: WaterEntry) {
        // Delete the entry
        deleteEntry(entry)

        // Recalculate totals for the day
        val total = getTotalForDateSync(entry.date)
        val count = getEntryCountForDate(entry.date)

        // Update daily summary (or it will be 0 if no entries left)
        insertDailySummary(
            DailySummary(
                date = entry.date,
                totalMl = total,
                entryCount = count,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }
}
