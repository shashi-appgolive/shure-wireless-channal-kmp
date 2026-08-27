package com.shure.wireless.channels.devices.domain.repository

import com.shure.wireless.channels.devices.domain.model.StoredDevice
import kotlinx.coroutines.flow.Flow

interface StoredDeviceRepository {
    fun observeAll(): Flow<List<StoredDevice>>

    suspend fun save(device: StoredDevice)

    suspend fun saveAll(devices: List<StoredDevice>)
}
