package com.shure.wireless.channels.devices.data

import com.shure.wireless.channels.core.database.dao.StoredDeviceDao
import com.shure.wireless.channels.core.database.entity.StoredDeviceEntity
import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.StoredDeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomStoredDeviceRepository(
    private val dao: StoredDeviceDao,
) : StoredDeviceRepository {

    override fun observeAll(): Flow<List<StoredDevice>> =
        dao.observeAll().map { entities ->
            Logger.d(TAG, "Room emitted ${entities.size} persisted devices")
            entities.map(StoredDeviceEntity::toDomain)
        }

    override suspend fun save(device: StoredDevice) {
        Logger.d(TAG, "Saving ${device.id} through StoredDeviceDao")
        dao.upsert(device.toEntity())
    }

    override suspend fun saveAll(devices: List<StoredDevice>) {
        Logger.d(TAG, "Saving ${devices.size} devices through StoredDeviceDao")
        dao.upsertAll(devices.map { it.toEntity() })
    }

    private companion object {
        const val TAG = "RoomDeviceRepository"
    }
}

private fun StoredDeviceEntity.toDomain(): StoredDevice = StoredDevice(
    id = id,
    name = name,
    model = model,
    ipAddress = ipAddress,
    firmwareVersion = firmwareVersion,
    lastSeenAtEpochMillis = lastSeenAtEpochMillis,
)

private fun StoredDevice.toEntity(): StoredDeviceEntity = StoredDeviceEntity(
    id = id,
    name = name,
    model = model,
    ipAddress = ipAddress,
    firmwareVersion = firmwareVersion,
    lastSeenAtEpochMillis = lastSeenAtEpochMillis,
)
