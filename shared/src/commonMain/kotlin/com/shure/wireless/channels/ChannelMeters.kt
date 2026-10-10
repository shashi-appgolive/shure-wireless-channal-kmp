package com.shure.wireless.channels

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.ui.theme.ShureColors

internal fun formatWorkbenchFrequency(frequency: Double): String {
    val digits = frequency.toLong().toString()
    if (digits.length <= 3) return "$digits MHz"
    return "${digits.dropLast(3)}.${digits.takeLast(3)} MHz"
}

internal fun rfSignalProgress(level: Double?): Float =
    level?.let { ((it + 90.0) / 90.0).coerceIn(0.0, 1.0).toFloat() } ?: 0f

internal val RfOverload = Color(0xFFFF4D4F)
internal val AudioOverload = Color(0xFFFF4D4F)

internal fun isAudioMeterFull(progress: Float): Boolean = progress >= 0.9f
internal fun isRfMeterFull(level: Double?): Boolean = level != null && rfSignalProgress(level) >= 0.9f

internal fun inventoryRfMeterColor(level: Double?): Color =
    if (isRfMeterFull(level)) RfOverload else Color.White

@Composable
internal fun smoothMeterProgress(progress: Float): Float {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "meter-progress",
    )
    return animatedProgress
}

@Composable
internal fun TinyRfMeter(level: Double?, modifier: Modifier = Modifier, height: Dp = 4.dp, showBorder: Boolean = true) {
    val progress = smoothMeterProgress(rfSignalProgress(level))
    val overloaded = isRfMeterFull(level)
    val shape = RoundedCornerShape(8.dp)
    val overloadShape = RoundedCornerShape(4.dp)
    val trackHeight = height + 6.dp
    Box(
        modifier.height(trackHeight).background(Color(0xFF262626), shape)
            .then(if (overloaded && showBorder) Modifier.border(1.dp, RfOverload, overloadShape) else Modifier),
    ) {
        Box(
            Modifier.fillMaxWidth(progress).height(trackHeight)
                .padding(horizontal = if (overloaded) 3.dp else 0.dp, vertical = 3.dp)
                .background(inventoryRfMeterColor(level), shape),
        )
    }
}

@Composable
internal fun RfMeterPair(meter: RfMeterChange?, modifier: Modifier = Modifier.width(104.dp)) {
    val antennaA = meter?.antennas?.firstOrNull { it.antenna.endsWith("A") }?.level
    val antennaB = meter?.antennas?.firstOrNull { it.antenna.endsWith("B") }?.level
    val overloaded = isRfMeterFull(antennaA) || isRfMeterFull(antennaB)
    Column(
        modifier.then(if (overloaded) Modifier.border(1.dp, RfOverload, RoundedCornerShape(4.dp)) else Modifier)
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TinyRfMeter(antennaA, Modifier.fillMaxWidth(), height = 4.dp, showBorder = false)
        TinyRfMeter(antennaB, Modifier.fillMaxWidth(), height = 4.dp, showBorder = false)
    }
}

@Composable
internal fun AudioMeter(progress: Float, modifier: Modifier = Modifier, height: Dp = 5.dp) {
    val animatedProgress = smoothMeterProgress(progress)
    val overloaded = isAudioMeterFull(progress)
    val shape = RoundedCornerShape(8.dp)
    val trackHeight = height + 6.dp
    BoxWithConstraints(
        modifier.fillMaxWidth().height(trackHeight).background(Color(0xFF252525), shape)
            .then(if (overloaded) Modifier.border(1.dp, AudioOverload, shape) else Modifier),
    ) {
        val meterWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        Box(
            Modifier.fillMaxWidth(animatedProgress).height(trackHeight)
                .padding(horizontal = if (overloaded) 3.dp else 0.dp, vertical = 3.dp).background(
                Brush.horizontalGradient(
                    colors = if (overloaded) listOf(AudioOverload, AudioOverload)
                    else listOf(ShureColors.Green, Color(0xFFFFD54F), Color(0xFFE85D5D)),
                    startX = 0f,
                    endX = meterWidthPx.coerceAtLeast(1f),
                ),
                shape,
            ),
        )
    }
}
