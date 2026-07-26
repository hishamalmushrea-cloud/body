package com.eyevoicecoach.domain.social

import com.eyevoicecoach.domain.model.CommunicationStyle
import com.eyevoicecoach.domain.model.DailyCommunicationBoost
import com.eyevoicecoach.domain.model.SocialProgressStats
import com.eyevoicecoach.domain.model.SocialSelfAssessment
import com.eyevoicecoach.domain.model.SocialTrainingContent
import com.eyevoicecoach.domain.repository.DailyCommunicationBoostRepository
import com.eyevoicecoach.domain.repository.SocialTrainingRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/** Retrieves ethical scenario training cards for the chosen situation, goal and optional style. */
class GetScenarioTrainingUseCase @Inject constructor(private val socialTraining: SocialTrainingRepository) {
    /** Streams appropriate training cards without inferring or diagnosing the other person. */
    operator fun invoke(scenarioType: String?, goal: String?, style: CommunicationStyle?, track: String? = null): Flow<List<SocialTrainingContent>> =
        socialTraining.observeContent(scenarioType, goal, style, track)
}

/** Retrieves one non-repeating daily communication cue. */
class GetDailyCommunicationBoostUseCase @Inject constructor(private val boosts: DailyCommunicationBoostRepository) {
    /** Gets and records one unseen daily cue. */
    suspend operator fun invoke(): DailyCommunicationBoost? = boosts.getAndMarkNextBoost()
}

/** Filters adaptation guidance for the style selected explicitly by the user. */
class GetPersonalityAdaptationTipUseCase @Inject constructor(private val socialTraining: SocialTrainingRepository) {
    /** Streams adaptation cards matching [style]. */
    operator fun invoke(style: CommunicationStyle): Flow<List<SocialTrainingContent>> =
        socialTraining.observeContent(style = style)
}

/** Saves one ethically framed social role-play and its private evaluation. */
class SaveScenarioSessionUseCase @Inject constructor(private val socialTraining: SocialTrainingRepository) {
    /** Saves a completed role-play linked to an optional private recording. */
    suspend operator fun invoke(contentId: Int, recordingId: Long?, assessment: SocialSelfAssessment, selectedStyle: CommunicationStyle = CommunicationStyle.UNKNOWN): Long =
        socialTraining.saveSession(contentId, recordingId, assessment, selectedStyle)
}

/** Persists a private social self-assessment with its completed role-play session. */
class SaveSelfAssessmentUseCase @Inject constructor(private val socialTraining: SocialTrainingRepository) {
    /** Saves the assessment after a self-recorded scenario attempt. */
    suspend operator fun invoke(contentId: Int, recordingId: Long?, assessment: SocialSelfAssessment, selectedStyle: CommunicationStyle = CommunicationStyle.UNKNOWN): Long =
        socialTraining.saveSession(contentId, recordingId, assessment, selectedStyle)
}

/** Suggests the next practice card based on the least practised ethical communication module. */
class SuggestNextSocialPracticeUseCase @Inject constructor(private val socialTraining: SocialTrainingRepository) {
    /** Returns a card from the least represented module, or the first available card. */
    suspend operator fun invoke(): SocialTrainingContent? {
        val all = socialTraining.observeContent().first()
        if (all.isEmpty()) return null
        val stats = socialTraining.observeStats().first()
        return all.firstOrNull { it.moduleCategory != stats.strongestModule } ?: all.first()
    }
}

/** Rejects wording that teaches pressure, deception or disregard for another person's boundaries. */
class ValidateContentEthicsUseCase @Inject constructor() {
    /** Returns true only when every supplied text avoids restricted coercive wording. */
    operator fun invoke(texts: Iterable<String>): Boolean = texts.all(::isSafe)

    /** Checks one piece of text against restricted wording. */
    fun isSafe(text: String): Boolean = prohibited.none { term -> text.contains(term, ignoreCase = true) }

    private companion object {
        val prohibited = setOf("أجبره", "اضغط عليه", "لا تقبل الرفض", "لاحقه", "اخدعه", "استغل خوفه")
    }
}
