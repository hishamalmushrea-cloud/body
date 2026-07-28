package com.eyevoicecoach.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data access operations for private life-skills lesson progress and reflections. */
@Dao
interface LifeSkillsDao {
    /** Streams all user-completed lesson records. */
    @Query("SELECT * FROM life_lesson_completions ORDER BY completed_at DESC")
    fun observeCompletions(): Flow<List<LifeLessonCompletionEntity>>

    /** Saves one lesson reflection, replacing a previous completion for the same lesson. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(completion: LifeLessonCompletionEntity)

    /** Deletes all life-skills progress during an explicit privacy reset. */
    @Query("DELETE FROM life_lesson_completions")
    suspend fun clearAll()
}
