package com.eyevoicecoach.core.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.eyevoicecoach.domain.model.AppTheme

/** Applies one of the six calm, high-contrast themes used by the coaching experience. */
@Composable
fun CoachTheme(theme: AppTheme, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorsFor(theme), content = content)
}

private fun colorsFor(theme: AppTheme): ColorScheme = when (theme) {
    AppTheme.OLED -> darkColorScheme(primary = Color(0xFFC9A227), secondary = Color(0xFFE6CF7C), background = Color.Black, surface = Color(0xFF121212), onPrimary = Color.Black)
    AppTheme.CLASSIC -> lightColorScheme(primary = Color(0xFF735C00), secondary = Color(0xFF715C2A), background = Color(0xFFFFFBFF), surface = Color(0xFFFFFBFF))
    AppTheme.NAVY -> darkColorScheme(primary = Color(0xFF9CCAFF), secondary = Color(0xFFB6C8E6), background = Color(0xFF071A33), surface = Color(0xFF102743))
    AppTheme.EMERALD -> darkColorScheme(primary = Color(0xFF8ADEB2), secondary = Color(0xFFB4E8CD), background = Color(0xFF061F17), surface = Color(0xFF0D3025))
    AppTheme.BURGUNDY -> darkColorScheme(primary = Color(0xFFFFB1B6), secondary = Color(0xFFEAC0C3), background = Color(0xFF26080F), surface = Color(0xFF42151E))
    AppTheme.IMPERIAL -> darkColorScheme(primary = Color(0xFFDCC0FF), secondary = Color(0xFFE6D7FF), background = Color(0xFF1B102D), surface = Color(0xFF302044))
}
