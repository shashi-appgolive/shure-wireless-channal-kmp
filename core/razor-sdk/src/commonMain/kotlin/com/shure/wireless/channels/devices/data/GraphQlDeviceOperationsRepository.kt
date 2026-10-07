package com.shure.wireless.channels.devices.data

import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.core.network.ApiResult
import com.shure.wireless.channels.devices.data.graphql.DeviceDiscoveryGraphQlApi
import com.shure.wireless.channels.devices.data.graphql.toStoredDevices
import com.shure.wireless.channels.devices.domain.model.DeviceEvent
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.DeviceOperationsRepository
import kotlinx.coroutines.flow.Flow

class GraphQlDeviceOperationsRepository(
    private val discoveryApi: DeviceDiscoveryGraphQlApi,
    private val fallbackEventsRepository: DeviceOperationsRepository = DemoDeviceOperationsRepository(),
) : DeviceOperationsRepository {

    override suspend fun connect(address: String): StoredDevice {
        val endpoint = address.toGraphQlEndpoint()
        Logger.d(TAG, "Connect requested for $address; executing GraphQL discovery path at $endpoint")
        val devices = executeDiscoverDevices(endpoint)
        return devices.firstOrNull()
            ?: error("No devices returned from GraphQL discovery")
    }

    override suspend fun discoverDevices(address: String): List<StoredDevice> {
        val endpoint = address.toGraphQlEndpoint()
        Logger.d(TAG, "Executing GraphQL device discovery at $endpoint")
        return executeDiscoverDevices(endpoint)
    }

    private suspend fun executeDiscoverDevices(endpointOverride: String? = null): List<StoredDevice> {
        return when (val result = discoveryApi.discoverDevices(endpointOverride)) {
            is ApiResult.Success -> {
                Logger.d(TAG, "GraphQL discovery response received: ${result.data}")
                val devices = result.data.toStoredDevices()
                devices.forEach { device ->
                    Logger.d(
                        TAG,
                        "Mapped device id=${device.id}, hardwareId=${device.hardwareId}, status=${device.status}, model=${device.model}, category=${device.category}",
                    )
                }
                Logger.i(TAG, "GraphQL discovery returned ${devices.size} devices")
                devices
            }

            is ApiResult.Error -> {
                Logger.e(TAG, "GraphQL discovery failed: ${result.exception.message}")
                throw result.exception
            }
        }
    }

    override fun listenEvents(): Flow<DeviceEvent> =
        fallbackEventsRepository.listenEvents()

    private fun String.toGraphQlEndpoint(): String {
        val value = trim().trimEnd('/')
        return when {
            value.startsWith("http://", ignoreCase = true) -> value
            value.startsWith("https://", ignoreCase = true) -> value
            else -> "http://$value"
        }
    }

    private companion object {
        const val TAG = "GraphQlDeviceOperationsRepo"
    }
}
