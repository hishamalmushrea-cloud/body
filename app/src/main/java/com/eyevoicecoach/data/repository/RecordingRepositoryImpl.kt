package com.eyevoicecoach.data.repository

import android.net.Uri
import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.data.local.RecordingEntity
import com.eyevoicecoach.domain.model.Recording
import com.eyevoicecoach.domain.repository.RecordingRepository
import java.io.File
import com.eyevoicecoach.domain.usecase.RecordingRetentionPolicy
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room implementation that keeps all audio files in private application storage. */
class RecordingRepositoryImpl @Inject constructor(
    private val database: CoachDatabase,
) : RecordingRepository {
    private val recordings = database.recordingDao()

    override fun observeRecordings(): Flow<List<Recording>> = recordings.observeAll().map { rows -> rows.map(RecordingEntity::toDomain) }

    override suspend fun addRecording(uri: String, tipId: Int, durationSeconds: Int) {
        recordings.insert(
            RecordingEntity(
                recordingUri = uri,
                dateRecorded = System.currentTimeMillis(),
                tipId = tipId,
                durationSeconds = durationSeconds.coerceAtLeast(0),
            ),
        )
    }

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

    private fun fileForUri(uri: String): File? = runCatching {
        val parsed = Uri.parse(uri)
        if (parsed.scheme == "file") File(requireNotNull(parsed.path)) else null
    }.getOrNull()

    private fun RecordingEntity.toDomain() = Recording(id, recordingUri, dateRecorded, tipId, durationSeconds)
}
