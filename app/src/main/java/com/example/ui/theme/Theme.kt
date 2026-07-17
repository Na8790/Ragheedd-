package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue,
    secondary = WarmGold,
    tertiary = EmeraldGreen,
    background = Color(0xFF0B0F19), // Midnight Blue
    surface = Color(0xFF151F32),    // Slate Surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFF1F5F9),
    error = RedError
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBlue,
    secondary = WarmGold,
    tertiary = EmeraldGreen,
    background = OffWhite,
    surface = SurfaceWhite,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkGray,
    onSurface = DarkGray,
    error = RedError
)

@Composable
fun TajribahTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We enforce our premium luxury colors rather than dynamic system colors
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}

// Keep the original name for backward compatibility with pre-generated files if any
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    TajribahTheme(darkTheme = darkTheme, content = content)
}
