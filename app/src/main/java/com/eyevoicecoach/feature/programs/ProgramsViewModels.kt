package com.eyevoicecoach.feature.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.catalog.TrainingCatalog
import com.eyevoicecoach.domain.catalog.TrainingProgram
import com.eyevoicecoach.domain.model.Achievement
import com.eyevoicecoach.domain.model.ProgramProgress
import com.eyevoicecoach.domain.repository.AchievementRepository
import com.eyevoicecoach.domain.repository.ProgramProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Streams the six ready-made programs with their local completion state. */
@HiltViewModel
class ProgramsViewModel @Inject constructor(progress: ProgramProgressRepository) : ViewModel() {
    /** Ready-made programs and completion records. */
    val programs: StateFlow<List<ProgramWithProgress>> = progress.observeProgress().combine(kotlinx.coroutines.flow.flowOf(TrainingCatalog.programs)) { records, catalog ->
        catalog.map { program -> ProgramWithProgress(program, records.firstOrNull { it.programId == program.id } ?: ProgramProgress(program.id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrainingCatalog.programs.map { ProgramWithProgress(it, ProgramProgress(it.id)) })
}

/** One catalog program paired with its persisted activity. */
data class ProgramWithProgress(val program: TrainingProgram, val progress: ProgramProgress)

/** Provides one program's stages, completion locks and start action. */
@HiltViewModel
class ProgramDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val progressRepository: ProgramProgressRepository,
) : ViewModel() {
    private val programId = checkNotNull(savedStateHandle.get<String>("programId"))
    private val program = requireNotNull(TrainingCatalog.program(programId))

    /** Current program and its persisted completion state. */
    val state: StateFlow<ProgramWithProgress> = progressRepository.observeProgress().combine(kotlinx.coroutines.flow.flowOf(program)) { progress, current ->
        ProgramWithProgress(current, progress.firstOrNull { it.programId == current.id } ?: ProgramProgress(current.id))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgramWithProgress(program, ProgramProgress(program.id)))

    /** Creates the local program record before its first day is opened. */
    fun start() = viewModelScope.launch { progressRepository.start(programId) }
}

/** Streams elegant, local-only achievement milestones. */
@HiltViewModel
class AchievementsViewModel @Inject constructor(achievements: AchievementRepository) : ViewModel() {
    /** Earned and in-progress achievements. */
    val achievements: StateFlow<List<Achievement>> = achievements.observeAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
