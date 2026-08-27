package com.shure.wireless.channels.devices.data

import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.StoredDeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Process-memory-only implementation used where a real persistence layer
 * (Room + a platform SQLiteDriver) isn't wired up yet, e.g. the web target,
 * which needs a worker-backed SQLite driver that this project doesn't build
 * a worker script for. Devices survive for the lifetime of the page/process
 * but are not persisted across a reload.
 */
class InMemoryStoredDeviceRepository : StoredDeviceRepository {
    private val _devices = MutableStateFlow<List<StoredDevice>>(emptyList())

    override fun observeAll(): Flow<List<StoredDevice>> = _devices.asStateFlow()

    override suspend fun save(device: StoredDevice) {
        Logger.d(TAG, "Saving ${device.id} in-memory (no persistence backend on this target)")
        _devices.update { current -> (current.filterNot { it.id == device.id } + device) }
    }

    override suspend fun saveAll(devices: List<StoredDevice>) {
        Logger.d(TAG, "Saving ${devices.size} devices in-memory (no persistence backend on this target)")
        _devices.update { current ->
            val incomingIds = devices.map { it.id }.toSet()
            current.filterNot { it.id in incomingIds } + devices
        }
    }

    private companion object {
        const val TAG = "InMemoryDeviceRepository"
    }
}
