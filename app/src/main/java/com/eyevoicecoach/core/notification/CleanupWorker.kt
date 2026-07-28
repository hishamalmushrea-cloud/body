package com.eyevoicecoach.core.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eyevoicecoach.domain.repository.RecordingRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Performs the mandatory seven-day audio cleanup in a battery-friendly background job. */
@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val recordings: RecordingRepository,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result = runCatching {
        recordings.deleteExpiredRecordings(System.currentTimeMillis())
        Result.success()
    }.getOrElse { Result.retry() }
}
