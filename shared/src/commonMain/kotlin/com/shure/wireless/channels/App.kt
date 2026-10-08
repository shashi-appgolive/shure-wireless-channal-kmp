package com.shure.wireless.channels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shure.wireless.channels.devices.presentation.DeviceViewModel
import com.shure.wireless.channels.di.defaultGraphQlBaseUrl
import com.shure.wireless.channels.ui.theme.ShureColors
import com.shure.wireless.channels.ui.workbench.WorkbenchActions
import com.shure.wireless.channels.ui.workbench.WorkbenchBackground
import com.shure.wireless.channels.ui.workbench.WorkbenchError
import com.shure.wireless.channels.ui.workbench.WorkbenchGreen
import com.shure.wireless.channels.ui.workbench.WorkbenchMutedText
import com.shure.wireless.channels.ui.workbench.WorkbenchScreen
import com.shure.wireless.channels.ui.workbench.WorkbenchSurface
import org.koin.compose.viewmodel.koinViewModel

private val ConsoleBackground = WorkbenchBackground
private val ConsoleSurface = WorkbenchSurface
private val ConsoleBorder = ShureColors.Border
private val ShureGreen = WorkbenchGreen
private val MutedText = WorkbenchMutedText
private val ErrorRed = WorkbenchError
private val EventBlue = ShureColors.Event

@Composable
fun App(viewModel: DeviceViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WorkbenchScreen(
        state = uiState,
        actions = WorkbenchActions(
            discover = viewModel::discoverDevices,
            startAudioListening = viewModel::startListening,
            stopAudioListening = viewModel::stopListening,
            startRfListening = viewModel::startRfListening,
            stopRfListening = viewModel::stopRfListening,
            updateDeviceName = { id, name -> viewModel.updateDeviceName(defaultGraphQlBaseUrl(), id, name) },
            updateAudioGain = { id, gain -> viewModel.updateAudioChannelGain(defaultGraphQlBaseUrl(), id, gain) },
            meterProgress = viewModel::audioMeterProgress,
            clearError = viewModel::clearError,
        ),
    )
}
