package com.eyevoicecoach.feature.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.model.Tip
import com.eyevoicecoach.domain.repository.TipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Streams exercises for browsing by category and context. */
@HiltViewModel
class TrainingViewModel @Inject constructor(private val tips: TipRepository) : ViewModel() {
    private val _filter = MutableStateFlow("الكل")
    private val _context = MutableStateFlow("الكل")
    private val _allTips = tips.observeTips().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(TrainingUiState())

    /** All locally available exercises matching the active filters. */
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _allTips.collect {
                refreshState()
            }
        }
    }

    /** Applies a category filter. */
    fun setCategory(category: String) { _filter.value = category; refreshState() }

    /** Applies a context filter. */
    fun setContext(context: String) { _context.value = context; refreshState() }

    private fun refreshState() {
        _uiState.value = _uiState.value.copy(
            category = _filter.value,
            context = _context.value,
            tips = _allTips.value.filter { (_filter.value == "الكل" || it.category == _filter.value) && (_context.value == "الكل" || it.context == _context.value) },
        )
    }
}

/** Filtered training list state. */
data class TrainingUiState(val category: String = "الكل", val context: String = "الكل", val tips: List<Tip> = emptyList())

/** Loads one exercise and exposes its favorite state. */
@HiltViewModel
class TipDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tips: TipRepository,
) : ViewModel() {
    private val tipId = checkNotNull(savedStateHandle.get<Int>("tipId"))
    private val _tip = MutableStateFlow<Tip?>(null)

    /** Selected exercise, or null while it is being read. */
    val tip: StateFlow<Tip?> = _tip.asStateFlow()

    /** Favorite state for the selected exercise. */
    val isFavorite: StateFlow<Boolean> = tips.observeFavorite(tipId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init { viewModelScope.launch { _tip.value = tips.getTip(tipId) } }

    /** Adds or removes the selected exercise from favorites. */
    fun toggleFavorite() = viewModelScope.launch { tips.toggleFavorite(tipId) }
}

/** Streams favorite exercises. */
@HiltViewModel
class FavoritesViewModel @Inject constructor(tips: TipRepository) : ViewModel() {
    /** Favorited exercise list. */
    val favorites: StateFlow<List<Tip>> = tips.observeFavorites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
