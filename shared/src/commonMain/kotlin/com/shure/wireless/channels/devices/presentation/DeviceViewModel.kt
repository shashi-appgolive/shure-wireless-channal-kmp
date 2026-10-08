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
import com.shure.wireless.channels.razorsdk.RazorSdk
import com.shure.wireless.channels.devices.domain.usecase.GetStoredDevicesUseCase
import com.shure.wireless.channels.devices.domain.usecase.ObserveStoredDevicesUseCase
import com.shure.wireless.channels.devices.domain.usecase.ListenDeviceEventsUseCase
import com.shure.wireless.channels.devices.domain.usecase.SaveStoredDeviceUseCase
import com.shure.wireless.channels.devices.domain.usecase.SaveStoredDevicesUseCase
import com.shure.wireless.channels.di.defaultGraphQlBaseUrl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val razorSdk: RazorSdk,
    private val listenDeviceEventsUseCase: ListenDeviceEventsUseCase,
) : ViewModel() {

    fun audioMeterProgress(rmsValue: Double, deviceModel: String): Float =
        razorSdk.audioMeters.rmsProgress(rmsValue, deviceModel)

    private val _uiState = MutableStateFlow(DeviceUiState(isLoading = true, inventory = InventoryUiState(endpoint = defaultGraphQlBaseUrl())))
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var saveJob: Job? = null
    private var connectJob: Job? = null
    private var discoveryJob: Job? = null
    private var deviceModelsJob: Job? = null
    private var discoveryConnectionsJob: Job? = null
    private val meterJobs = mutableMapOf<String, Job>()
    private val rfMeterJobs = mutableMapOf<String, Job>()
    private var eventsJob: Job? = null

    init {
        Logger.d(TAG, "DeviceViewModel initialized")
        observeLogs()
        observeDevices()
        discoverDevices(_uiState.value.inventory.endpoint)
        viewModelScope.launch {
            delay(SPLASH_DURATION_MILLIS)
            _uiState.update { it.copy(isSplashVisible = false) }
        }
    }

    fun refreshInventory() = discoverDevices(_uiState.value.inventory.endpoint)

    fun setInventoryEndpoint(address: String) {
        val endpoint = address.trim()
        if (endpoint.isBlank()) {
            _uiState.update { it.copy(errorMessage = "GraphQL endpoint cannot be empty.") }
            return
        }
        if (endpoint != _uiState.value.inventory.endpoint) {
            discoveryJob?.cancel()
            stopInventoryMeters()
            _uiState.update {
                it.copy(
                    inventory = it.inventory.copy(endpoint = endpoint, selectedDeviceId = null).withDevices(emptyList()),
                    discoveredConnections = emptyList(),
                    audioMeters = emptyMap(),
                    rfMeters = emptyMap(),
                    errorMessage = null,
                )
            }
        }
        refreshInventory()
    }

    fun setInventorySearch(query: String) {
        _uiState.update { it.copy(inventory = it.inventory.copy(searchQuery = query).withDevices(it.discoveredConnections)) }
    }

    fun toggleInventoryOnlineFilter() {
        _uiState.update { it.copy(inventory = it.inventory.copy(onlineOnly = !it.inventory.onlineOnly).withDevices(it.discoveredConnections)) }
    }

    fun toggleInventorySortOrder() {
        _uiState.update { it.copy(inventory = it.inventory.copy(sortAscending = !it.inventory.sortAscending).withDevices(it.discoveredConnections)) }
    }

    fun selectInventoryChannel(deviceId: String, channelIndex: Int) {
        _uiState.update { it.copy(inventory = it.inventory.copy(selectedDeviceId = deviceId, selectedChannelIndex = channelIndex)) }
    }

    fun closeInventoryChannel() {
        _uiState.update { it.copy(inventory = it.inventory.copy(selectedDeviceId = null)) }
    }

    private fun stopInventoryMeters() {
        meterJobs.keys.toList().forEach(::stopListening)
        rfMeterJobs.keys.toList().forEach(::stopRfListening)
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
            _uiState.update { it.copy(isDiscovering = true, errorMessage = null) }
            runCatching { razorSdk.devices.discoverConnections(endpoint) }
                .onSuccess { devices ->
                    Logger.d(TAG, "Discovery result received: ${devices.devices.size} devices")
                    stopInventoryMeters()
                    _uiState.update { state ->
                        state.copy(
                            discoveredConnections = devices.devices,
                            inventory = state.inventory.copy(
                                selectedDeviceId = state.inventory.selectedDeviceId?.takeIf { id -> devices.devices.any { it.id == id } },
                            ).withDevices(devices.devices),
                            isDiscovering = false,
                            errorMessage = null,
                        )
                    }
                    devices.devices.flatMap { it.features.audioChannels }.map { it.id }.distinct().forEach(::startListening)
                    devices.devices.flatMap { it.features.rfChannels }.map { it.id }.distinct().forEach(::startRfListening)
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update { it.copy(isDiscovering = false, errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE) }
                }
        }
    }

    fun getDeviceModels(address: String) {
        val endpoint = address.trim()
        if (endpoint.isBlank()) {
            _uiState.update { it.copy(errorMessage = "GraphQL endpoint cannot be empty.") }
            return
        }
        deviceModelsJob?.cancel()
        deviceModelsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDeviceModels = true, errorMessage = null) }
            runCatching { razorSdk.devices.getDeviceModels(endpoint) }
                .onSuccess { models -> _uiState.update { it.copy(deviceModels = models, isLoadingDeviceModels = false) } }
                .onFailure { error -> _uiState.update { it.copy(isLoadingDeviceModels = false, errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE) } }
        }
    }

    fun discoverDevicesConnection(address: String) {
        val endpoint = address.trim()
        if (endpoint.isBlank()) {
            _uiState.update { it.copy(errorMessage = "GraphQL endpoint cannot be empty.") }
            return
        }
        discoveryConnectionsJob?.cancel()
        discoveryConnectionsJob = viewModelScope.launch {
            _uiState.update { it.copy(isDiscoveringConnections = true, errorMessage = null) }
            runCatching { razorSdk.devices.discoverConnections(endpoint) }
                .onSuccess { result -> _uiState.update { it.copy(discoveredConnections = result.devices, isDiscoveringConnections = false) } }
                .onFailure { error -> _uiState.update { it.copy(isDiscoveringConnections = false, errorMessage = error.message ?: DEFAULT_ERROR_MESSAGE) } }
        }
    }

    fun startListening(channelId: String) {
        meterJobs[channelId]?.cancel()
        meterJobs[channelId] = viewModelScope.launch {
            _uiState.update { it.copy(listeningChannelIds = it.listeningChannelIds + channelId) }
            runCatching {
                razorSdk.audioMeters.observe(channelId).collect { meter ->
                    _uiState.update { it.copy(audioMeters = it.audioMeters + (channelId to meter)) }
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(listeningChannelIds = it.listeningChannelIds - channelId, errorMessage = exception.message ?: DEFAULT_ERROR_MESSAGE) }
            }
        }
    }

    fun stopListening(channelId: String) {
        meterJobs.remove(channelId)?.cancel()
        _uiState.update { it.copy(listeningChannelIds = it.listeningChannelIds - channelId) }
    }

    fun startRfListening(channelId: String) {
        rfMeterJobs[channelId]?.cancel()
        rfMeterJobs[channelId] = viewModelScope.launch {
            _uiState.update { it.copy(listeningRfChannelIds = it.listeningRfChannelIds + channelId) }
            runCatching {
                razorSdk.rfMeters.observe(channelId).collect { meter ->
                    _uiState.update { it.copy(rfMeters = it.rfMeters + (channelId to meter)) }
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(listeningRfChannelIds = it.listeningRfChannelIds - channelId, errorMessage = exception.message ?: DEFAULT_ERROR_MESSAGE) }
            }
        }
    }

    fun stopRfListening(channelId: String) {
        rfMeterJobs.remove(channelId)?.cancel()
        _uiState.update { it.copy(listeningRfChannelIds = it.listeningRfChannelIds - channelId) }
    }

    fun updateDeviceName(address: String, deviceId: String, name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Device name cannot be empty.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingDeviceName = true, errorMessage = null) }
            when (val result = razorSdk.devices.updateName(deviceId, trimmedName, address)) {
                is com.shure.wireless.channels.razorsdk.SdkResult.Success ->
                    _uiState.update { state ->
                        val updated = state.discoveredConnections.map { device ->
                            if (device.id == deviceId) device.copy(features = device.features.copy(name = trimmedName)) else device
                        }
                        state.copy(
                            isUpdatingDeviceName = false,
                            discoveredConnections = updated,
                            inventory = state.inventory.withDevices(updated),
                        )
                    }
                is com.shure.wireless.channels.razorsdk.SdkResult.Failure ->
                    _uiState.update { it.copy(isUpdatingDeviceName = false, errorMessage = result.error.toString()) }
            }
        }
    }

    fun updateAudioChannelGain(address: String, channelId: String, gain: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingAudioGain = true, errorMessage = null) }
            when (val result = razorSdk.devices.updateAudioChannelGain(channelId, gain.toDouble(), address)) {
                is com.shure.wireless.channels.razorsdk.SdkResult.Success -> {
                    _uiState.update { it.copy(isUpdatingAudioGain = false) }
                }
                is com.shure.wireless.channels.razorsdk.SdkResult.Failure -> {
                    _uiState.update { it.copy(isUpdatingAudioGain = false, errorMessage = result.error.toString()) }
                }
            }
        }
    }

    override fun onCleared() {
        meterJobs.values.forEach(Job::cancel)
        meterJobs.clear()
        rfMeterJobs.values.forEach(Job::cancel)
        rfMeterJobs.clear()
        super.onCleared()
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
        const val SPLASH_DURATION_MILLIS = 1500L
        const val DEFAULT_ERROR_MESSAGE = "Something went wrong. Please try again."
    }
}
