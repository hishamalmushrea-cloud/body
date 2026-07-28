package com.eyevoicecoach.domain.model

/** A coaching exercise displayed to the user. */
data class Tip(
    val id: Int,
    val category: String,
    val context: String,
    val text: String,
    val practicalExample: String,
    val dailyChallenge: String,
    val fridayScript: String,
)

/** A locally stored voice recording and its associated exercise. */
data class Recording(
    val id: Long,
    val uri: String,
    val recordedAt: Long,
    val tipId: Int,
    val durationSeconds: Int,
)

/** Aggregate progress information for the statistics screen. */
data class ProgressStats(
    val viewedCount: Int,
    val totalTips: Int,
    val favoriteCount: Int,
    val recordingCount: Int,
    val categoryCounts: Map<String, Int>,
)

/** Selectable visual themes available to the user. */
enum class AppTheme(val key: String, val label: String) {
    OLED("oled", "الأسود الذهبي"),
    CLASSIC("classic", "الأبيض الكلاسيكي"),
    NAVY("navy", "الأزرق الداكن"),
    EMERALD("emerald", "الأخضر الملكي"),
    BURGUNDY("burgundy", "الأحمر العنابي"),
    IMPERIAL("imperial", "البنفسجي الإمبراطوري");

    companion object {
        /** Returns the theme saved with [key], falling back to the OLED theme. */
        fun fromKey(key: String): AppTheme = entries.firstOrNull { it.key == key } ?: OLED
    }
}

/** Persistent settings controlling the experience and reminders. */
data class UserSettings(
    val isOnboardingComplete: Boolean = false,
    val selectedContext: String = "عام",
    val theme: AppTheme = AppTheme.OLED,
    val remindersEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val isSocialEthicsAcknowledged: Boolean = false,
)

/** A structured self-review saved privately after a voice recording. */
data class SelfAssessment(
    val recordingId: Long,
    val clarity: Int,
    val pace: Int,
    val confidence: Int,
    val pauses: Int,
    val tension: TensionLevel,
    val note: String,
    val createdAt: Long,
)

/** Self-reported tension levels used in a recording review. */
enum class TensionLevel(val label: String) {
    LOW("منخفض"),
    MEDIUM("متوسط"),
    HIGH("عالٍ");

    companion object {
        /** Restores a persisted tension value, safely defaulting to medium. */
        fun fromName(value: String): TensionLevel = entries.firstOrNull { it.name == value } ?: MEDIUM
    }
}

/** Completion state for one ready-made coaching program. */
data class ProgramProgress(
    val programId: String,
    val completedDays: Set<Int> = emptySet(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
) {
    /** Returns the first unfinished day between one and [totalDays]. */
    fun nextDay(totalDays: Int): Int = (1..totalDays).firstOrNull { it !in completedDays } ?: totalDays

    /** Returns the numeric completion percentage. */
    fun percentage(totalDays: Int): Int = if (totalDays == 0) 0 else (completedDays.size * 100 / totalDays).coerceAtMost(100)
}

/** A refined, earned milestone presented in the achievements section. */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val symbol: String,
    val isUnlocked: Boolean,
    val progress: Int,
    val target: Int,
)

/** Private social-communication training content designed around consent and respect. */
data class SocialTrainingContent(
    val id: Int,
    val moduleCategory: String,
    val socialTrack: String,
    val context: String,
    val scenarioType: String,
    val personalityStyle: CommunicationStyle?,
    val difficulty: String,
    val durationMinutes: Int,
    val communicationGoal: String,
    val title: String,
    val tipText: String,
    val practicalExample: String,
    val boundaryRule: String,
    val roleplayPrompt: String,
    val eyeContactTip: String,
    val bodyLanguageTip: String,
    val voiceTip: String,
    val phrases: List<TrainingPhrase>,
)

/** A safe phrase associated with a social communication training card. */
data class TrainingPhrase(val type: PhraseType, val text: String)

/** Supported phrase purposes on a social training card. */
enum class PhraseType(val key: String, val label: String) {
    OPENING("opening", "افتتاحية مقترحة"),
    DO_SAY("do_say", "عبارة مناسبة"),
    AVOID_SAY("avoid_say", "ما لا تقله"),
    CLOSING("closing", "خاتمة محترمة");

    companion object {
        /** Restores a phrase type from persisted storage. */
        fun fromKey(key: String): PhraseType = entries.firstOrNull { it.key == key } ?: OPENING
    }
}

/** A communication style selection used only to adapt wording, never to diagnose a person. */
enum class CommunicationStyle(val code: String, val label: String, val guidance: String) {
    DIRECT("D", "الحازم / الموجّه للنتائج", "ابدأ بالخلاصة، اذكر الفائدة، ولا تكثر التفاصيل."),
    EXPRESSIVE("I", "التعبيري / الاجتماعي", "استخدم نبرة دافئة وقصة قصيرة قبل التفاصيل."),
    SUPPORTIVE("S", "الداعم / الهادئ", "تحدث بهدوء، امنح وقتاً، واشرح الخطوات بوضوح."),
    ANALYTICAL("C", "التحليلي / الدقيق", "قدم بيانات وأمثلة وشروطاً دقيقة، وتجنب المبالغة."),
    UNKNOWN("unknown", "غير معروف", "ابدأ بسؤال مفتوح واستمع قبل أن تعدّل أسلوبك.");

    companion object {
        /** Restores a style code without ادعاء معرفة نمط الطرف الآخر. */
        fun fromCode(code: String?): CommunicationStyle = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

/** Summary of one completed private social training attempt. */
data class SocialSession(
    val id: Long,
    val contentId: Int,
    val scenarioType: String,
    val personalityStyle: CommunicationStyle,
    val communicationGoal: String,
    val completedAt: Long,
    val recordingId: Long?,
)

/** A subjective, private social communication review saved after role-play. */
data class SocialSelfAssessment(
    val sessionId: Long,
    val clarityScore: Int,
    val confidenceScore: Int,
    val respectScore: Int,
    val listeningScore: Int,
    val calmnessScore: Int,
    val notes: String,
    val createdAt: Long,
)

/** One concise daily communication practice cue. */
data class DailyCommunicationBoost(val id: Int, val text: String)

/** Aggregated local-only social practice progress. */
data class SocialProgressStats(
    val completedSessions: Int = 0,
    val mostPracticedScenario: String? = null,
    val strongestModule: String? = null,
    val averageConfidence: Float = 0f,
    val averageRespect: Float = 0f,
    val averageCalmness: Float = 0f,
    val rejectionPracticeCount: Int = 0,
    val mostSelectedStyle: CommunicationStyle? = null,
    val currentStreak: Int = 0,
)

/** One practical path in the فن التعامل library. */
data class SocialSkillTrack(val id: String, val title: String, val description: String)

/** A short ethical choice exercise that explains why one response fits the situation best. */
data class SocialChoiceExercise(
    val id: String,
    val contentId: Int,
    val prompt: String,
    val options: List<String>,
    val preferredIndex: Int,
    val explanation: String,
)

/** A private completion record for one life-skills lesson. */
data class LifeLessonCompletion(
    val lessonId: String,
    val trackId: String,
    val completedAt: Long,
    val reflection: String,
    val chosenOption: Int?,
)

/** Aggregated private progress across the educational life-skills tracks. */
data class LifeSkillsProgress(
    val completedLessonIds: Set<String> = emptySet(),
    val reflections: Map<String, String> = emptyMap(),
) {
    /** Returns completion percentage for [totalLessons] without exceeding 100. */
    fun percentage(totalLessons: Int): Int =
        if (totalLessons <= 0) 0 else (completedLessonIds.size * 100 / totalLessons).coerceAtMost(100)
}
