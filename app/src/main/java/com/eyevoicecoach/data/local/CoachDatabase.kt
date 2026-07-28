package com.eyevoicecoach.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    /** Deletes all favorites as part of an explicit privacy reset. */
    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()

    /** Clears the daily rotation history only. */
    @Query("DELETE FROM history")
    suspend fun clearHistory()

    /** Streams shown timestamps for local streak calculations. */
    @Query("SELECT date_shown FROM history ORDER BY date_shown DESC")
    fun observeShownDates(): Flow<List<Long>>

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
    suspend fun insert(recording: RecordingEntity): Long

    /** Gets recordings older than [cutoffMillis]. */
    @Query("SELECT * FROM recordings WHERE date_recorded < :cutoffMillis")
    suspend fun olderThan(cutoffMillis: Long): List<RecordingEntity>

    /** Removes the metadata row after file deletion. */
    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Deletes all private recording metadata rows. */
    @Query("DELETE FROM recordings")
    suspend fun deleteAll()
}

/** Data access operations for self-assessments. */
@Dao
interface AssessmentDao {
    /** Saves one recording review, replacing an earlier review of the same recording. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(assessment: SelfAssessmentEntity)

    /** Reads a private review by its recording identifier. */
    @Query("SELECT * FROM self_assessments WHERE recording_id = :recordingId")
    suspend fun find(recordingId: Long): SelfAssessmentEntity?

    /** Deletes every review during a privacy reset. */
    @Query("DELETE FROM self_assessments")
    suspend fun deleteAll()
}

/** Data access operations for ready-made program completion. */
@Dao
interface ProgramProgressDao {
    /** Streams program progress joined with all program-day completions. */
    @Query("SELECT * FROM program_progress")
    fun observeProgress(): Flow<List<ProgramProgressEntity>>

    /** Streams all completed program days. */
    @Query("SELECT * FROM program_day_completions")
    fun observeCompletions(): Flow<List<ProgramDayCompletionEntity>>

    /** Creates a program progress record when it is started. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun start(progress: ProgramProgressEntity)

    /** Saves a completed day once, retaining the first reviewed recording. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun completeDay(completion: ProgramDayCompletionEntity)

    /** Counts program day completions. */
    @Query("SELECT COUNT(*) FROM program_day_completions WHERE program_id = :programId")
    suspend fun completedDayCount(programId: String): Int

    /** Marks one program as completely finished. */
    @Query("UPDATE program_progress SET completed_at = :completedAt WHERE program_id = :programId")
    suspend fun markCompleted(programId: String, completedAt: Long)

    /** Clears all program history during a privacy reset. */
    @Query("DELETE FROM program_day_completions")
    suspend fun clearCompletions()

    /** Clears all program progress during a privacy reset. */
    @Query("DELETE FROM program_progress")
    suspend fun clearProgress()
}

/** Private Room database; no user data is exported or backed up. */
@Database(entities = [TipEntity::class, HistoryEntity::class, FavoriteEntity::class, RecordingEntity::class, ProgramProgressEntity::class, ProgramDayCompletionEntity::class, SelfAssessmentEntity::class, SocialTrainingContentEntity::class, SocialTrainingPhraseEntity::class, SocialSessionEntity::class, SocialSelfAssessmentEntity::class, LifeLessonCompletionEntity::class], version = 5, exportSchema = true)
abstract class CoachDatabase : RoomDatabase() {
    /** Provides exercise data access. */
    abstract fun tipDao(): TipDao

    /** Provides recording data access. */
    abstract fun recordingDao(): RecordingDao

    /** Provides private assessment data access. */
    abstract fun assessmentDao(): AssessmentDao

    /** Provides ready-made program progress data access. */
    abstract fun programProgressDao(): ProgramProgressDao

    /** Provides responsible social-training data access. */
    abstract fun socialTrainingDao(): SocialTrainingDao

    /** Provides life-skills lesson progress data access. */
    abstract fun lifeSkillsDao(): LifeSkillsDao
}

/** Explicit non-destructive database migrations for locally retained user data. */
object CoachDatabaseMigrations {
    /** Adds programs, day completions and encrypted-device-only self-reviews. */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS program_progress (program_id TEXT NOT NULL, started_at INTEGER NOT NULL, completed_at INTEGER, PRIMARY KEY(program_id))")
            database.execSQL("CREATE TABLE IF NOT EXISTS program_day_completions (program_id TEXT NOT NULL, day_number INTEGER NOT NULL, completed_at INTEGER NOT NULL, recording_id INTEGER NOT NULL, PRIMARY KEY(program_id, day_number))")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_program_day_completions_recording_id ON program_day_completions(recording_id)")
            database.execSQL("CREATE TABLE IF NOT EXISTS self_assessments (recording_id INTEGER NOT NULL, clarity INTEGER NOT NULL, pace INTEGER NOT NULL, confidence INTEGER NOT NULL, pauses INTEGER NOT NULL, tension TEXT NOT NULL, note TEXT NOT NULL, created_at INTEGER NOT NULL, PRIMARY KEY(recording_id), FOREIGN KEY(recording_id) REFERENCES recordings(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_self_assessments_recording_id ON self_assessments(recording_id)")
        }
    }

    /** Adds responsible-social-training content, session history and private reviews. */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS social_training_content (id INTEGER NOT NULL, module_category TEXT NOT NULL, context TEXT NOT NULL, scenario_type TEXT NOT NULL, personality_style TEXT, difficulty TEXT NOT NULL, duration_minutes INTEGER NOT NULL, communication_goal TEXT NOT NULL, title TEXT NOT NULL, tip_text TEXT NOT NULL, practical_example TEXT NOT NULL, boundary_rule TEXT NOT NULL, roleplay_prompt TEXT NOT NULL, eye_contact_tip TEXT NOT NULL, body_language_tip TEXT NOT NULL, voice_tip TEXT NOT NULL, content_version INTEGER NOT NULL, content_safety_level TEXT NOT NULL, PRIMARY KEY(id))")
            database.execSQL("CREATE TABLE IF NOT EXISTS social_training_phrases (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, content_id INTEGER NOT NULL, phrase_type TEXT NOT NULL, text TEXT NOT NULL, FOREIGN KEY(content_id) REFERENCES social_training_content(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_social_training_phrases_content_id ON social_training_phrases(content_id)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_social_training_phrases_content_id_phrase_type_text ON social_training_phrases(content_id, phrase_type, text)")
            database.execSQL("CREATE TABLE IF NOT EXISTS social_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, content_id INTEGER NOT NULL, session_mode TEXT NOT NULL, scenario_type TEXT NOT NULL, personality_style TEXT NOT NULL, communication_goal TEXT NOT NULL, date_completed INTEGER NOT NULL, recording_id INTEGER, FOREIGN KEY(content_id) REFERENCES social_training_content(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_social_sessions_content_id ON social_sessions(content_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_social_sessions_date_completed ON social_sessions(date_completed)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_social_sessions_scenario_type ON social_sessions(scenario_type)")
            database.execSQL("CREATE TABLE IF NOT EXISTS social_self_assessments (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, session_id INTEGER NOT NULL, clarity_score INTEGER NOT NULL, confidence_score INTEGER NOT NULL, respect_score INTEGER NOT NULL, listening_score INTEGER NOT NULL, calmness_score INTEGER NOT NULL, notes TEXT NOT NULL, created_at INTEGER NOT NULL, FOREIGN KEY(session_id) REFERENCES social_sessions(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_social_self_assessments_session_id ON social_self_assessments(session_id)")
        }
    }

    /** Adds explicit فن التعامل tracks without touching retained social sessions. */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE social_training_content ADD COLUMN social_track TEXT NOT NULL DEFAULT 'social_basics'")
        }
    }

    /** Adds private completion and reflection data for the educational life-skills tracks. */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS life_lesson_completions (lesson_id TEXT NOT NULL, track_id TEXT NOT NULL, completed_at INTEGER NOT NULL, reflection TEXT NOT NULL, chosen_option INTEGER, PRIMARY KEY(lesson_id))")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_life_lesson_completions_track_id ON life_lesson_completions(track_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_life_lesson_completions_completed_at ON life_lesson_completions(completed_at)")
        }
    }
}
