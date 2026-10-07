package com.shure.wireless.channels.devices.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shure.wireless.channels.core.common.AppLogStore
import com.shure.wireless.channels.core.common.AsyncOperation
import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.core.common.NoParams
import com.shure.wireless.channels.core.common.onFailure
import com.shure.wireless.channels.core.common.onSuccess
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.usecase.ConnectDeviceUseCase
import com.shure.wireless.channels.devices.domain.usecase.DiscoverDevicesUseCase
import com.shure.wireless.channels.devices.domain.usecase.GetStoredDevicesUseCase
import com.shure.wireless.channels.devices.domain.usecase.ObserveStoredDevicesUseCase
import com.shure.wireless.channels.devices.domain.usecase.ListenDeviceEventsUseCase
import com.shure.wireless.channels.devices.domain.usecase.SaveStoredDeviceUseCase
import com.shure.wireless.channels.devices.domain.usecase.SaveStoredDevicesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DeviceViewModel(
    private val getStoredDevicesUseCase: GetStoredDevicesUseCase,
    private val observeStoredDevicesUseCase: ObserveStoredDevicesUseCase,
    private val saveStoredDeviceUseCase: SaveStoredDeviceUseCase,
    private val saveStoredDevicesUseCase: SaveStoredDevicesUseCase,
    private val connectDeviceUseCase: ConnectDeviceUseCase,
    private val discoverDevicesUseCase: DiscoverDevicesUseCase,
    private val listenDeviceEventsUseCase: ListenDeviceEventsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceUiState(isLoading = true))
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var saveJob: Job? = null
    private var connectJob: Job? = null
    private var discoveryJob: Job? = null
    private var eventsJob: Job? = null

    init {
        Logger.d(TAG, "DeviceViewModel initialized")
        observeLogs()
        observeDevices()
    }

    private fun observeLogs() {
        viewModelScope.launch {
            AppLogStore.entries.collect { entries ->
                _uiState.update { it.copy(logs = entries) }
            }
        }
    }

    fun connect(address: String) {
        val endpoint = address.trim()
        if (endpoint.isBlank()) {
            Logger.w(TAG, "Connect blocked because endpoint is empty")
            _uiState.update { it.copy(errorMessage = "GraphQL endpoint cannot be empty.") }
            return
        }

        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            Logger.d(TAG, "Connect requested for $endpoint")
            connectDeviceUseCase.execute(CONNECT_DEVICE_USE_CASE, endpoint).collect { operation ->
                if (operation.isLoading) {
                    _uiState.update { it.copy(isConnecting = true, errorMessage = null) }
                }
                operation.onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            errorMessage = failure.message ?: DEFAULT_ERROR_MESSAGE,
                        )
                    }
                }
                operation.data?.let { device ->
                    Logger.d(TAG, "Connection result received; forwarding device to save use case")
                    _uiState.update { it.copy(connectedDevice = device, isConnecting = false) }
                    persistDevices(listOf(device))
                }
            }
        }
    }

    fun discoverDevices(address: String) {
        val endpoint = address.trim()
        if (endpoint.isBlank()) {
            Logger.w(TAG, "Discovery blocked because endpoint is empty")
            _uiState.update { it.copy(errorMessage = "GraphQL endpoint cannot be empty.") }
            return
        }

        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            Logger.d(TAG, "Device discovery requested for $endpoint")
            discoverDevicesUseCase.execute(DISCOVER_DEVICES_USE_CASE, endpoint).collect { operation ->
                if (operation.isLoading) {
                    _uiState.update { it.copy(isDiscovering = true, errorMessage = null) }
                }
                operation.onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isDiscovering = false,
                            errorMessage = failure.message ?: DEFAULT_ERROR_MESSAGE,
                        )
                    }
                }
                operation.data?.let { devices ->
                    Logger.d(TAG, "Discovery result received; forwarding devices to save use case")
                    _uiState.update { it.copy(isDiscovering = false) }
                    persistDevices(devices)
                }
            }
        }
    }

    fun toggleEventListening() {
        if (eventsJob?.isActive == true) {
            eventsJob?.cancel()
            eventsJob = null
            _uiState.update { it.copy(isListening = false) }
            Logger.i(TAG, "Stopped listening for device events")
            return
        }

        eventsJob = viewModelScope.launch {
            Logger.d(TAG, "Subscribing to device events")
            listenDeviceEventsUseCase(NoParams).collect { operation ->
                if (operation.isLoading) {
                    _uiState.update { it.copy(isListening = true, errorMessage = null) }
                }
                operation.onFailure { failure ->
                    _uiState.update {
                        it.copy(
                            isListening = false,
                            errorMessage = failure.message ?: DEFAULT_ERROR_MESSAGE,
                        )
                    }
                }
                operation.data?.let { event ->
                    Logger.i(EVENT_TAG, "${event.deviceId}: ${event.message}")
                    _uiState.update { state ->
                        state.copy(events = (state.events + event).takeLast(MAX_EVENTS))
                    }
                }
            }
        }
    }

    fun clearLogs() {
        AppLogStore.clear()
        Logger.i(TAG, "Log console cleared")
    }

    /**
     * Subscribes to Room changes for the lifetime of this ViewModel.
     */
    private fun observeDevices() {
        viewModelScope.launch {
            observeStoredDevicesUseCase(NoParams).collect { operation ->
                operation.updateDeviceListState()
            }
        }
    }

    /**
     * Performs a one-shot read using the Convo-style [GetStoredDevicesUseCase].
     */
    fun refreshDevices() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            getStoredDevicesUseCase.execute(GET_DEVICES_USE_CASE).collect { operation ->
                operation.updateDeviceListState()
            }
        }
    }

    /**
     * Saves a discovered device using the parameterized Convo-style use case.
     * The Room observation updates [uiState] with the saved device afterwards.
     */
    fun saveDevice(device: StoredDevice) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            saveStoredDeviceUseCase.execute(SAVE_DEVICE_USE_CASE, device).collect { operation ->
                if (operation.isLoading) {
                    _uiState.update { it.copy(isSaving = true, errorMessage = null) }
                }

                operation
                    .onSuccess {
                        Logger.i(TAG, "Saved device ${device.id}")
                        _uiState.update { it.copy(isSaving = false, errorMessage = null) }
                    }
                    .onFailure { failure ->
                        Logger.e(TAG, "Unable to save device ${device.id}: ${failure.message}")
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = failure.message ?: DEFAULT_ERROR_MESSAGE,
                            )
                        }
                    }
            }
        }
    }

    private suspend fun persistDevices(devices: List<StoredDevice>) {
        if (devices.isEmpty()) return
        Logger.d(TAG, "Calling SaveStoredDevicesUseCase with ${devices.size} devices")
        saveStoredDevicesUseCase.execute(SAVE_DEVICES_USE_CASE, devices).collect { operation ->
            if (operation.isLoading) {
                _uiState.update { it.copy(isSaving = true) }
            }
            operation.onSuccess {
                Logger.i(TAG, "Database save completed; waiting for Room emission")
                _uiState.update { it.copy(isSaving = false) }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = failure.message ?: DEFAULT_ERROR_MESSAGE,
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun AsyncOperation<List<StoredDevice>>.updateDeviceListState() {
        if (isLoading) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        }

        onSuccess { devices ->
            Logger.d(TAG, "Loaded ${devices.size} stored devices")
            _uiState.update {
                it.copy(
                    devices = devices,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }.onFailure { failure ->
            Logger.e(TAG, "Unable to load devices: ${failure.message}")
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = failure.message ?: DEFAULT_ERROR_MESSAGE,
                )
            }
        }
    }

    private companion object {
        const val TAG = "DeviceViewModel"
        const val GET_DEVICES_USE_CASE = "GetStoredDevicesUseCase"
        const val CONNECT_DEVICE_USE_CASE = "ConnectDeviceUseCase"
        const val DISCOVER_DEVICES_USE_CASE = "DiscoverDevicesUseCase"
        const val SAVE_DEVICE_USE_CASE = "SaveStoredDeviceUseCase"
        const val SAVE_DEVICES_USE_CASE = "SaveStoredDevicesUseCase"
        const val EVENT_TAG = "DeviceEvent"
        const val MAX_EVENTS = 20
        const val DEFAULT_ERROR_MESSAGE = "Something went wrong. Please try again."
    }
}
