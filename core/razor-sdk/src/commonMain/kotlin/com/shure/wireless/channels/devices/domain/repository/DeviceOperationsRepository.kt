package com.shure.wireless.channels.devices.domain.repository

import com.shure.wireless.channels.devices.domain.model.DeviceEvent
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevicesConnection
import kotlinx.coroutines.flow.Flow

interface DeviceOperationsRepository {
    suspend fun connect(address: String): StoredDevice

    suspend fun discoverDevices(address: String): List<StoredDevice>

    suspend fun getDeviceModels(address: String): List<String>

    suspend fun updateName(address: String, deviceId: String, name: String): StoredDevice
    suspend fun updateAudioChannelGain(address: String, channelId: String, gain: Double): Double

    suspend fun discoverDevicesConnection(address: String): DiscoveredDevicesConnection

    fun listenEvents(): Flow<DeviceEvent>
}
