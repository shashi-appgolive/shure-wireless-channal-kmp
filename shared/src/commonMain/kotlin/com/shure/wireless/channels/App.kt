package com.shure.wireless.channels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shure.wireless.channels.devices.presentation.DeviceViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(viewModel: DeviceViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.isSplashVisible) {
        SplashScreen()
        return
    }
    InventoryScreen(
        state = uiState,
        onDiscover = viewModel::refreshInventory,
        onEndpointChange = viewModel::setInventoryEndpoint,
        onSearchChange = viewModel::setInventorySearch,
        onToggleOnlineFilter = viewModel::toggleInventoryOnlineFilter,
        onToggleSortOrder = viewModel::toggleInventorySortOrder,
        onSelectChannel = viewModel::selectInventoryChannel,
        onCloseChannel = viewModel::closeInventoryChannel,
        onUpdateDeviceName = { id, name -> viewModel.updateDeviceName(uiState.inventory.endpoint, id, name) },
        onUpdateAudioGain = { id, gain -> viewModel.updateAudioChannelGain(uiState.inventory.endpoint, id, gain) },
        meterProgress = viewModel::audioMeterProgress,
        onClearError = viewModel::clearError,
    )
}
