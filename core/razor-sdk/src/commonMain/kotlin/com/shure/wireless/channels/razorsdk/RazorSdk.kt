package com.shure.wireless.channels.razorsdk

import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevicesConnection
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.MeterChangeType
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.devices.data.graphql.AudioMeterSubscription
import com.shure.wireless.channels.devices.data.graphql.RfMeterSubscription
import com.shure.wireless.channels.core.network.websocket.WebSocketClient
import kotlinx.coroutines.flow.Flow
import com.shure.wireless.channels.devices.domain.repository.DeviceOperationsRepository

/** Stable public entry point for the initial Razor SDK surface. */
interface RazorSdk {
    val devices: DeviceApi
    val audioMeters: AudioMeterApi
    val rfMeters: RfMeterApi
}

interface AudioMeterApi {
    fun observe(channelId: String, types: List<MeterChangeType> = listOf(MeterChangeType.AUDIO_CHANNEL_CLIP, MeterChangeType.AUDIO_CHANNEL_PEAK, MeterChangeType.AUDIO_CHANNEL_RMS), updateRate: String = "PT0.1S"): Flow<AudioMeterChange>
}

interface RfMeterApi {
    fun observe(channelId: String, types: List<MeterChangeType> = listOf(MeterChangeType.RF_CHANNEL_SIGNAL_STRENGTH), updateRate: String = "PT0.1S"): Flow<RfMeterChange>
}

/** Device APIs currently supported by the SDK. */
interface DeviceApi {
    suspend fun updateName(deviceId: String, name: String, address: String): SdkResult<StoredDevice>
    suspend fun updateAudioChannelGain(channelId: String, gain: Double, address: String): SdkResult<Double>

    suspend fun getDeviceModels(address: String): List<String>

    suspend fun discoverConnections(address: String): DiscoveredDevicesConnection
}

/** Default implementation backed by the SDK's device operations repository. */
class DefaultRazorSdk(
    repository: DeviceOperationsRepository,
    webSocketClient: WebSocketClient,
) : RazorSdk {
    override val devices: DeviceApi = DefaultDeviceApi(repository)
    override val audioMeters: AudioMeterApi = object : AudioMeterApi {
        private val subscription = AudioMeterSubscription(webSocketClient)

        override fun observe(
            channelId: String,
            types: List<MeterChangeType>,
            updateRate: String,
        ): Flow<AudioMeterChange> = subscription.observe(channelId, types, updateRate)
    }
    override val rfMeters: RfMeterApi = object : RfMeterApi {
        private val subscription = RfMeterSubscription(webSocketClient)

        override fun observe(channelId: String, types: List<MeterChangeType>, updateRate: String): Flow<RfMeterChange> =
            subscription.observe(channelId, types, updateRate)
    }
}

private class DefaultDeviceApi(
    private val repository: DeviceOperationsRepository,
) : DeviceApi {
    override suspend fun updateName(deviceId: String, name: String, address: String): SdkResult<StoredDevice> {
        if (deviceId.isBlank() || name.isBlank() || address.isBlank()) return SdkResult.Failure(SdkError.InvalidInput)
        return runCatching { SdkResult.Success(repository.updateName(address, deviceId, name.trim())) }
            .getOrElse { SdkResult.Failure(SdkError.Network(it.message ?: "Device update failed")) }
    }

    override suspend fun updateAudioChannelGain(channelId: String, gain: Double, address: String): SdkResult<Double> {
        if (channelId.isBlank() || address.isBlank()) return SdkResult.Failure(SdkError.InvalidInput)
        return runCatching { SdkResult.Success(repository.updateAudioChannelGain(address, channelId, gain)) }
            .getOrElse { SdkResult.Failure(SdkError.Network(it.message ?: "Audio channel update failed")) }
    }

    override suspend fun getDeviceModels(address: String): List<String> =
        runCatching {
            Logger.d(TAG, "SDK getDeviceModels started")
            repository.getDeviceModels(address)
        }.onSuccess {
            Logger.i(TAG, "SDK getDeviceModels completed: ${it.size} models")
        }.onFailure {
            Logger.e(TAG, "SDK getDeviceModels failed: ${it.message}")
        }.getOrThrow()

    override suspend fun discoverConnections(address: String): DiscoveredDevicesConnection =
        runCatching {
            Logger.d(TAG, "SDK discoverConnections started")
            repository.discoverDevicesConnection(address)
        }.onSuccess {
            Logger.i(TAG, "SDK discoverConnections completed: ${it.devices.size} devices")
        }.onFailure {
            Logger.e(TAG, "SDK discoverConnections failed: ${it.message}")
        }.getOrThrow()

    private companion object {
        const val TAG = "RazorSdk"
    }
}
