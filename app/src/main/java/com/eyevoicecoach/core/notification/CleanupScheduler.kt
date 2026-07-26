package com.eyevoicecoach.core.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Registers a unique daily background cleanup work request. */
class CleanupScheduler @Inject constructor(private val context: Context) {
    /** Ensures a single periodic cleanup request exists. */
    fun schedule() {
        val request = PeriodicWorkRequestBuilder<CleanupWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "recording_retention_cleanup",
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
