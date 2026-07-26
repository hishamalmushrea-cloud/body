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
