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
