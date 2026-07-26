package com.eyevoicecoach.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.core.notification.ReminderScheduler
import com.eyevoicecoach.domain.model.UserSettings
import com.eyevoicecoach.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Controls the encrypted context, visual theme and daily reminder settings. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminders: ReminderScheduler,
) : ViewModel() {
    /** Live user settings. */
    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    /** Selects the preferred context for future daily exercises. */
    fun selectContext(context: String) = viewModelScope.launch { settingsRepository.setContext(context) }

    /** Applies the requested visual theme. */
    fun selectTheme(themeKey: String) = viewModelScope.launch { settingsRepository.setTheme(themeKey) }

    /** Saves and schedules, or cancels, the daily reminder. */
    fun setReminder(enabled: Boolean, hour: Int, minute: Int) = viewModelScope.launch {
        settingsRepository.setReminder(enabled, hour, minute)
        reminders.update(enabled, hour, minute)
    }
}
