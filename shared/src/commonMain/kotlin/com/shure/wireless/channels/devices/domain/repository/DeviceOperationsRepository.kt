package com.shure.wireless.channels.devices.domain.repository

import com.shure.wireless.channels.devices.domain.model.DeviceEvent
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import kotlinx.coroutines.flow.Flow

interface DeviceOperationsRepository {
    suspend fun connect(address: String): StoredDevice

    suspend fun discoverDevices(): List<StoredDevice>

    fun listenEvents(): Flow<DeviceEvent>
}
