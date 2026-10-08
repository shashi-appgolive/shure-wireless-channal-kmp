package com.shure.wireless.channels.ui.workbench

data class DeviceDetailsActions(
    val startAudioListening: (String) -> Unit,
    val stopAudioListening: (String) -> Unit,
    val startRfListening: (String) -> Unit,
    val stopRfListening: (String) -> Unit,
    val updateDeviceName: (String, String) -> Unit,
    val updateAudioGain: (String, Int) -> Unit,
    val refresh: () -> Unit,
    val back: () -> Unit,
)
