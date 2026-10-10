package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.shure.wireless.channels.AudioOverload
import com.shure.wireless.channels.RfOverload
import com.shure.wireless.channels.isAudioMeterFull
import com.shure.wireless.channels.isRfMeterFull
import com.shure.wireless.channels.rfSignalProgress
import com.shure.wireless.channels.ui.theme.ShureDimens

private val MeterTrack = Color(0xFF252525)
private val RfTrack = Color(0xFF303030)
private val MeterGreen = Color(0xFF9CFF00)

@Composable
fun AudioMeter(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "audio-meter-progress",
    )
    val overloaded = isAudioMeterFull(progress)
    val shape = RoundedCornerShape(8.dp)
    val trackHeight = ShureDimens.MeterHeight + 6.dp
    BoxWithConstraints(
        Modifier.fillMaxWidth().height(trackHeight).background(MeterTrack, shape)
            .then(if (overloaded) Modifier.border(1.dp, AudioOverload, shape) else Modifier),
    ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        Box(
            Modifier.fillMaxWidth(animatedProgress).height(trackHeight)
                .padding(horizontal = if (overloaded) 3.dp else 0.dp, vertical = 3.dp).background(
                Brush.horizontalGradient(
                    colors = if (overloaded) listOf(AudioOverload, AudioOverload)
                    else listOf(MeterGreen, Color(0xFFFFD54F), Color(0xFFE85D5D)),
                    startX = 0f,
                    endX = widthPx.coerceAtLeast(1f),
                ),
                shape,
            ),
        )
    }
}

@Composable
fun TinyRfMeter(level: Double?, modifier: Modifier = Modifier, showBorder: Boolean = true) {
    val progress = rfSignalProgress(level)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "rf-meter-progress",
    )
    val overloaded = isRfMeterFull(level)
    val shape = RoundedCornerShape(8.dp)
    val overloadShape = RoundedCornerShape(4.dp)
    val trackHeight = ShureDimens.TinyMeterHeight + 6.dp
    Box(modifier.height(trackHeight).background(RfTrack, shape)
        .then(if (overloaded && showBorder) Modifier.border(1.dp, RfOverload, overloadShape) else Modifier)) {
        Box(Modifier.fillMaxWidth(animatedProgress).height(trackHeight)
            .padding(horizontal = if (overloaded) 3.dp else 0.dp, vertical = 3.dp)
            .background(if (overloaded) RfOverload else MeterGreen, shape))
    }
}

fun formatWorkbenchFrequency(frequency: Double): String {
    val digits = frequency.toLong().toString()
    if (digits.length <= 3) return "$digits MHz"
    return "${digits.dropLast(3)}.${digits.takeLast(3)} MHz"
}
