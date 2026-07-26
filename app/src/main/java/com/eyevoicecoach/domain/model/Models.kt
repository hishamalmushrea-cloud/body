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
