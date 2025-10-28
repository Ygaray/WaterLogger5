package com.example.waterlogger5.di

import android.content.Context
import androidx.room.Room
import com.example.waterlogger5.data.local.WaterDao
import com.example.waterlogger5.data.local.WaterDatabase
import com.example.waterlogger5.data.repository.WaterRepository
import com.example.waterlogger5.data.repository.WaterRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database and repository dependencies.
 * Provides Room Database, DAO, and Repository instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Room database instance.
     * Singleton to ensure only one instance across the app.
     */
    @Provides
    @Singleton
    fun provideWaterDatabase(
        @ApplicationContext context: Context
    ): WaterDatabase {
        return Room.databaseBuilder(
            context,
            WaterDatabase::class.java,
            WaterDatabase.DATABASE_NAME
        ).build()
    }

    /**
     * Provides the WaterDao from the database.
     */
    @Provides
    @Singleton
    fun provideWaterDao(database: WaterDatabase): WaterDao {
        return database.waterDao()
    }
}

/**
 * Separate module for repository binding.
 * Uses @Binds for efficient dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the WaterRepositoryImpl to the WaterRepository interface.
     * This allows Hilt to inject WaterRepository and provide WaterRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindWaterRepository(
        repositoryImpl: WaterRepositoryImpl
    ): WaterRepository
}
