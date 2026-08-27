package com.shure.wireless.channels.devices.domain.usecase

import com.shure.wireless.channels.core.common.NoParams
import com.shure.wireless.channels.core.common.ObservableUseCase
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.StoredDeviceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class ObserveStoredDevicesUseCase(
    private val repository: StoredDeviceRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ObservableUseCase<NoParams, List<StoredDevice>>(dispatcher) {

    override fun observe(params: NoParams): Flow<List<StoredDevice>> = repository.observeAll()

    override val tag: String = "ObserveStoredDevices"
}
