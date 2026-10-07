package com.shure.wireless.channels.devices.data.graphql

import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.core.network.ApiResult
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
            is ApiResult.Success -> ApiResult.Success(result.data.deviceModels, result.statusCode)
            is ApiResult.Error -> result
        }
    }

    private companion object {
        const val TAG = "DeviceDiscoveryGraphQlApi"
    }
}
