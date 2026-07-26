package com.eyevoicecoach.data.repository

import android.content.Context
import com.eyevoicecoach.data.preferences.EncryptedPreferenceDataStore
import com.eyevoicecoach.domain.model.DailyCommunicationBoost
import com.eyevoicecoach.domain.repository.DailyCommunicationBoostRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import org.json.JSONObject

/** Encrypted-preferences repository for a locally rotating daily communication cue. */
class DailyCommunicationBoostRepositoryImpl @Inject constructor(
    private val context: Context,
    private val store: EncryptedPreferenceDataStore,
) : DailyCommunicationBoostRepository {
    override suspend fun getAndMarkNextBoost(): DailyCommunicationBoost? {
        val boosts = readBoosts()
        if (boosts.isEmpty()) return null
        val seen = store.stringFlow(SEEN_IDS, "").first().split(',').mapNotNull(String::toIntOrNull).toSet()
        val next = boosts.filterNot { it.id in seen }.randomOrNull()
        if (next == null) return null
        store.putString(SEEN_IDS, (seen + next.id).sorted().joinToString(","))
        return next
    }

    override suspend fun clearHistory() = store.putString(SEEN_IDS, "")

    private fun readBoosts(): List<DailyCommunicationBoost> {
        val root = context.assets.open("daily_boost.json").bufferedReader().use { JSONObject(it.readText()) }
        val values = root.getJSONArray("boosts")
        return buildList {
            for (index in 0 until values.length()) {
                val item = values.getJSONObject(index)
                add(DailyCommunicationBoost(item.getInt("id"), item.getString("text")))
            }
        }
    }

    private companion object { const val SEEN_IDS = "seen_daily_communication_boost_ids" }
}
