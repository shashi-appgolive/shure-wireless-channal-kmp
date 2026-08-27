package com.shure.wireless.channels.devices.domain.usecase

import com.shure.wireless.channels.core.common.Completed
import com.shure.wireless.channels.core.common.FlowUseCase2
import com.shure.wireless.channels.core.common.success
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.StoredDeviceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

class GetStoredDevicesUseCase(
    private val repository: StoredDeviceRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : FlowUseCase2<List<StoredDevice>>(dispatcher) {

    override suspend fun executeInternal(): Completed<List<StoredDevice>> =
        success(repository.observeAll().first())
}
