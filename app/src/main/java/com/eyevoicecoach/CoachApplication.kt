package com.eyevoicecoach

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.eyevoicecoach.core.notification.CleanupScheduler
import com.eyevoicecoach.core.notification.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Application entry point that configures Hilt-managed background cleanup and notifications. */
@HiltAndroidApp
class CoachApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var cleanupScheduler: CleanupScheduler
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()
        reminderScheduler.createChannel()
        cleanupScheduler.schedule()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
