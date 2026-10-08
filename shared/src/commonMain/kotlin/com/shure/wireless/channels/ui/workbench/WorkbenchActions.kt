package com.shure.wireless.channels.ui.workbench

data class WorkbenchActions(
    val discover: (String) -> Unit,
    val startAudioListening: (String) -> Unit,
    val stopAudioListening: (String) -> Unit,
    val startRfListening: (String) -> Unit,
    val stopRfListening: (String) -> Unit,
    val updateDeviceName: (String, String) -> Unit,
    val updateAudioGain: (String, Int) -> Unit,
    val meterProgress: (Double, String) -> Float,
    val clearError: () -> Unit,
)
