package com.shure.wireless.channels.ui.components

import com.shure.wireless.channels.devices.domain.model.AudioChannel

internal data class ChannelAudioSettingsUiModel(
    val gain: Int,
    val audioOutput: String?,
    val toneVolume: String?,
    val toneFrequency: Double?,
)

internal fun AudioChannel.toAudioSettingsUiModel() = ChannelAudioSettingsUiModel(
    gain = (features.gain ?: 0.0).toInt(),
    audioOutput = features.inputSource,
    toneVolume = features.toneGain?.let { gain ->
        val value = if (gain % 1.0 == 0.0) gain.toInt().toString() else gain.toString()
        "${if (gain > 0) "+" else ""}$value dB"
    },
    toneFrequency = features.toneFrequency,
)
