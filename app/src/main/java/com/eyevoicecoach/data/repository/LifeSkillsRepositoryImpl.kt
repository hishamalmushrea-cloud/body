package com.eyevoicecoach.data.repository

import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.data.local.LifeLessonCompletionEntity
import com.eyevoicecoach.domain.model.LifeSkillsProgress
import com.eyevoicecoach.domain.repository.LifeSkillsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed repository for private life-skills lesson reflections and completion. */
class LifeSkillsRepositoryImpl @Inject constructor(database: CoachDatabase) : LifeSkillsRepository {
    private val lessons = database.lifeSkillsDao()

    override fun observeProgress(): Flow<LifeSkillsProgress> = lessons.observeCompletions().map { rows ->
        LifeSkillsProgress(
            completedLessonIds = rows.map(LifeLessonCompletionEntity::lessonId).toSet(),
            reflections = rows.associate { row -> row.lessonId to row.reflection },
        )
    }

    override suspend fun completeLesson(lessonId: String, trackId: String, reflection: String, chosenOption: Int?) {
        lessons.upsert(
            LifeLessonCompletionEntity(
                lessonId = lessonId,
                trackId = trackId,
                completedAt = System.currentTimeMillis(),
                reflection = reflection.trim(),
                chosenOption = chosenOption,
            ),
        )
    }

    override suspend fun clearAll() = lessons.clearAll()
}
