package com.eyevoicecoach.data.repository

import android.content.Context
import com.eyevoicecoach.domain.model.SocialChoiceExercise
import com.eyevoicecoach.domain.repository.SocialChoiceRepository
import javax.inject.Inject
import org.json.JSONObject

/** Asset-backed repository for short ethical response-choice exercises. */
class SocialChoiceRepositoryImpl @Inject constructor(private val context: Context) : SocialChoiceRepository {
    override suspend fun getChoice(contentId: Int): SocialChoiceExercise? = readChoices().firstOrNull { it.contentId == contentId }

    private fun readChoices(): List<SocialChoiceExercise> {
        val root = context.assets.open("social_choices.json").bufferedReader().use { JSONObject(it.readText()) }
        val values = root.getJSONArray("choices")
        return buildList {
            for (index in 0 until values.length()) {
                val item = values.getJSONObject(index)
                val options = item.getJSONArray("options")
                add(
                    SocialChoiceExercise(
                        id = item.getString("id"),
                        contentId = item.getInt("content_id"),
                        prompt = item.getString("prompt"),
                        options = List(options.length()) { optionIndex -> options.getString(optionIndex) },
                        preferredIndex = item.getInt("preferred_index"),
                        explanation = item.getString("explanation"),
                    ),
                )
            }
        }
    }
}
