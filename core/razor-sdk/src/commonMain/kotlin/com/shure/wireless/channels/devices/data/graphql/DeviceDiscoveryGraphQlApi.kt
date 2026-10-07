package com.shure.wireless.channels.devices.data.graphql

import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.core.network.ApiResult
import com.shure.wireless.channels.core.network.GraphQlErrorSummary
import com.shure.wireless.channels.core.network.NetworkException
import com.shure.wireless.channels.core.network.graphql.GraphQlClient

class DeviceDiscoveryGraphQlApi(
    private val graphQlClient: GraphQlClient,
) {
    internal suspend fun discoverDevices(endpointOverride: String? = null): ApiResult<DiscoverDevicesData> {
        val endpoint = endpointOverride ?: graphQlClient.endpoint
        Logger.d(TAG, "Sending DiscoverDevicesQuery to $endpoint")
        val result = graphQlClient.executeData<DiscoverDevicesData>(
            query = DiscoverDevicesQuery,
            operationName = DiscoverDevicesOperationName,
            endpoint = endpoint,
        )
        when (result) {
            is ApiResult.Success -> Logger.d(TAG, "Received DiscoverDevicesQuery response: ${result.data}")
            is ApiResult.Error -> Logger.e(TAG, "DiscoverDevicesQuery failed: ${result.exception.message}")
        }
        return result
    }

    internal suspend fun getDeviceModels(endpointOverride: String? = null): ApiResult<List<String>> {
        val endpoint = endpointOverride ?: graphQlClient.endpoint
        Logger.d(TAG, "Sending DeviceModelsQuery to $endpoint")
        return when (val result = graphQlClient.executeData<DeviceModelsData>(
            query = DeviceModelsQuery,
            operationName = DeviceModelsOperationName,
            endpoint = endpoint,
        )) {
            is ApiResult.Success -> {
                Logger.i(TAG, "DeviceModelsQuery returned ${result.data.deviceModels.size} models")
                ApiResult.Success(result.data.deviceModels, result.statusCode)
            }
            is ApiResult.Error -> {
                Logger.e(TAG, "DeviceModelsQuery failed: ${result.exception.message}")
                result
            }
        }
    }

    internal suspend fun discoverDevicesConnection(endpointOverride: String? = null): ApiResult<com.shure.wireless.channels.devices.domain.model.DiscoveredDevicesConnection> {
        val endpoint = endpointOverride ?: graphQlClient.endpoint
        Logger.d(TAG, "Sending DiscoverDevicesConnection query to $endpoint")
        return when (val result = graphQlClient.executeData<ConnectionData>(DiscoverDevicesConnectionQuery, DiscoverDevicesConnectionOperationName, endpoint = endpoint)) {
            is ApiResult.Success -> ApiResult.Success(result.data.toModel(), result.statusCode)
            is ApiResult.Error -> result
        }
    }

    internal suspend fun updateName(deviceId: String, name: String, endpointOverride: String? = null): ApiResult<UpdatedDevice> {
        val endpoint = endpointOverride ?: graphQlClient.endpoint
        return when (val result = graphQlClient.updateDeviceName(deviceId, name, endpoint)) {
            is ApiResult.Success -> {
                val operation = result.data.updateNodes.firstOrNull()
                    ?: return ApiResult.Error(NetworkException.GraphQl(listOf(GraphQlErrorSummary("Device update returned no operation result"))))
                operation.error?.let { error ->
                    return ApiResult.Error(NetworkException.GraphQl(listOf(GraphQlErrorSummary("${error.code ?: "DEVICE_UPDATE_FAILED"}: ${error.message ?: "Device update failed"}"))))
                }
                val device = operation.device
                    ?: return ApiResult.Error(NetworkException.GraphQl(listOf(GraphQlErrorSummary("Device update returned no device"))))
                ApiResult.Success(device, result.statusCode)
            }
            is ApiResult.Error -> result
        }
    }

    internal suspend fun updateAudioChannelGain(channelId: String, gain: Double, endpointOverride: String? = null): ApiResult<Double> {
        val endpoint = endpointOverride ?: graphQlClient.endpoint
        return when (val result = graphQlClient.updateAudioChannelGain(channelId, gain, endpoint)) {
            is ApiResult.Error -> result
            is ApiResult.Success -> {
                val operation = result.data.updateNodes.firstOrNull()
                    ?: return ApiResult.Error(NetworkException.GraphQl(listOf(GraphQlErrorSummary("Audio channel update returned no result"))))
                operation.error?.let { error -> return ApiResult.Error(NetworkException.GraphQl(listOf(GraphQlErrorSummary(error.message ?: "Audio channel update failed")))) }
                ApiResult.Success(operation.audioChannel?.features?.gain?.gain ?: gain, result.statusCode)
            }
        }
    }

    private companion object {
        const val TAG = "DeviceDiscoveryGraphQlApi"
    }
}
