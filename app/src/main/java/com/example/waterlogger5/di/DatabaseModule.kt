package com.example.waterlogger5.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for database and repository dependencies.
 * Will be populated in Phase 2 with:
 * - Room Database provider
 * - DAO provider
 * - Repository bindings
 * - DataStore provider
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Database, DAO, and Repository dependencies will be provided here
}
