package com.eyevoicecoach.domain.social

import com.eyevoicecoach.domain.model.CommunicationStyle
import com.eyevoicecoach.domain.model.DailyCommunicationBoost
import com.eyevoicecoach.domain.model.PhraseType
import com.eyevoicecoach.domain.model.SocialProgressStats
import com.eyevoicecoach.domain.model.SocialSelfAssessment
import com.eyevoicecoach.domain.model.SocialTrainingContent
import com.eyevoicecoach.domain.model.TrainingPhrase
import com.eyevoicecoach.domain.repository.DailyCommunicationBoostRepository
import com.eyevoicecoach.domain.repository.SocialTrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SocialUseCasesTest {
    @Test
    fun `scenario use case forwards scenario goal and selected style`() = runBlocking {
        val repository = FakeSocialRepository()
        GetScenarioTrainingUseCase(repository)("client_meeting", "understand_need_before_offer", CommunicationStyle.ANALYTICAL).first()

        assertEquals("client_meeting", repository.scenario)
        assertEquals("understand_need_before_offer", repository.goal)
        assertEquals(CommunicationStyle.ANALYTICAL, repository.style)
    }

    @Test
    fun `save social session keeps role play assessment local to repository`() = runBlocking {
        val repository = FakeSocialRepository()
        val assessment = SocialSelfAssessment(0, 4, 5, 5, 4, 5, "أريد إبطاء الإجابة", 1L)

        val sessionId = SaveScenarioSessionUseCase(repository)(2001, 7L, assessment)

        assertEquals(1L, sessionId)
        assertEquals(2001, repository.savedContentId)
        assertEquals(5, repository.savedAssessment?.respectScore)
    }

    @Test
    fun `daily boost use case returns the next private cue`() = runBlocking {
        val useCase = GetDailyCommunicationBoostUseCase(FakeBoostRepository())
        assertEquals("اترك وقفة قصيرة", useCase()?.text)
    }
}

private class FakeSocialRepository : SocialTrainingRepository {
    var scenario: String? = null
    var goal: String? = null
    var style: CommunicationStyle? = null
    var savedContentId: Int? = null
    var savedAssessment: SocialSelfAssessment? = null

    override suspend fun seedSocialTrainingIfNeeded() = Unit
    override fun observeContent(scenarioType: String?, goal: String?, style: CommunicationStyle?): Flow<List<SocialTrainingContent>> {
        this.scenario = scenarioType
        this.goal = goal
        this.style = style
        return MutableStateFlow(listOf(sampleContent()))
    }
    override suspend fun getContent(id: Int): SocialTrainingContent = sampleContent()
    override suspend fun saveSession(contentId: Int, recordingId: Long?, assessment: SocialSelfAssessment, selectedStyle: CommunicationStyle): Long {
        savedContentId = contentId
        savedAssessment = assessment
        return 1L
    }
    override fun observeStats(): Flow<SocialProgressStats> = MutableStateFlow(SocialProgressStats())
    override suspend fun clearSocialTrainingData() = Unit

    private fun sampleContent() = SocialTrainingContent(2001, "rapport", "عام", "networking", null, "beginner", 3, "build_professional_rapport", "عنوان", "نصيحة", "مثال", "قاعدة", "مطالبة", "عين", "جسد", "صوت", listOf(TrainingPhrase(PhraseType.OPENING, "مرحباً")))
}

private class FakeBoostRepository : DailyCommunicationBoostRepository {
    override suspend fun getAndMarkNextBoost() = DailyCommunicationBoost(1, "اترك وقفة قصيرة")
    override suspend fun clearHistory() = Unit
}
