package com.eyevoicecoach

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.eyevoicecoach.core.ui.CoachTheme
import com.eyevoicecoach.domain.model.AppTheme
import com.eyevoicecoach.domain.model.CommunicationStyle
import com.eyevoicecoach.domain.model.PhraseType
import com.eyevoicecoach.domain.model.SocialTrainingContent
import com.eyevoicecoach.domain.model.TrainingPhrase
import com.eyevoicecoach.domain.model.UserSettings
import com.eyevoicecoach.feature.social.SocialTrainingCardScreen
import com.eyevoicecoach.feature.social.SocialTrainingScreen
import org.junit.Rule
import org.junit.Test

class SocialTrainingUiTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `social card renders the role play prompt and ethical boundary`() {
        val content = SocialTrainingContent(2001, "social_opening", "social_basics", "عام", "public_micro_interaction", CommunicationStyle.UNKNOWN, "beginner", 3, "start", "بدء حديث محترم", "وصف الموقف", "مثال", "إذا لم يبدُ الطرف الآخر مهتماً، أنهِ الحديث بلطف.", "قل شكراً ثم أنهِ الحديث.", "العين", "الجسد", "الصوت", listOf(TrainingPhrase(PhraseType.OPENING, "عفواً، هل تعرف أين المدخل؟")))
        composeRule.setContent { CoachTheme(AppTheme.OLED) { SocialTrainingCardScreen(content, null, CommunicationStyle.UNKNOWN, onRecord = { _, _ -> }, onBack = {}) } }
        composeRule.onNodeWithText("تحدي التسجيل").assertIsDisplayed()
        composeRule.onNodeWithText("قاعدة الاحترام").assertIsDisplayed()
    }

    @Test
    fun `choosing client scenario displays client content only`() {
        val client = SocialTrainingContent(2002, "client_communication", "understanding_needs", "زبون", "client_meeting", null, "beginner", 3, "goal", "جلسة العميل", "النص", "مثال", "حدود", "مطالبة", "عين", "جسد", "صوت", emptyList())
        val public = SocialTrainingContent(2001, "social_opening", "social_basics", "عام", "public_micro_interaction", null, "beginner", 3, "goal", "جلسة عامة", "النص", "مثال", "حدود", "مطالبة", "عين", "جسد", "صوت", emptyList())
        composeRule.setContent {
            var scenario by remember { mutableStateOf<String?>(null) }
            val visible = listOf(client, public).filter { scenario == null || it.scenarioType == scenario }
            CoachTheme(AppTheme.OLED) {
                SocialTrainingScreen(
                    settings = UserSettings(isSocialEthicsAcknowledged = true),
                    boost = null,
                    content = visible,
                    selectedStyle = null,
                    onScenario = { scenario = it }, onGoal = {}, onStyle = {}, onTrack = {}, onRefreshBoost = {}, onAcknowledgeEthics = {}, onOpenCard = { _, _ -> }, onStyleAssistant = {}, onBack = {},
                )
            }
        }
        composeRule.onNodeWithText("لقاء عميل").performClick()
        composeRule.onNodeWithText("جلسة العميل").assertIsDisplayed()
        composeRule.onNodeWithText("جلسة عامة").assertDoesNotExist()
    }
}
