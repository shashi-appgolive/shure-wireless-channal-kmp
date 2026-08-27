package com.shure.wireless.channels.devices.domain.usecase

import com.shure.wireless.channels.core.common.Completed
import com.shure.wireless.channels.core.common.FlowUseCase3
import com.shure.wireless.channels.core.common.success
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.StoredDeviceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SaveStoredDeviceUseCase(
    private val repository: StoredDeviceRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : FlowUseCase3<Unit, StoredDevice>(dispatcher) {

    override suspend fun executeInternal(params: StoredDevice): Completed<Unit> {
        repository.save(params)
        return success(Unit)
    }
}
