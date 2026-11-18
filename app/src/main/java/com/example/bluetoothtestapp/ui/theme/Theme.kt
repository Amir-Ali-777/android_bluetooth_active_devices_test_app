package com.example.bluetoothtestapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = ScoHighlight,
    onPrimary = Color.White,
    background = BackgroundTop,
    onBackground = Color.White,
    surface = BackgroundTop,
    onSurface = Color.White
)

@Composable
fun BluetoothTestAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
