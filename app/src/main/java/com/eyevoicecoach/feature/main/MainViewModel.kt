package com.eyevoicecoach.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.model.UserSettings
import com.eyevoicecoach.domain.repository.SettingsRepository
import com.eyevoicecoach.domain.repository.TipRepository
import com.eyevoicecoach.domain.repository.SocialTrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Coordinates startup data import and the global encrypted settings state. */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val tips: TipRepository,
    private val socialTraining: SocialTrainingRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())

    /** Startup and settings state used by the root navigation host. */
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                tips.seedBundledTipsIfNeeded()
                socialTraining.seedSocialTrainingIfNeeded()
            }
                .onFailure { _uiState.value = _uiState.value.copy(error = "تعذر تجهيز التمارين. أعد فتح التطبيق للمحاولة.") }
            settingsRepository.settings.collectLatest { settings ->
                _uiState.value = _uiState.value.copy(isLoading = false, settings = settings)
            }
        }
    }

    /** Completes the locally stored introduction. */
    fun finishOnboarding() = viewModelScope.launch { settingsRepository.completeOnboarding() }
}

/** Root rendering state. */
data class MainUiState(
    val isLoading: Boolean = true,
    val settings: UserSettings = UserSettings(),
    val error: String? = null,
)
