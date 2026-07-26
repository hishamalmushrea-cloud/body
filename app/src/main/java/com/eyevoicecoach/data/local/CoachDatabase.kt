package com.eyevoicecoach.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** Data access operations for bundled exercises, history and favorites. */
@Dao
interface TipDao {
    /** Inserts a data update without overwriting existing user-safe content. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnoringExisting(tips: List<TipEntity>)

    /** Returns one random unviewed exercise for a context. */
    @Query("SELECT * FROM tips WHERE context = :context AND id NOT IN (SELECT tip_id FROM history) ORDER BY RANDOM() LIMIT 1")
    suspend fun nextForContext(context: String): TipEntity?

    /** Returns one random unviewed Friday challenge for a context. */
    @Query("SELECT * FROM tips WHERE context = :context AND friday_script != '' AND id NOT IN (SELECT tip_id FROM history) ORDER BY RANDOM() LIMIT 1")
    suspend fun nextFridayForContext(context: String): TipEntity?

    /** Returns one random unviewed Friday challenge from all contexts. */
    @Query("SELECT * FROM tips WHERE friday_script != '' AND id NOT IN (SELECT tip_id FROM history) ORDER BY RANDOM() LIMIT 1")
    suspend fun nextFridayAny(): TipEntity?

    /** Returns one random unviewed exercise from all contexts. */
    @Query("SELECT * FROM tips WHERE id NOT IN (SELECT tip_id FROM history) ORDER BY RANDOM() LIMIT 1")
    suspend fun nextAny(): TipEntity?

    /** Adds one viewed event. */
    @Insert
    suspend fun insertHistory(history: HistoryEntity)

    /** Streams all exercises, retaining a stable content order. */
    @Query("SELECT * FROM tips ORDER BY id")
    fun observeAll(): Flow<List<TipEntity>>

    /** Streams exercises in a selected context. */
    @Query("SELECT * FROM tips WHERE context = :context ORDER BY id")
    fun observeByContext(context: String): Flow<List<TipEntity>>

    /** Finds an exercise by primary key. */
    @Query("SELECT * FROM tips WHERE id = :id")
    suspend fun findById(id: Int): TipEntity?

    /** Streams favorite exercises. */
    @Query("SELECT tips.* FROM tips INNER JOIN favorites ON tips.id = favorites.tip_id ORDER BY favorites.added_date DESC")
    fun observeFavorites(): Flow<List<TipEntity>>

    /** Checks one favorite state. */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE tip_id = :tipId)")
    fun observeIsFavorite(tipId: Int): Flow<Boolean>

    /** Inserts a favorite. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    /** Removes a favorite. */
    @Query("DELETE FROM favorites WHERE tip_id = :tipId")
    suspend fun removeFavorite(tipId: Int)

    /** Clears the daily rotation history only. */
    @Query("DELETE FROM history")
    suspend fun clearHistory()

    /** Counts viewed history rows. */
    @Query("SELECT COUNT(*) FROM history")
    fun observeViewedCount(): Flow<Int>

    /** Counts bundled exercise rows. */
    @Query("SELECT COUNT(*) FROM tips")
    fun observeTipCount(): Flow<Int>

    /** Counts favorites. */
    @Query("SELECT COUNT(*) FROM favorites")
    fun observeFavoriteCount(): Flow<Int>

    /** Returns category totals in a lightweight projection. */
    @Query("SELECT category AS category, COUNT(*) AS count FROM history INNER JOIN tips ON tips.id = history.tip_id GROUP BY category")
    fun observeCategoryCounts(): Flow<List<CategoryCount>>
}

/** Count projection used for the statistics dashboard. */
data class CategoryCount(val category: String, val count: Int)

/** Data access operations for private recording metadata. */
@Dao
interface RecordingDao {
    /** Streams recordings from newest to oldest. */
    @Query("SELECT * FROM recordings ORDER BY date_recorded DESC")
    fun observeAll(): Flow<List<RecordingEntity>>

    /** Inserts recording metadata after the private file has been completed. */
    @Insert
    suspend fun insert(recording: RecordingEntity)

    /** Gets recordings older than [cutoffMillis]. */
    @Query("SELECT * FROM recordings WHERE date_recorded < :cutoffMillis")
    suspend fun olderThan(cutoffMillis: Long): List<RecordingEntity>

    /** Removes the metadata row after file deletion. */
    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/** Private Room database; no user data is exported or backed up. */
@Database(entities = [TipEntity::class, HistoryEntity::class, FavoriteEntity::class, RecordingEntity::class], version = 1, exportSchema = true)
abstract class CoachDatabase : RoomDatabase() {
    /** Provides exercise data access. */
    abstract fun tipDao(): TipDao

    /** Provides recording data access. */
    abstract fun recordingDao(): RecordingDao
}
