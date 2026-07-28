package com.eyevoicecoach.core.ui

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.eyevoicecoach.domain.model.AppTheme

private val DarkAcademyColorScheme = darkColorScheme(
    primary = Color(0xFFE5E5E5), // Off-white
    onPrimary = Color(0xFF0F0F0F),
    primaryContainer = Color(0xFF2A2A2A),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFB0B0B0), // Soft gray
    onSecondary = Color(0xFF0F0F0F),
    secondaryContainer = Color(0xFF1F1F1F),
    onSecondaryContainer = Color(0xFFE5E5E5),
    background = Color(0xFF0F0F0F), // True deep black/dark gray
    onBackground = Color(0xFFE5E5E5),
    surface = Color(0xFF141414), // Slightly elevated black for cards
    onSurface = Color(0xFFE5E5E5),
    surfaceVariant = Color(0xFF1F1F1F),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

@Composable
fun CoachTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val colorScheme = DarkAcademyColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CoachTypography,
        content = content
    )
}
