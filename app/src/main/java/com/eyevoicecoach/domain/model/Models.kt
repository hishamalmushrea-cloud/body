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
