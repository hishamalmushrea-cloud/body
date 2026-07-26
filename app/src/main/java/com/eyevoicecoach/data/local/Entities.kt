package com.eyevoicecoach.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Room row containing immutable bundled coaching content. */
@Entity(tableName = "tips")
data class TipEntity(
    @PrimaryKey val id: Int,
    val category: String,
    val context: String,
    @ColumnInfo(name = "tip_text") val text: String,
    @ColumnInfo(name = "practical_example") val practicalExample: String,
    @ColumnInfo(name = "daily_challenge") val dailyChallenge: String,
    @ColumnInfo(name = "friday_script") val fridayScript: String,
)

/** Room row recording that an exercise has appeared in the daily rotation. */
@Entity(
    tableName = "history",
    foreignKeys = [ForeignKey(entity = TipEntity::class, parentColumns = ["id"], childColumns = ["tip_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("tip_id")],
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "tip_id") val tipId: Int,
    @ColumnInfo(name = "date_shown") val dateShown: Long,
)

/** Room row identifying a favorite exercise. */
@Entity(
    tableName = "favorites",
    foreignKeys = [ForeignKey(entity = TipEntity::class, parentColumns = ["id"], childColumns = ["tip_id"], onDelete = ForeignKey.CASCADE)],
)
data class FavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "tip_id") val tipId: Int,
    @ColumnInfo(name = "added_date") val addedDate: Long,
)

/** Room row describing a private audio file saved within app storage. */
@Entity(
    tableName = "recordings",
    foreignKeys = [ForeignKey(entity = TipEntity::class, parentColumns = ["id"], childColumns = ["tip_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("tip_id"), Index("date_recorded")],
)
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recording_uri") val recordingUri: String,
    @ColumnInfo(name = "date_recorded") val dateRecorded: Long,
    @ColumnInfo(name = "tip_id") val tipId: Int,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Int,
)

/** Room row recording a user's progress through a built-in program. */
@Entity(tableName = "program_progress")
data class ProgramProgressEntity(
    @PrimaryKey @ColumnInfo(name = "program_id") val programId: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
)

/** Room row linking one program day to the reviewed private recording that completed it. */
@Entity(
    tableName = "program_day_completions",
    primaryKeys = ["program_id", "day_number"],
    indices = [Index("recording_id")],
)
data class ProgramDayCompletionEntity(
    @ColumnInfo(name = "program_id") val programId: String,
    @ColumnInfo(name = "day_number") val dayNumber: Int,
    @ColumnInfo(name = "completed_at") val completedAt: Long,
    @ColumnInfo(name = "recording_id") val recordingId: Long,
)

/** Room row containing the private subjective review of one voice recording. */
@Entity(
    tableName = "self_assessments",
    foreignKeys = [ForeignKey(entity = RecordingEntity::class, parentColumns = ["id"], childColumns = ["recording_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("recording_id")],
)
data class SelfAssessmentEntity(
    @PrimaryKey @ColumnInfo(name = "recording_id") val recordingId: Long,
    val clarity: Int,
    val pace: Int,
    val confidence: Int,
    val pauses: Int,
    val tension: String,
    val note: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

/** Room row containing vetted responsible-social-communication training content. */
@Entity(tableName = "social_training_content")
data class SocialTrainingContentEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "module_category") val moduleCategory: String,
    val context: String,
    @ColumnInfo(name = "scenario_type") val scenarioType: String,
    @ColumnInfo(name = "personality_style") val personalityStyle: String?,
    val difficulty: String,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    @ColumnInfo(name = "communication_goal") val communicationGoal: String,
    val title: String,
    @ColumnInfo(name = "tip_text") val tipText: String,
    @ColumnInfo(name = "practical_example") val practicalExample: String,
    @ColumnInfo(name = "boundary_rule") val boundaryRule: String,
    @ColumnInfo(name = "roleplay_prompt") val roleplayPrompt: String,
    @ColumnInfo(name = "eye_contact_tip") val eyeContactTip: String,
    @ColumnInfo(name = "body_language_tip") val bodyLanguageTip: String,
    @ColumnInfo(name = "voice_tip") val voiceTip: String,
    @ColumnInfo(name = "content_version") val contentVersion: Int,
    @ColumnInfo(name = "content_safety_level") val contentSafetyLevel: String,
)

/** Room phrase row kept separately to avoid inflating social content records. */
@Entity(
    tableName = "social_training_phrases",
    foreignKeys = [ForeignKey(entity = SocialTrainingContentEntity::class, parentColumns = ["id"], childColumns = ["content_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("content_id"), Index(value = ["content_id", "phrase_type", "text"], unique = true)],
)
data class SocialTrainingPhraseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "content_id") val contentId: Int,
    @ColumnInfo(name = "phrase_type") val phraseType: String,
    val text: String,
)

/** Room row marking one privately completed social role-play session. */
@Entity(
    tableName = "social_sessions",
    foreignKeys = [ForeignKey(entity = SocialTrainingContentEntity::class, parentColumns = ["id"], childColumns = ["content_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("content_id"), Index("date_completed"), Index("scenario_type")],
)
data class SocialSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "content_id") val contentId: Int,
    @ColumnInfo(name = "session_mode") val sessionMode: String,
    @ColumnInfo(name = "scenario_type") val scenarioType: String,
    @ColumnInfo(name = "personality_style") val personalityStyle: String,
    @ColumnInfo(name = "communication_goal") val communicationGoal: String,
    @ColumnInfo(name = "date_completed") val dateCompleted: Long,
    @ColumnInfo(name = "recording_id") val recordingId: Long?,
)

/** Room row containing a private social-session self-review. */
@Entity(
    tableName = "social_self_assessments",
    foreignKeys = [ForeignKey(entity = SocialSessionEntity::class, parentColumns = ["id"], childColumns = ["session_id"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("session_id")],
)
data class SocialSelfAssessmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "clarity_score") val clarityScore: Int,
    @ColumnInfo(name = "confidence_score") val confidenceScore: Int,
    @ColumnInfo(name = "respect_score") val respectScore: Int,
    @ColumnInfo(name = "listening_score") val listeningScore: Int,
    @ColumnInfo(name = "calmness_score") val calmnessScore: Int,
    val notes: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
