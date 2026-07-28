package com.eyevoicecoach.feature.social

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.model.CommunicationStyle
import com.eyevoicecoach.domain.model.DailyCommunicationBoost
import com.eyevoicecoach.domain.model.SocialProgressStats
import com.eyevoicecoach.domain.model.SocialTrainingContent
import com.eyevoicecoach.domain.model.SocialChoiceExercise
import com.eyevoicecoach.domain.repository.SettingsRepository
import com.eyevoicecoach.domain.repository.SocialTrainingRepository
import com.eyevoicecoach.domain.repository.SocialChoiceRepository
import com.eyevoicecoach.domain.social.GetDailyCommunicationBoostUseCase
import com.eyevoicecoach.domain.social.GetScenarioTrainingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Drives the ethical social scenario chooser and its daily practice cue. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SocialTrainingViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val scenarios: GetScenarioTrainingUseCase,
    private val getBoost: GetDailyCommunicationBoostUseCase,
) : ViewModel() {
    private val scenarioType = MutableStateFlow<String?>(null)
    private val goal = MutableStateFlow<String?>(null)
    private val style = MutableStateFlow<CommunicationStyle?>(null)
    private val track = MutableStateFlow<String?>(null)
    private val _boost = MutableStateFlow<DailyCommunicationBoost?>(null)

    /** Explicitly selected communication style, if any. */
    val selectedStyle: StateFlow<CommunicationStyle?> = style

    /** Cards matching the selected situation, goal and communication style. */
    val content: StateFlow<List<SocialTrainingContent>> = combine(scenarioType, goal, style, track) { scenario, currentGoal, currentStyle, currentTrack ->
        SocialFilter(scenario, currentGoal, currentStyle, currentTrack)
    }.flatMapLatest { filter -> scenarios(filter.scenario, filter.goal, filter.style, filter.track) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Whether the user has acknowledged ethical, self-recording-only use. */
    val ethicsAccepted = settings.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.eyevoicecoach.domain.model.UserSettings())

    /** Today's non-repeating communication cue. */
    val boost: StateFlow<DailyCommunicationBoost?> = _boost

    init { refreshBoost() }

    /** Filters cards by a scenario type, or clears the filter with null. */
    fun selectScenario(value: String?) { scenarioType.value = value }

    /** Filters cards by a communication goal, or clears the filter with null. */
    fun selectGoal(value: String?) { goal.value = value }

    /** Applies an explicitly chosen communication style without diagnosing anyone. */
    fun selectStyle(value: CommunicationStyle?) { style.value = value }

    /** Filters cards by one practical فن التعامل path. */
    fun selectTrack(value: String?) { track.value = value }

    /** Records acknowledgement of the responsible social-training notice. */
    fun acknowledgeEthics() = viewModelScope.launch { settings.setSocialEthicsAcknowledged() }

    /** Retrieves the next daily cue after the user explicitly asks for one. */
    fun refreshBoost() = viewModelScope.launch { _boost.value = getBoost() }
}

private data class SocialFilter(
    val scenario: String?,
    val goal: String?,
    val style: CommunicationStyle?,
    val track: String?,
)

/** Loads one complete social training card. */
@HiltViewModel
class SocialCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    socialTraining: SocialTrainingRepository,
    choiceRepository: SocialChoiceRepository,
) : ViewModel() {
    private val id = checkNotNull(savedStateHandle.get<Int>("socialContentId"))

    /** Selected card, including safe phrases and role-play prompt. */
    val content: StateFlow<SocialTrainingContent?> = kotlinx.coroutines.flow.flow { emit(socialTraining.getContent(id)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Optional educational choice exercise for the selected social situation. */
    val choice: StateFlow<SocialChoiceExercise?> = kotlinx.coroutines.flow.flow { emit(choiceRepository.getChoice(id)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** Exposes strictly local social-practice statistics. */
@HiltViewModel
class SocialStatisticsViewModel @Inject constructor(socialTraining: SocialTrainingRepository) : ViewModel() {
    /** Aggregated progress that never evaluates or stores data about another person. */
    val stats: StateFlow<SocialProgressStats> = socialTraining.observeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SocialProgressStats())
}
