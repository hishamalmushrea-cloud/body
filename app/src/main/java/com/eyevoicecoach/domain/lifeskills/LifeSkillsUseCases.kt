package com.eyevoicecoach.domain.lifeskills

import com.eyevoicecoach.domain.model.LifeSkillsProgress
import com.eyevoicecoach.domain.repository.LifeSkillsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** Saves a completed life-skills lesson and its optional private reflection. */
class CompleteLifeLessonUseCase @Inject constructor(private val repository: LifeSkillsRepository) {
    /** Records a completion after validating that the lesson belongs to the built-in catalogue. */
    suspend operator fun invoke(lessonId: String, trackId: String, reflection: String, chosenOption: Int?) {
        require(LifeSkillsCatalog.track(trackId)?.lessons?.any { it.id == lessonId } == true) { "الدرس غير موجود في المسار المحدد" }
        repository.completeLesson(lessonId, trackId, reflection, chosenOption)
    }
}

/** Streams the user's device-local progress in all life-skills tracks. */
class ObserveLifeSkillsProgressUseCase @Inject constructor(private val repository: LifeSkillsRepository) {
    /** Returns current completion state and saved reflections. */
    operator fun invoke(): Flow<LifeSkillsProgress> = repository.observeProgress()
}
