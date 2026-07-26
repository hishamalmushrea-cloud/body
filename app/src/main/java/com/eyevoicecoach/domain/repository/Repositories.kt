package com.eyevoicecoach.domain.repository

import com.eyevoicecoach.domain.model.ProgressStats
import com.eyevoicecoach.domain.model.Recording
import com.eyevoicecoach.domain.model.SelfAssessment
import com.eyevoicecoach.domain.model.ProgramProgress
import com.eyevoicecoach.domain.model.Tip
import com.eyevoicecoach.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/** Contract for exercise content, history and favorites stored locally. */
interface TipRepository {
    /** Imports bundled data only when its version is newer than the stored version. */
    suspend fun seedBundledTipsIfNeeded()

    /** Chooses and records a fresh daily exercise for [context], or returns null when exhausted. */
    suspend fun getAndMarkNextTip(context: String, isFriday: Boolean): Tip?

    /** Returns all exercises, optionally limited to a context. */
    fun observeTips(context: String? = null): Flow<List<Tip>>

    /** Observes exercises marked as favorites. */
    fun observeFavorites(): Flow<List<Tip>>

    /** Returns an exercise by identifier. */
    suspend fun getTip(id: Int): Tip?

    /** Toggles the favorite state for [tipId]. */
    suspend fun toggleFavorite(tipId: Int)

    /** Observes whether one exercise is a favorite. */
    fun observeFavorite(tipId: Int): Flow<Boolean>

    /** Clears viewed history so daily exercise rotation can begin again. */
    suspend fun resetHistory()

    /** Deletes history and favorites without deleting bundled exercise content. */
    suspend fun clearUserActivity()

    /** Observes calculated usage statistics. */
    fun observeStats(): Flow<ProgressStats>
}

/** Contract for private, device-local audio recordings. */
interface RecordingRepository {
    /** Observes recordings newest first. */
    fun observeRecordings(): Flow<List<Recording>>

    /** Persists metadata after a recording file is successfully finalized. */
    suspend fun addRecording(uri: String, tipId: Int, durationSeconds: Int): Long

    /** Deletes both the file and metadata for [recording]. */
    suspend fun deleteRecording(recording: Recording)

    /** Deletes recording files and rows older than seven days. */
    suspend fun deleteExpiredRecordings(nowMillis: Long)

    /** Deletes every private recording file and metadata row. */
    suspend fun deleteAllRecordings()
}

/** Contract for self-assessments attached to private recordings. */
interface AssessmentRepository {
    /** Saves or replaces a private review for its recording. */
    suspend fun save(assessment: SelfAssessment)

    /** Returns the review for a recording when one exists. */
    suspend fun get(recordingId: Long): SelfAssessment?

    /** Deletes all locally saved self-assessments. */
    suspend fun deleteAll()
}

/** Contract for local program progress and program-day completion. */
interface ProgramProgressRepository {
    /** Streams all program progress records. */
    fun observeProgress(): Flow<List<ProgramProgress>>

    /** Marks a program as started without changing any completed days. */
    suspend fun start(programId: String)

    /** Completes one day and links it to the recording used in the review. */
    suspend fun completeDay(programId: String, dayNumber: Int, recordingId: Long)

    /** Deletes all stored program progress. */
    suspend fun clearAll()
}

/** Contract for private achievements calculated from device-local activity. */
interface AchievementRepository {
    /** Streams earned and in-progress achievements. */
    fun observeAchievements(): Flow<List<com.eyevoicecoach.domain.model.Achievement>>
}

/** Contract for encrypted application preferences. */
interface SettingsRepository {
    /** Streams all current settings. */
    val settings: Flow<UserSettings>

    /** Marks the introduction as completed. */
    suspend fun completeOnboarding()

    /** Saves the preferred exercise context. */
    suspend fun setContext(context: String)

    /** Saves a visual theme identifier. */
    suspend fun setTheme(themeKey: String)

    /** Saves reminder enablement and its local time. */
    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int)

    /** Clears encrypted user settings to restore first-run defaults. */
    suspend fun clearAll()
}
