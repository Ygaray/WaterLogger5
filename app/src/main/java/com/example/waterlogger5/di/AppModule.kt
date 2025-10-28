package com.example.waterlogger5.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for app-level dependencies.
 * Will be populated in later phases as needed.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // App-level dependencies will be provided here
    // (e.g., shared utilities, app context, etc.)
}
