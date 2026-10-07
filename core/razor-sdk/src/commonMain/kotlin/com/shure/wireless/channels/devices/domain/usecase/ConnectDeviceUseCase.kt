package com.shure.wireless.channels.devices.domain.usecase

import com.shure.wireless.channels.core.common.Completed
import com.shure.wireless.channels.core.common.FlowUseCase3
import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.core.common.success
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.DeviceOperationsRepository

class ConnectDeviceUseCase(
    private val repository: DeviceOperationsRepository,
) : FlowUseCase3<StoredDevice, String>() {
    override suspend fun executeInternal(params: String): Completed<StoredDevice> {
        Logger.d(TAG, "Delegating connection to repository")
        return success(repository.connect(params.trim()))
    }

    private companion object {
        const val TAG = "ConnectDeviceUseCase"
    }
}
