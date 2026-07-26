package com.eyevoicecoach.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.data.local.SocialContentWithPhrases
import com.eyevoicecoach.data.local.SocialSelfAssessmentEntity
import com.eyevoicecoach.data.local.SocialSessionAnalytics
import com.eyevoicecoach.data.local.SocialSessionEntity
import com.eyevoicecoach.data.local.SocialTrainingContentEntity
import com.eyevoicecoach.data.local.SocialTrainingPhraseEntity
import com.eyevoicecoach.data.preferences.EncryptedPreferenceDataStore
import com.eyevoicecoach.domain.model.CommunicationStyle
import com.eyevoicecoach.domain.model.PhraseType
import com.eyevoicecoach.domain.model.SocialProgressStats
import com.eyevoicecoach.domain.model.SocialSelfAssessment
import com.eyevoicecoach.domain.model.SocialTrainingContent
import com.eyevoicecoach.domain.model.TrainingPhrase
import com.eyevoicecoach.domain.social.ValidateContentEthicsUseCase
import com.eyevoicecoach.domain.repository.SocialTrainingRepository
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/** Room-backed ethical social-training repository with local content safety validation. */
class SocialTrainingRepositoryImpl @Inject constructor(
    private val context: Context,
    private val database: CoachDatabase,
    private val encryptedStore: EncryptedPreferenceDataStore,
    private val validateContentEthics: ValidateContentEthicsUseCase,
) : SocialTrainingRepository {
    private val social = database.socialTrainingDao()

    override suspend fun seedSocialTrainingIfNeeded() {
        val root = context.assets.open("social_training.json").bufferedReader().use { reader -> JSONObject(reader.readText()) }
        val version = root.getInt("version_code")
        if (version <= (encryptedStore.stringFlow(CONTENT_VERSION, "0").first().toIntOrNull() ?: 0)) return
        val contents = mutableListOf<SocialTrainingContentEntity>()
        val phrases = mutableListOf<SocialTrainingPhraseEntity>()
        val items = root.getJSONArray("content")
        for (index in 0 until items.length()) {
            val item = items.getJSONObject(index)
            val contentId = item.getInt("id")
            val phraseArray = item.getJSONArray("phrases")
            val textValues = buildList {
                add(item.getString("title")); add(item.getString("tip_text")); add(item.getString("practical_example")); add(item.getString("boundary_rule")); add(item.getString("roleplay_prompt"))
                for (phraseIndex in 0 until phraseArray.length()) add(phraseArray.getJSONObject(phraseIndex).getString("text"))
            }
            require(validateContentEthics(textValues)) { "محتوى تواصل غير متوافق مع قواعد الاحترام" }
            contents += SocialTrainingContentEntity(
                id = contentId,
                moduleCategory = item.getString("module_category"),
                context = item.getString("context"),
                scenarioType = item.getString("scenario_type"),
                personalityStyle = item.optString("personality_style").ifBlank { null },
                difficulty = item.getString("difficulty"),
                durationMinutes = item.getInt("duration_minutes"),
                communicationGoal = item.getString("communication_goal"),
                title = item.getString("title"),
                tipText = item.getString("tip_text"),
                practicalExample = item.getString("practical_example"),
                boundaryRule = item.getString("boundary_rule"),
                roleplayPrompt = item.getString("roleplay_prompt"),
                eyeContactTip = item.getString("eye_contact_tip"),
                bodyLanguageTip = item.getString("body_language_tip"),
                voiceTip = item.getString("voice_tip"),
                contentVersion = item.getInt("content_version"),
                contentSafetyLevel = item.getString("content_safety_level"),
            )
            for (phraseIndex in 0 until phraseArray.length()) {
                val phrase = phraseArray.getJSONObject(phraseIndex)
                phrases += SocialTrainingPhraseEntity(contentId = contentId, phraseType = phrase.getString("type"), text = phrase.getString("text"))
            }
        }
        database.withTransaction {
            social.insertContentIgnoringExisting(contents)
            social.insertPhrasesIgnoringExisting(phrases)
        }
        encryptedStore.putString(CONTENT_VERSION, version.toString())
    }

    override fun observeContent(scenarioType: String?, goal: String?, style: CommunicationStyle?): Flow<List<SocialTrainingContent>> =
        social.observeAllWithPhrases().map { rows ->
            rows.map(SocialContentWithPhrases::toDomain).filter { content ->
                (scenarioType.isNullOrBlank() || scenarioType == "الكل" || content.scenarioType == scenarioType) &&
                    (goal.isNullOrBlank() || goal == "الكل" || content.communicationGoal == goal) &&
                    (style == null || style == CommunicationStyle.UNKNOWN || content.personalityStyle == null || content.personalityStyle == style)
            }
        }

    override suspend fun getContent(id: Int): SocialTrainingContent? = social.findWithPhrases(id)?.toDomain()

    override suspend fun saveSession(contentId: Int, recordingId: Long?, assessment: SocialSelfAssessment, selectedStyle: CommunicationStyle): Long = database.withTransaction {
        val content = requireNotNull(social.findWithPhrases(contentId)) { "بطاقة التدريب غير موجودة" }.content
        val sessionId = social.insertSession(
            SocialSessionEntity(
                contentId = contentId,
                sessionMode = "roleplay",
                scenarioType = content.scenarioType,
                personalityStyle = if (selectedStyle != CommunicationStyle.UNKNOWN) selectedStyle.code else content.personalityStyle ?: CommunicationStyle.UNKNOWN.code,
                communicationGoal = content.communicationGoal,
                dateCompleted = System.currentTimeMillis(),
                recordingId = recordingId,
            ),
        )
        social.insertAssessment(
            SocialSelfAssessmentEntity(
                sessionId = sessionId,
                clarityScore = assessment.clarityScore.coerceIn(1, 5),
                confidenceScore = assessment.confidenceScore.coerceIn(1, 5),
                respectScore = assessment.respectScore.coerceIn(1, 5),
                listeningScore = assessment.listeningScore.coerceIn(1, 5),
                calmnessScore = assessment.calmnessScore.coerceIn(1, 5),
                notes = assessment.notes.trim(),
                createdAt = assessment.createdAt,
            ),
        )
        sessionId
    }

    override fun observeStats(): Flow<SocialProgressStats> = social.observeAnalytics().map(::toStats)

    override suspend fun clearSocialTrainingData() = database.withTransaction {
        social.clearAssessments()
        social.clearSessions()
    }

    private fun SocialContentWithPhrases.toDomain() = SocialTrainingContent(
        id = content.id,
        moduleCategory = content.moduleCategory,
        context = content.context,
        scenarioType = content.scenarioType,
        personalityStyle = content.personalityStyle?.let(CommunicationStyle::fromCode),
        difficulty = content.difficulty,
        durationMinutes = content.durationMinutes,
        communicationGoal = content.communicationGoal,
        title = content.title,
        tipText = content.tipText,
        practicalExample = content.practicalExample,
        boundaryRule = content.boundaryRule,
        roleplayPrompt = content.roleplayPrompt,
        eyeContactTip = content.eyeContactTip,
        bodyLanguageTip = content.bodyLanguageTip,
        voiceTip = content.voiceTip,
        phrases = phrases.map { TrainingPhrase(PhraseType.fromKey(it.phraseType), it.text) },
    )

    private fun toStats(rows: List<SocialSessionAnalytics>): SocialProgressStats {
        if (rows.isEmpty()) return SocialProgressStats()
        val moduleScores = rows.groupBy(SocialSessionAnalytics::moduleCategory).mapValues { (_, sessions) ->
            val scores = sessions.map { analytics -> listOfNotNull(analytics.confidenceScore, analytics.respectScore, analytics.calmnessScore).average() }
            if (scores.size > 1) scores.first() - scores.last() else scores.firstOrNull() ?: 0.0
        }
        val scenarioCounts = rows.groupingBy(SocialSessionAnalytics::scenarioType).eachCount()
        val styles = rows.filter { it.personalityStyle != CommunicationStyle.UNKNOWN.code }.groupingBy(SocialSessionAnalytics::personalityStyle).eachCount()
        return SocialProgressStats(
            completedSessions = rows.size,
            mostPracticedScenario = scenarioCounts.maxByOrNull { it.value }?.key,
            strongestModule = moduleScores.maxByOrNull { it.value }?.key,
            averageConfidence = rows.mapNotNull(SocialSessionAnalytics::confidenceScore).average().toFloat(),
            averageRespect = rows.mapNotNull(SocialSessionAnalytics::respectScore).average().toFloat(),
            averageCalmness = rows.mapNotNull(SocialSessionAnalytics::calmnessScore).average().toFloat(),
            rejectionPracticeCount = rows.count { it.moduleCategory in setOf("boundaries", "objection_handling", "conflict_deescalation") },
            mostSelectedStyle = styles.maxByOrNull { it.value }?.key?.let(CommunicationStyle::fromCode),
            currentStreak = socialStreak(rows),
        )
    }

    private fun socialStreak(rows: List<SocialSessionAnalytics>): Int {
        val days = rows.map { Instant.ofEpochMilli(it.dateCompleted).atZone(ZoneId.systemDefault()).toLocalDate() }.distinct().sortedDescending()
        if (days.isEmpty()) return 0
        val today = java.time.LocalDate.now()
        if (days.first() != today && days.first() != today.minusDays(1)) return 0
        var streak = 1
        days.zipWithNext().forEach { (newer, older) -> if (newer.minusDays(1) == older) streak += 1 else return streak }
        return streak
    }

    private companion object { const val CONTENT_VERSION = "social_training_content_version" }
}
