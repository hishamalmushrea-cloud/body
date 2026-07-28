package com.eyevoicecoach.data.repository

import android.net.Uri
import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.data.local.RecordingEntity
import com.eyevoicecoach.domain.model.Recording
import com.eyevoicecoach.domain.repository.RecordingRepository
import com.eyevoicecoach.domain.usecase.RecordingRetentionPolicy
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Room implementation that keeps all audio files in private application storage. */
class RecordingRepositoryImpl @Inject constructor(
    private val database: CoachDatabase,
) : RecordingRepository {
    private val recordings = database.recordingDao()

    override fun observeRecordings(): Flow<List<Recording>> = recordings.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun addRecording(uri: String, tipId: Int, durationSeconds: Int): Long = recordings.insert(
            RecordingEntity(
                recordingUri = uri,
                dateRecorded = System.currentTimeMillis(),
                tipId = tipId,
                durationSeconds = durationSeconds.coerceAtLeast(0),
            ),
        )

    override suspend fun deleteRecording(recording: Recording) {
        fileForUri(recording.uri)?.delete()
        recordings.deleteById(recording.id)
    }

    override suspend fun deleteExpiredRecordings(nowMillis: Long) {
        val cutoff = RecordingRetentionPolicy.cutoffAt(nowMillis)
        recordings.olderThan(cutoff).forEach { row ->
            fileForUri(row.recordingUri)?.delete()
            recordings.deleteById(row.id)
        }
    }

    override suspend fun deleteAllRecordings() {
        recordings.observeAll().first().forEach { row -> fileForUri(row.recordingUri)?.delete() }
        recordings.deleteAll()
    }

    private fun fileForUri(uri: String): File? = runCatching {
        val parsed = Uri.parse(uri)
        if (parsed.scheme == "file") File(requireNotNull(parsed.path)) else null
    }.getOrNull()

    private fun RecordingEntity.toDomain() = Recording(id, recordingUri, dateRecorded, tipId, durationSeconds)
}
