package com.eyevoicecoach.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** Content row and its separate phrase rows, materialized by Room. */
data class SocialContentWithPhrases(
    @Embedded val content: SocialTrainingContentEntity,
    @Relation(parentColumn = "id", entityColumn = "content_id") val phrases: List<SocialTrainingPhraseEntity>,
)

/** Projection used to calculate local-only social training statistics. */
data class SocialSessionAnalytics(
    val id: Long,
    val moduleCategory: String,
    val scenarioType: String,
    val personalityStyle: String,
    val dateCompleted: Long,
    val confidenceScore: Int?,
    val respectScore: Int?,
    val calmnessScore: Int?,
)

/** Data access operations for vetted social training cards and phrases. */
@Dao
interface SocialTrainingDao {
    /** Inserts new bundled social content without replacing user-safe existing rows. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContentIgnoringExisting(content: List<SocialTrainingContentEntity>)

    /** Updates track labels without replacing content or deleting session history. */
    @Query("UPDATE social_training_content SET social_track = :track WHERE id = :id")
    suspend fun updateTrack(id: Int, track: String)

    /** Inserts phrases for newly bundled cards. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPhrasesIgnoringExisting(phrases: List<SocialTrainingPhraseEntity>)

    /** Streams every content card together with its phrases. */
    @Transaction
    @Query("SELECT * FROM social_training_content ORDER BY id")
    fun observeAllWithPhrases(): Flow<List<SocialContentWithPhrases>>

    /** Reads one full social card. */
    @Transaction
    @Query("SELECT * FROM social_training_content WHERE id = :id")
    suspend fun findWithPhrases(id: Int): SocialContentWithPhrases?

    /** Inserts a completed private practice session. */
    @Insert
    suspend fun insertSession(session: SocialSessionEntity): Long

    /** Inserts the subjective social review after its session exists. */
    @Insert
    suspend fun insertAssessment(assessment: SocialSelfAssessmentEntity)

    /** Streams the compact joined data used for local statistics. */
    @Query("SELECT social_sessions.id AS id, social_training_content.module_category AS moduleCategory, social_sessions.scenario_type AS scenarioType, social_sessions.personality_style AS personalityStyle, social_sessions.date_completed AS dateCompleted, social_self_assessments.confidence_score AS confidenceScore, social_self_assessments.respect_score AS respectScore, social_self_assessments.calmness_score AS calmnessScore FROM social_sessions INNER JOIN social_training_content ON social_sessions.content_id = social_training_content.id LEFT JOIN social_self_assessments ON social_sessions.id = social_self_assessments.session_id ORDER BY social_sessions.date_completed DESC")
    fun observeAnalytics(): Flow<List<SocialSessionAnalytics>>

    /** Deletes social-session reviews explicitly requested by the user. */
    @Query("DELETE FROM social_self_assessments")
    suspend fun clearAssessments()

    /** Deletes social sessions and cascades their reviews. */
    @Query("DELETE FROM social_sessions")
    suspend fun clearSessions()
}
