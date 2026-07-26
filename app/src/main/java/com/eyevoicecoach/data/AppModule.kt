package com.eyevoicecoach.data

import android.content.Context
import androidx.room.Room
import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.data.preferences.EncryptedPreferenceDataStore
import com.eyevoicecoach.data.repository.RecordingRepositoryImpl
import com.eyevoicecoach.data.repository.SettingsRepositoryImpl
import com.eyevoicecoach.data.repository.TipRepositoryImpl
import com.eyevoicecoach.domain.repository.RecordingRepository
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
}

/** Hilt providers for application-scoped local storage. */
@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    /** Creates the app-private Room database. */
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CoachDatabase =
        Room.databaseBuilder(context, CoachDatabase::class.java, "coach.db").build()

    /** Creates the keystore-encrypted preferences store. */
    @Provides @Singleton
    fun provideEncryptedStore(@ApplicationContext context: Context) = EncryptedPreferenceDataStore(context)

    /** Makes application context available to repositories that read assets. */
    @Provides @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context
}
