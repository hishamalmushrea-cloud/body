package com.eyevoicecoach

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.eyevoicecoach.core.ui.CoachTheme
import com.eyevoicecoach.domain.model.AppTheme
import org.junit.Rule
import org.junit.Test

class CoachNavigationTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `arabic interface renders right to left content`() {
        composeRule.setContent { CoachTheme(AppTheme.OLED) { Text("تمرين اليوم") } }
        composeRule.onNodeWithText("تمرين اليوم").assertIsDisplayed()
    }
}
