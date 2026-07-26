package com.eyevoicecoach.domain.repository

import com.eyevoicecoach.domain.model.ProgressStats
import com.eyevoicecoach.domain.model.Recording
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

    /** Observes calculated usage statistics. */
    fun observeStats(): Flow<ProgressStats>
}

/** Contract for private, device-local audio recordings. */
interface RecordingRepository {
    /** Observes recordings newest first. */
    fun observeRecordings(): Flow<List<Recording>>

    /** Persists metadata after a recording file is successfully finalized. */
    suspend fun addRecording(uri: String, tipId: Int, durationSeconds: Int)

    /** Deletes both the file and metadata for [recording]. */
    suspend fun deleteRecording(recording: Recording)

    /** Deletes recording files and rows older than seven days. */
    suspend fun deleteExpiredRecordings(nowMillis: Long)
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
}
