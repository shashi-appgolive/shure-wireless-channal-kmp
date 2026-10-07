package com.shure.wireless.channels.devices.domain.model

data class DeviceEvent(
    val deviceId: String,
    val type: DeviceEventType,
    val message: String,
)

enum class DeviceEventType {
    CONNECTED,
    SIGNAL_CHANGED,
    BATTERY_CHANGED,
    FREQUENCY_CHANGED,
}
