package com.eyevoicecoach.data.repository

import com.eyevoicecoach.data.preferences.EncryptedPreferenceDataStore
import com.eyevoicecoach.domain.model.AppTheme
import com.eyevoicecoach.domain.model.UserSettings
import com.eyevoicecoach.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Encrypted DataStore implementation of user preference storage. */
class SettingsRepositoryImpl @Inject constructor(
    private val store: EncryptedPreferenceDataStore,
) : SettingsRepository {
    override val settings: Flow<UserSettings> = combine(
        store.stringFlow(ONBOARDING, "false"),
        store.stringFlow(CONTEXT, "عام"),
        store.stringFlow(THEME, AppTheme.OLED.key),
        store.stringFlow(REMINDERS, "false"),
        store.stringFlow(REMINDER_HOUR, "20"),
        store.stringFlow(REMINDER_MINUTE, "0"),
        store.stringFlow(SOCIAL_ETHICS, "false"),
    ) { values ->
        UserSettings(
            isOnboardingComplete = values[0].toBoolean(),
            selectedContext = values[1],
            theme = AppTheme.fromKey(values[2]),
            remindersEnabled = values[3].toBoolean(),
            reminderHour = values[4].toIntOrNull()?.coerceIn(0, 23) ?: 20,
            reminderMinute = values[5].toIntOrNull()?.coerceIn(0, 59) ?: 0,
            isSocialEthicsAcknowledged = values[6].toBoolean(),
        )
    }

    override suspend fun completeOnboarding() = store.putString(ONBOARDING, "true")

    override suspend fun setContext(context: String) = store.putString(CONTEXT, context)

    override suspend fun setTheme(themeKey: String) = store.putString(THEME, themeKey)

    override suspend fun clearAll() = store.clear()

    override suspend fun setSocialEthicsAcknowledged() = store.putString(SOCIAL_ETHICS, "true")

    override suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        store.putString(REMINDERS, enabled.toString())
        store.putString(REMINDER_HOUR, hour.coerceIn(0, 23).toString())
        store.putString(REMINDER_MINUTE, minute.coerceIn(0, 59).toString())
    }

    private companion object {
        const val ONBOARDING = "onboarding_complete"
        const val CONTEXT = "selected_context"
        const val THEME = "theme"
        const val REMINDERS = "reminders_enabled"
        const val REMINDER_HOUR = "reminder_hour"
        const val REMINDER_MINUTE = "reminder_minute"
        const val SOCIAL_ETHICS = "social_ethics_acknowledged"
    }
}
