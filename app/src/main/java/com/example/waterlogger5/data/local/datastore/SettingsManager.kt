package com.example.waterlogger5.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "water_logger_settings")

/**
 * Manages app settings using DataStore Preferences.
 * Provides type-safe access to user preferences with reactive Flow updates.
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        // Preference keys
        private val DAILY_GOAL_KEY = intPreferencesKey("daily_goal_ml")

        // Default values
        const val DEFAULT_DAILY_GOAL_ML = 2000
    }

    /**
     * Get the daily water goal as a reactive Flow.
     * Emits updates whenever the goal changes.
     */
    val dailyGoalFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[DAILY_GOAL_KEY] ?: DEFAULT_DAILY_GOAL_ML
    }

    /**
     * Update the daily water goal.
     * @param goalMl The new daily goal in milliliters
     */
    suspend fun updateDailyGoal(goalMl: Int) {
        dataStore.edit { preferences ->
            preferences[DAILY_GOAL_KEY] = goalMl
        }
    }

    /**
     * Reset daily goal to default value (2000ml).
     */
    suspend fun resetDailyGoalToDefault() {
        updateDailyGoal(DEFAULT_DAILY_GOAL_ML)
    }

    /**
     * Get the current daily goal synchronously (suspending function).
     * Use this when you need the current value once, not reactively.
     */
    suspend fun getCurrentDailyGoal(): Int {
        var goal = DEFAULT_DAILY_GOAL_ML
        dataStore.data.map { preferences ->
            goal = preferences[DAILY_GOAL_KEY] ?: DEFAULT_DAILY_GOAL_ML
        }
        return goal
    }
}
