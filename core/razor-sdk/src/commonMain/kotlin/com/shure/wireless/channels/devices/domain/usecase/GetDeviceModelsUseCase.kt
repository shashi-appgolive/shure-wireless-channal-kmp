package com.shure.wireless.channels.devices.domain.usecase

import com.shure.wireless.channels.devices.domain.repository.DeviceOperationsRepository

class GetDeviceModelsUseCase(
    private val repository: DeviceOperationsRepository,
) {
    suspend fun execute(address: String): List<String> =
        repository.getDeviceModels(address.trim())
}
