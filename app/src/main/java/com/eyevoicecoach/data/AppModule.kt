package com.eyevoicecoach.data

import android.content.Context
import androidx.room.Room
import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.data.local.CoachDatabaseMigrations
import com.eyevoicecoach.data.preferences.EncryptedPreferenceDataStore
import com.eyevoicecoach.data.repository.RecordingRepositoryImpl
import com.eyevoicecoach.data.repository.AssessmentRepositoryImpl
import com.eyevoicecoach.data.repository.ProgramProgressRepositoryImpl
import com.eyevoicecoach.data.repository.AchievementRepositoryImpl
import com.eyevoicecoach.data.repository.SettingsRepositoryImpl
import com.eyevoicecoach.data.repository.TipRepositoryImpl
import com.eyevoicecoach.domain.repository.RecordingRepository
import com.eyevoicecoach.domain.repository.AssessmentRepository
import com.eyevoicecoach.domain.repository.ProgramProgressRepository
import com.eyevoicecoach.domain.repository.AchievementRepository
import com.eyevoicecoach.domain.repository.SettingsRepository
import com.eyevoicecoach.domain.repository.TipRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt bindings for private persistence and repository interfaces. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /** Binds the Room exercise repository. */
    @Binds @Singleton abstract fun bindTipRepository(implementation: TipRepositoryImpl): TipRepository

    /** Binds the Room recordings repository. */
    @Binds @Singleton abstract fun bindRecordingRepository(implementation: RecordingRepositoryImpl): RecordingRepository

    /** Binds the encrypted settings repository. */
    @Binds @Singleton abstract fun bindSettingsRepository(implementation: SettingsRepositoryImpl): SettingsRepository

    /** Binds private recording assessment storage. */
    @Binds @Singleton abstract fun bindAssessmentRepository(implementation: AssessmentRepositoryImpl): AssessmentRepository

    /** Binds local ready-made program progress storage. */
    @Binds @Singleton abstract fun bindProgramProgressRepository(implementation: ProgramProgressRepositoryImpl): ProgramProgressRepository

    /** Binds derived device-local achievements. */
    @Binds @Singleton abstract fun bindAchievementRepository(implementation: AchievementRepositoryImpl): AchievementRepository
}

/** Hilt providers for application-scoped local storage. */
@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    /** Creates the app-private Room database. */
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CoachDatabase =
        Room.databaseBuilder(context, CoachDatabase::class.java, "coach.db")
            .addMigrations(CoachDatabaseMigrations.MIGRATION_1_2)
            .build()

    /** Creates the keystore-encrypted preferences store. */
    @Provides @Singleton
    fun provideEncryptedStore(@ApplicationContext context: Context) = EncryptedPreferenceDataStore(context)

    /** Makes application context available to repositories that read assets. */
    @Provides @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context
}
