package com.eyevoicecoach.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.model.Tip
import com.eyevoicecoach.domain.repository.SettingsRepository
import com.eyevoicecoach.domain.usecase.GetDailyTipUseCase
import com.eyevoicecoach.domain.usecase.ResetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Loads a daily no-repeat exercise and supports an explicit cycle reset. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDailyTip: GetDailyTipUseCase,
    private val resetHistory: ResetHistoryUseCase,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))

    /** State of today's exercise and any graceful loading error. */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { refresh() }

    /** Selects a new exercise under the currently selected context. */
    fun refresh() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        val context = settings.settings.first().selectedContext
        runCatching { getDailyTip(context) }
            .onSuccess { tip -> _uiState.value = HomeUiState(isLoading = false, tip = tip, context = context) }
            .onFailure { _uiState.value = HomeUiState(isLoading = false, context = context, error = "تعذر تحميل تمرين اليوم.") }
    }

    /** Clears history and immediately starts a fresh rotation. */
    fun resetAndRefresh() = viewModelScope.launch {
        resetHistory()
        refresh()
    }
}

/** Daily training content state. */
data class HomeUiState(
    val isLoading: Boolean = false,
    val tip: Tip? = null,
    val context: String = "عام",
    val error: String? = null,
)
