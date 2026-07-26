package com.eyevoicecoach.feature.home

import com.eyevoicecoach.domain.model.ProgressStats
import com.eyevoicecoach.domain.model.Tip
import com.eyevoicecoach.domain.model.UserSettings
import com.eyevoicecoach.domain.repository.SettingsRepository
import com.eyevoicecoach.domain.repository.TipRepository
import com.eyevoicecoach.domain.usecase.GetDailyTipUseCase
import com.eyevoicecoach.domain.usecase.ResetHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @Test
    fun `view model exposes selected context and loaded daily tip`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = HomeFakeTips()
            val settings = HomeFakeSettings()
            val viewModel = HomeViewModel(GetDailyTipUseCase(repository), ResetHistoryUseCase(repository), settings)

            advanceUntilIdle()

            assertEquals("اجتماع", viewModel.uiState.value.context)
            assertEquals(7, viewModel.uiState.value.tip?.id)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class HomeFakeTips : TipRepository {
    override suspend fun seedBundledTipsIfNeeded() = Unit
    override suspend fun getAndMarkNextTip(context: String, isFriday: Boolean): Tip? = Tip(7, "العين", context, "نصيحة", "مثال", "تحدٍ", "نص")
    override fun observeTips(context: String?): Flow<List<Tip>> = emptyFlow()
    override fun observeFavorites(): Flow<List<Tip>> = emptyFlow()
    override suspend fun getTip(id: Int): Tip? = null
    override suspend fun toggleFavorite(tipId: Int) = Unit
    override fun observeFavorite(tipId: Int): Flow<Boolean> = MutableStateFlow(false)
    override suspend fun resetHistory() = Unit
    override suspend fun clearUserActivity() = Unit
    override fun observeStats(): Flow<ProgressStats> = emptyFlow()
}

private class HomeFakeSettings : SettingsRepository {
    override val settings: Flow<UserSettings> = MutableStateFlow(UserSettings(selectedContext = "اجتماع"))
    override suspend fun completeOnboarding() = Unit
    override suspend fun setContext(context: String) = Unit
    override suspend fun setTheme(themeKey: String) = Unit
    override suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) = Unit
    override suspend fun clearAll() = Unit
}
