package com.example.waterlogger5.data.repository

import com.example.waterlogger5.data.local.WaterDao
import com.example.waterlogger5.data.local.datastore.SettingsManager
import com.example.waterlogger5.data.local.entity.DailySummary
import com.example.waterlogger5.data.local.entity.WaterEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WaterRepository.
 * Coordinates between Room database and DataStore for data operations.
 */
@Singleton
class WaterRepositoryImpl @Inject constructor(
    private val waterDao: WaterDao,
    private val settingsManager: SettingsManager
) : WaterRepository {

    // ==================== WATER INTAKE OPERATIONS ====================

    override suspend fun addWaterIntake(amountMl: Int, date: String, timestamp: Long) {
        val entry = WaterEntry(
            amountMl = amountMl,
            timestamp = timestamp,
            date = date
        )
        // Use transaction to ensure consistency between entries and summaries
        waterDao.addWaterIntakeTransaction(entry)
    }

    override suspend fun deleteWaterEntry(entry: WaterEntry) {
        // Use transaction to update summary after deletion
        waterDao.deleteEntryTransaction(entry)
    }

    // ==================== QUERY OPERATIONS ====================

    override fun getTodayTotal(date: String): Flow<Int> {
        return waterDao.getTotalForDate(date)
    }

    override fun getAllDailySummaries(): Flow<List<DailySummary>> {
        return waterDao.getAllDailySummaries()
    }

    override suspend fun getEntriesForDate(date: String): List<WaterEntry> {
        return waterDao.getEntriesForDate(date)
    }

    // ==================== SETTINGS OPERATIONS ====================

    override fun getDailyGoal(): Flow<Int> {
        return settingsManager.dailyGoalFlow
    }

    override suspend fun updateDailyGoal(goalMl: Int) {
        settingsManager.updateDailyGoal(goalMl)
    }

    override suspend fun resetDailyGoalToDefault() {
        settingsManager.resetDailyGoalToDefault()
    }
}
