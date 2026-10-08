package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.shure.wireless.channels.ui.theme.ShureDimens

private val MeterTrack = Color(0xFF252525)
private val RfTrack = Color(0xFF303030)
private val MeterGreen = Color(0xFF9CFF00)

@Composable
fun AudioMeter(progress: Float) {
    BoxWithConstraints(
        Modifier.fillMaxWidth().height(ShureDimens.MeterHeight).background(MeterTrack, RoundedCornerShape(8.dp)),
    ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        Box(
            Modifier.fillMaxWidth(progress).height(ShureDimens.MeterHeight).background(
                Brush.horizontalGradient(
                    colors = listOf(MeterGreen, Color(0xFFFFD54F), Color(0xFFE85D5D)),
                    startX = 0f,
                    endX = widthPx.coerceAtLeast(1f),
                ),
                RoundedCornerShape(8.dp),
            ),
        )
    }
}

@Composable
fun TinyRfMeter(level: Double?, modifier: Modifier = Modifier) {
    val progress = level?.let { ((it + 90.0) / 90.0).coerceIn(0.0, 1.0).toFloat() } ?: 0f
    Box(modifier.height(ShureDimens.TinyMeterHeight).background(RfTrack, RoundedCornerShape(8.dp))) {
        Box(Modifier.fillMaxWidth(progress).height(ShureDimens.TinyMeterHeight).background(MeterGreen, RoundedCornerShape(8.dp)))
    }
}

fun formatWorkbenchFrequency(frequency: Double): String {
    val digits = frequency.toLong().toString()
    if (digits.length <= 3) return "$digits MHz"
    return "${digits.dropLast(3)}.${digits.takeLast(3)} MHz"
}
