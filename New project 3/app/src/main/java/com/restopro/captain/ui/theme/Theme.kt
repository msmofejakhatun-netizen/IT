package com.restopro.captain.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CaptainColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color.White,
    secondary = Color(0xFF334155),
    tertiary = Color(0xFFF97316),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626)
)

@Composable
fun RestoProCaptainTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CaptainColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
