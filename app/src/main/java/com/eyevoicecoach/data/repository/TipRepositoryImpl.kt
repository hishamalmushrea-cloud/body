package com.eyevoicecoach.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.eyevoicecoach.data.local.CategoryCount
import com.eyevoicecoach.data.local.CoachDatabase
import com.eyevoicecoach.data.local.FavoriteEntity
import com.eyevoicecoach.data.local.HistoryEntity
import com.eyevoicecoach.data.local.TipEntity
import com.eyevoicecoach.data.preferences.EncryptedPreferenceDataStore
import com.eyevoicecoach.domain.model.ProgressStats
import com.eyevoicecoach.domain.model.Tip
import com.eyevoicecoach.domain.repository.TipRepository
import java.io.BufferedReader
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column

/** Room-backed repository that preserves user history while importing bundled exercise updates. */
class TipRepositoryImpl @Inject constructor(
    private val context: Context,
    private val database: CoachDatabase,
    private val settingsStore: EncryptedPreferenceDataStore,
) : TipRepository {
    private val tips = database.tipDao()

    override suspend fun seedBundledTipsIfNeeded() {
        val json = context.assets.open("tips.json").bufferedReader().use(BufferedReader::readText)
        val root = JSONObject(json)
        val version = root.getInt("version_code")
        val currentVersion = settingsStore.stringFlow(DATA_VERSION, "0").first().toIntOrNull() ?: 0
        if (version <= currentVersion) return
        val array = root.getJSONArray("tips")
        val parsed = buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    TipEntity(
                        id = item.getInt("id"),
                        category = item.getString("category"),
                        context = item.getString("context"),
                        text = item.getString("tip_text"),
                        practicalExample = item.getString("practical_example"),
                        dailyChallenge = item.getString("daily_challenge"),
                        fridayScript = item.getString("friday_script"),
                    ),
                )
            }
        }
        database.withTransaction { tips.insertIgnoringExisting(parsed) }
        settingsStore.putString(DATA_VERSION, version.toString())
    }

    override suspend fun getAndMarkNextTip(context: String, isFriday: Boolean): Tip? = database.withTransaction {
        val selected = if (isFriday) {
            tips.nextFridayForContext(context) ?: if (context != "عام") tips.nextFridayForContext("عام") else null
        } else {
            tips.nextForContext(context) ?: if (context != "عام") tips.nextForContext("عام") else null
        }
        val fallback = selected ?: if (isFriday) tips.nextFridayAny() else tips.nextAny()
        fallback?.also { tips.insertHistory(HistoryEntity(tipId = it.id, dateShown = System.currentTimeMillis())) }?.toDomain()
    }

    override fun observeTips(context: String?): Flow<List<Tip>> =
        (if (context.isNullOrBlank() || context == "الكل") tips.observeAll() else tips.observeByContext(context))
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeFavorites(): Flow<List<Tip>> = tips.observeFavorites().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTip(id: Int): Tip? = tips.findById(id)?.toDomain()

    override suspend fun toggleFavorite(tipId: Int) {
        if (tips.observeIsFavorite(tipId).first()) {
            tips.removeFavorite(tipId)
        } else {
            tips.addFavorite(FavoriteEntity(tipId, System.currentTimeMillis()))
        }
    }

    override fun observeFavorite(tipId: Int): Flow<Boolean> = tips.observeIsFavorite(tipId)

    override suspend fun resetHistory() = tips.clearHistory()

    override suspend fun clearUserActivity() {
        tips.clearHistory()
        tips.clearFavorites()
    }

    override fun observeStats(): Flow<ProgressStats> = combine(
        tips.observeViewedCount(),
        tips.observeTipCount(),
        tips.observeFavoriteCount(),
        database.recordingDao().observeAll(),
        tips.observeCategoryCounts(),
    ) { viewed, total, favorites, recordings, categories ->
        ProgressStats(
            viewedCount = viewed,
            totalTips = total,
            favoriteCount = favorites,
            recordingCount = recordings.size,
            categoryCounts = categories.toMap(),
        )
    }

    private fun CategoryCount.toPair(): Pair<String, Int> = category to count

    private fun List<CategoryCount>.toMap(): Map<String, Int> = associate { it.toPair() }

    private fun TipEntity.toDomain() = Tip(id, category, context, text, practicalExample, dailyChallenge, fridayScript)

    private companion object {
        const val DATA_VERSION = "tips_data_version"
    }
}
