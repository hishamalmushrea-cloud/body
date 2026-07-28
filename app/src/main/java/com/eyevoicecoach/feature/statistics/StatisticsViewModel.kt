package com.eyevoicecoach.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyevoicecoach.domain.model.ProgressStats
import com.eyevoicecoach.domain.repository.TipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Exposes local-only progress metrics for the statistics dashboard. */
@HiltViewModel
class StatisticsViewModel @Inject constructor(tips: TipRepository) : ViewModel() {
    /** Live aggregate progress derived solely from Room data. */
    val stats: StateFlow<ProgressStats> = tips.observeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressStats(0, 0, 0, 0, emptyMap()))
}
