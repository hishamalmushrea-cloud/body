package com.eyevoicecoach.domain.usecase

import com.eyevoicecoach.domain.model.ProgressStats
import com.eyevoicecoach.domain.model.Recording
import com.eyevoicecoach.domain.model.Tip
import com.eyevoicecoach.domain.repository.RecordingRepository
import com.eyevoicecoach.domain.repository.TipRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UseCasesTest {
    @Test
    fun `daily selection records different exercises until repository is exhausted`() = runBlocking {
        val repository = InMemoryTips(listOf(sampleTip(1), sampleTip(2)))
        val useCase = GetDailyTipUseCase(repository)
        val monday = Calendar.getInstance().apply { set(2026, Calendar.JULY, 27) }

        val first = useCase("عام", monday)
        val second = useCase("عام", monday)
        val exhausted = useCase("عام", monday)

        assertEquals(setOf(1, 2), setOf(first?.id, second?.id))
        assertNull(exhausted)
    }

    @Test
    fun `friday selection asks repository for a Friday challenge`() = runBlocking {
        val repository = InMemoryTips(listOf(sampleTip(1)))
        val friday = Calendar.getInstance().apply { set(2026, Calendar.JULY, 31) }

        GetDailyTipUseCase(repository)("عام", friday)

        assertTrue(repository.lastFridayFlag)
    }

    @Test
    fun `retention use case delegates cleanup with supplied clock`() = runBlocking {
        val recordings = InMemoryRecordings()
        DeleteExpiredRecordingsUseCase(recordings)(1_234L)
        assertEquals(1_234L, recordings.cleanupAt)
    }

    private fun sampleTip(id: Int) = Tip(id, "الصوت", "عام", "نصيحة", "مثال", "تحدٍ", "نص")
}

private class InMemoryTips(private val available: MutableList<Tip>) : TipRepository {
    var lastFridayFlag = false

    override suspend fun seedBundledTipsIfNeeded() = Unit
    override suspend fun getAndMarkNextTip(context: String, isFriday: Boolean): Tip? {
        lastFridayFlag = isFriday
        return available.removeFirstOrNull()
    }
    override fun observeTips(context: String?): Flow<List<Tip>> = MutableStateFlow(available)
    override fun observeFavorites(): Flow<List<Tip>> = emptyFlow()
    override suspend fun getTip(id: Int): Tip? = available.firstOrNull { it.id == id }
    override suspend fun toggleFavorite(tipId: Int) = Unit
    override fun observeFavorite(tipId: Int): Flow<Boolean> = MutableStateFlow(false)
    override suspend fun resetHistory() = Unit
    override fun observeStats(): Flow<ProgressStats> = MutableStateFlow(ProgressStats(0, 0, 0, 0, emptyMap()))
}

private class InMemoryRecordings : RecordingRepository {
    var cleanupAt: Long? = null
    override fun observeRecordings(): Flow<List<Recording>> = emptyFlow()
    override suspend fun addRecording(uri: String, tipId: Int, durationSeconds: Int) = Unit
    override suspend fun deleteRecording(recording: Recording) = Unit
    override suspend fun deleteExpiredRecordings(nowMillis: Long) { cleanupAt = nowMillis }
}
