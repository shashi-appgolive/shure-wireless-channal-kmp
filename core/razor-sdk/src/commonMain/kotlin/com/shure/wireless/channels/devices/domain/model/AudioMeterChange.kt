package com.shure.wireless.channels.devices.domain.model

data class AudioMeterChange(
    val id: String,
    val peakLevel: Double? = null,
    val rmsLevel: Double? = null,
)

enum class MeterChangeType {
    AUDIO_CHANNEL_CLIP,
    AUDIO_CHANNEL_PEAK,
    AUDIO_CHANNEL_RMS,
    RF_CHANNEL_SIGNAL_STRENGTH,
}
