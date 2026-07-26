package com.eyevoicecoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eyevoicecoach.core.ui.CoachTheme
import com.eyevoicecoach.feature.main.CoachRoot
import com.eyevoicecoach.feature.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

/** Single Compose activity for the entirely offline coaching experience. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                CoachTheme(state.settings.theme) {
                    CoachRoot(state = state, onFinishOnboarding = { viewModel.finishOnboarding() })
                }
            }
        }
    }
}
