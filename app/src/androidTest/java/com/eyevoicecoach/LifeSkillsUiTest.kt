package com.eyevoicecoach

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.eyevoicecoach.core.ui.CoachTheme
import com.eyevoicecoach.domain.lifeskills.LifeSkillsCatalog
import com.eyevoicecoach.domain.model.AppTheme
import com.eyevoicecoach.domain.model.LifeSkillsProgress
import com.eyevoicecoach.feature.lifeskills.LifeLessonScreen
import org.junit.Rule
import org.junit.Test

class LifeSkillsUiTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `life lesson renders explanation practice decision and private reflection`() {
        val track = LifeSkillsCatalog.tracks.first()
        val lesson = track.lessons.first()
        composeRule.setContent {
            CoachTheme(AppTheme.OLED) {
                LifeLessonScreen(track, lesson, LifeSkillsProgress(), onComplete = { _, _ -> }, onBack = {})
            }
        }
        composeRule.onNodeWithText("تدريب عملي").assertIsDisplayed()
        composeRule.onNodeWithText("اختيار تطبيقي").assertIsDisplayed()
        composeRule.onNodeWithText("تأمل وتطبيق خاص").assertIsDisplayed()
    }
}
