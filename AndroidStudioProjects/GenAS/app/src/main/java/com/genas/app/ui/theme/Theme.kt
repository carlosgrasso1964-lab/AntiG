package com.genas.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue800,
    onPrimary = CardBackground,
    primaryContainer = Blue100,
    secondary = Gray600,
    error = Red600,
    errorContainer = Red100,
    background = Gray100,
    surface = CardBackground,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun GenASTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
