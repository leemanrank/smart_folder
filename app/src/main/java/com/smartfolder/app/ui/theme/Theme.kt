package com.smartfolder.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF1A1C1E),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBEE9FF),
    secondary = Color(0xFFB3CAD7),
    onSecondary = Color(0xFF1D3040),
    tertiary = Color(0xFFC5C4DD),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006781),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBEE9FF),
    onPrimaryContainer = Color(0xFF001F29),
    secondary = Color(0xFF4C6270),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF5C5B7C),
    error = Color(0xFFBA1A1A),
    background = Color(0xFFFCFCFF),
    surface = Color(0xFFFCFCFF),
)

@Composable
fun SmartFolderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
