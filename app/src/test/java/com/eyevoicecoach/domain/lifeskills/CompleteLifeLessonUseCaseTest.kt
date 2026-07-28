package com.eyevoicecoach.domain.lifeskills

import com.eyevoicecoach.domain.model.LifeSkillsProgress
import com.eyevoicecoach.domain.repository.LifeSkillsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CompleteLifeLessonUseCaseTest {
    @Test
    fun `completion use case stores reflection against a valid lesson`() = runBlocking {
        val repository = FakeLifeSkillsRepository()
        val track = LifeSkillsCatalog.tracks.first()
        val lesson = track.lessons.first()

        CompleteLifeLessonUseCase(repository)(lesson.id, track.id, "خطوتي هذا الأسبوع", 1)

        assertEquals(lesson.id, repository.lessonId)
        assertEquals("خطوتي هذا الأسبوع", repository.reflection)
    }
}

private class FakeLifeSkillsRepository : LifeSkillsRepository {
    var lessonId: String? = null
    var reflection: String? = null
    override fun observeProgress(): Flow<LifeSkillsProgress> = MutableStateFlow(LifeSkillsProgress())
    override suspend fun completeLesson(lessonId: String, trackId: String, reflection: String, chosenOption: Int?) {
        this.lessonId = lessonId
        this.reflection = reflection
    }
    override suspend fun clearAll() = Unit
}
