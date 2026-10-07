package com.shure.wireless.channels.devices.data

import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.devices.domain.model.DeviceEvent
import com.shure.wireless.channels.devices.domain.model.DeviceEventType
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.repository.DeviceOperationsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Deterministic demo implementation. Replace this repository with the real
 * Shure discovery/connection transport without changing the ViewModel or UI.
 */
class DemoDeviceOperationsRepository : DeviceOperationsRepository {
    override suspend fun connect(address: String): StoredDevice {
        require(address.isNotBlank()) { "Device address is required" }
        Logger.d(TAG, "Opening demo connection to $address")
        delay(700)
        return StoredDevice(
            id = "ulxd4-$address",
            name = "ULXD4 Receiver",
            model = "ULXD4Q",
            ipAddress = address,
            firmwareVersion = "2.8.1",
            lastSeenAtEpochMillis = 1L,
        ).also { Logger.i(TAG, "Connected to ${it.name} at $address") }
    }

    override suspend fun discoverDevices(address: String): List<StoredDevice> {
        Logger.d(TAG, "Scanning the demo network at $address")
        delay(1_000)
        return listOf(
            StoredDevice(
                id = "demo-ulxd4-20",
                name = "Stage Receiver A",
                model = "ULXD4Q",
                ipAddress = "192.168.1.20",
                firmwareVersion = "2.8.1",
                lastSeenAtEpochMillis = 2L,
            ),
            StoredDevice(
                id = "demo-axient-21",
                name = "Lead Vocal",
                model = "AD4Q",
                ipAddress = "192.168.1.21",
                firmwareVersion = "1.4.7",
                lastSeenAtEpochMillis = 3L,
            ),
            StoredDevice(
                id = "demo-psm-22",
                name = "Monitor Rack",
                model = "P10T",
                ipAddress = "192.168.1.22",
                firmwareVersion = "1.7.3",
                lastSeenAtEpochMillis = 4L,
            ),
        ).also { Logger.i(TAG, "Discovery returned ${it.size} devices") }
    }

    override fun listenEvents(): Flow<DeviceEvent> = flow {
        val events = listOf(
            DeviceEvent("demo-ulxd4-20", DeviceEventType.SIGNAL_CHANGED, "RF level changed to -48 dBm"),
            DeviceEvent("demo-axient-21", DeviceEventType.BATTERY_CHANGED, "Transmitter battery is 82%"),
            DeviceEvent("demo-psm-22", DeviceEventType.FREQUENCY_CHANGED, "Frequency changed to 518.500 MHz"),
        )
        var index = 0
        Logger.d(TAG, "Demo event stream subscribed")
        while (true) {
            delay(1_500)
            emit(events[index % events.size])
            index += 1
        }
    }

    private companion object {
        const val TAG = "DeviceOperationsRepo"
    }
}
