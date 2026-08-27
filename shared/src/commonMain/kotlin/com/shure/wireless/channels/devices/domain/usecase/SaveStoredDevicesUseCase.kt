package com.shure.wireless.channels.devices.domain.usecase

import com.shure.wireless.channels.core.common.Completed
import com.shure.wireless.channels.core.common.FlowUseCase3
import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.core.common.success
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.StoredDeviceRepository

class SaveStoredDevicesUseCase(
    private val repository: StoredDeviceRepository,
) : FlowUseCase3<Unit, List<StoredDevice>>() {
    override suspend fun executeInternal(params: List<StoredDevice>): Completed<Unit> {
        Logger.d(TAG, "Persisting ${params.size} devices through repository")
        repository.saveAll(params)
        return success(Unit)
    }

    private companion object {
        const val TAG = "SaveStoredDevicesUseCase"
    }
}
