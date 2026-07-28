package com.eyevoicecoach.feature.lifeskills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.lifeskills.CompleteLifeLessonUseCase
import com.eyevoicecoach.domain.lifeskills.LifeSkillsCatalog
import com.eyevoicecoach.domain.lifeskills.LifeSkillsLesson
import com.eyevoicecoach.domain.lifeskills.LifeSkillsTrack
import com.eyevoicecoach.domain.lifeskills.ObserveLifeSkillsProgressUseCase
import com.eyevoicecoach.domain.model.LifeSkillsProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Exposes all available life-skills tracks with private completion state. */
@HiltViewModel
class LifeSkillsViewModel @Inject constructor(observeProgress: ObserveLifeSkillsProgressUseCase) : ViewModel() {
    /** Local lesson-completion state for the entire catalogue. */
    val progress: StateFlow<LifeSkillsProgress> = observeProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LifeSkillsProgress())

    /** Immutable educational tracks provided entirely offline. */
    val tracks: List<LifeSkillsTrack> = LifeSkillsCatalog.tracks
}

/** Loads one life-skills track and its completion state. */
@HiltViewModel
class LifeSkillsTrackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeProgress: ObserveLifeSkillsProgressUseCase,
) : ViewModel() {
    private val trackId = checkNotNull(savedStateHandle.get<String>("lifeTrackId"))

    /** Selected offline learning track. */
    val track: LifeSkillsTrack = requireNotNull(LifeSkillsCatalog.track(trackId))

    /** Progress relevant to this and other tracks, used to render completed lessons. */
    val progress: StateFlow<LifeSkillsProgress> = observeProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LifeSkillsProgress())
}

/** Completes a lesson after the user has reviewed its decision exercise and reflection prompt. */
@HiltViewModel
class LifeLessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeProgress: ObserveLifeSkillsProgressUseCase,
    private val completeLesson: CompleteLifeLessonUseCase,
) : ViewModel() {
    private val lessonId = checkNotNull(savedStateHandle.get<String>("lifeLessonId"))
    private val pair = requireNotNull(LifeSkillsCatalog.lesson(lessonId))

    /** Track to which the selected lesson belongs. */
    val track: LifeSkillsTrack = pair.first

    /** Detailed offline lesson. */
    val lesson: LifeSkillsLesson = pair.second

    /** Existing reflection data, if the user has completed this lesson previously. */
    val progress: StateFlow<LifeSkillsProgress> = observeProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LifeSkillsProgress())

    /** Saves completion and a private reflection; an answer is optional. */
    fun complete(reflection: String, selectedOption: Int?) = viewModelScope.launch {
        completeLesson(lesson.id, track.id, reflection, selectedOption)
    }
}
