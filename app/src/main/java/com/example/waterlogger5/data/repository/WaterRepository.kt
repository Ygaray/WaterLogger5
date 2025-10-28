package com.example.waterlogger5.data.repository

import com.example.waterlogger5.data.local.entity.DailySummary
import com.example.waterlogger5.data.local.entity.WaterEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for water intake data operations.
 * Defines the contract for data access in the app following Clean Architecture principles.
 */
interface WaterRepository {

    // ==================== WATER INTAKE OPERATIONS ====================

    /**
     * Add a water intake entry.
     * @param amountMl Amount of water in milliliters
     * @param date Date string in YYYY-MM-DD format
     * @param timestamp Unix timestamp in milliseconds
     */
    suspend fun addWaterIntake(amountMl: Int, date: String, timestamp: Long)

    /**
     * Delete a water entry.
     */
    suspend fun deleteWaterEntry(entry: WaterEntry)

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get today's total water intake as a reactive Flow.
     * Updates automatically when data changes.
     */
    fun getTodayTotal(date: String): Flow<Int>

    /**
     * Get all daily summaries ordered by date (most recent first).
     * Returns as a reactive Flow for UI updates.
     */
    fun getAllDailySummaries(): Flow<List<DailySummary>>

    /**
     * Get all entries for a specific date.
     */
    suspend fun getEntriesForDate(date: String): List<WaterEntry>

    // ==================== SETTINGS OPERATIONS ====================

    /**
     * Get the daily water goal as a reactive Flow.
     */
    fun getDailyGoal(): Flow<Int>

    /**
     * Update the daily water goal.
     */
    suspend fun updateDailyGoal(goalMl: Int)

    /**
     * Reset daily goal to default (2000ml).
     */
    suspend fun resetDailyGoalToDefault()
}
