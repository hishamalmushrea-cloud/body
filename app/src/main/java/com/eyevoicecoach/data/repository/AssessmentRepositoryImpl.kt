package com.eyevoicecoach.data.repository

import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.data.local.SelfAssessmentEntity
import com.eyevoicecoach.domain.model.SelfAssessment
import com.eyevoicecoach.domain.model.TensionLevel
import com.eyevoicecoach.domain.repository.AssessmentRepository
import javax.inject.Inject

/** Room repository for private post-recording self-assessments. */
class AssessmentRepositoryImpl @Inject constructor(database: CoachDatabase) : AssessmentRepository {
    private val assessments = database.assessmentDao()

    override suspend fun save(assessment: SelfAssessment) {
        assessments.upsert(
            SelfAssessmentEntity(
                recordingId = assessment.recordingId,
                clarity = assessment.clarity.coerceIn(1, 5),
                pace = assessment.pace.coerceIn(1, 5),
                confidence = assessment.confidence.coerceIn(1, 5),
                pauses = assessment.pauses.coerceIn(1, 5),
                tension = assessment.tension.name,
                note = assessment.note.trim(),
                createdAt = assessment.createdAt,
            ),
        )
    }

    override suspend fun get(recordingId: Long): SelfAssessment? = assessments.find(recordingId)?.toDomain()

    override suspend fun deleteAll() = assessments.deleteAll()

    private fun SelfAssessmentEntity.toDomain() = SelfAssessment(
        recordingId = recordingId,
        clarity = clarity,
        pace = pace,
        confidence = confidence,
        pauses = pauses,
        tension = TensionLevel.fromName(tension),
        note = note,
        createdAt = createdAt,
    )
}
