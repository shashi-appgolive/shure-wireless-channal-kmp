package com.shure.wireless.channels.devices.domain.usecase

import com.shure.wireless.channels.core.common.NoParams
import com.shure.wireless.channels.core.common.ObservableUseCase
import com.shure.wireless.channels.devices.domain.model.DeviceEvent
import com.shure.wireless.channels.devices.domain.repository.DeviceOperationsRepository
import kotlinx.coroutines.flow.Flow

class ListenDeviceEventsUseCase(
    private val repository: DeviceOperationsRepository,
) : ObservableUseCase<NoParams, DeviceEvent>() {
    override fun observe(params: NoParams): Flow<DeviceEvent> = repository.listenEvents()

    override val tag: String = "ListenDeviceEventsUseCase"
}
