package com.shure.wireless.channels.devices.domain.usecase

import com.shure.wireless.channels.core.common.Completed
import com.shure.wireless.channels.core.common.FlowUseCase3
import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.core.common.success
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.DeviceOperationsRepository

class DiscoverDevicesUseCase(
    private val repository: DeviceOperationsRepository,
) : FlowUseCase3<List<StoredDevice>, String>() {
    override suspend fun executeInternal(params: String): Completed<List<StoredDevice>> {
        Logger.d(TAG, "Delegating discovery to repository")
        return success(repository.discoverDevices(params.trim()))
    }

    private companion object {
        const val TAG = "DiscoverDevicesUseCase"
    }
}
