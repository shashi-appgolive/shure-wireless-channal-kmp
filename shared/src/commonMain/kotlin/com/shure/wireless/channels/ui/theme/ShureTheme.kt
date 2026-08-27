package com.shure.wireless.channels.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ShureColors {
    val Black = Color(0xFF000000)
    val Background = Color(0xFF080B0A)
    val Surface = Color(0xFF111614)
    val SurfaceElevated = Color(0xFF18201C)
    val Border = Color(0xFF26312C)
    val Green = Color(0xFFA6FF00)
    val GreenMuted = Color(0xFF6EA900)
    val OnGreen = Color(0xFF071000)
    val TextPrimary = Color(0xFFF5F7F6)
    val TextMuted = Color(0xFF98A39D)
    val Error = Color(0xFFFF6B6B)
    val Warning = Color(0xFFFFC857)
    val Event = Color(0xFF62B5FF)
}

private val ShureDarkColorScheme = darkColorScheme(
    primary = ShureColors.Green,
    onPrimary = ShureColors.OnGreen,
    primaryContainer = ShureColors.GreenMuted,
    onPrimaryContainer = ShureColors.Black,
    secondary = ShureColors.Event,
    onSecondary = ShureColors.Black,
    background = ShureColors.Black,
    onBackground = ShureColors.TextPrimary,
    surface = ShureColors.Surface,
    onSurface = ShureColors.TextPrimary,
    surfaceVariant = ShureColors.SurfaceElevated,
    onSurfaceVariant = ShureColors.TextMuted,
    outline = ShureColors.Border,
    error = ShureColors.Error,
    onError = ShureColors.Black,
)

@Composable
fun ShureTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ShureDarkColorScheme,
        content = content,
    )
}
