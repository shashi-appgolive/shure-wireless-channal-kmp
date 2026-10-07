package com.shure.wireless.channels.devices.domain.model

data class StoredDevice(
    val id: String,
    val name: String,
    val model: String? = null,
    val hardwareId: String? = null,
    val status: String? = null,
    val category: String? = null,
    val ipAddress: String? = null,
    val firmwareVersion: String? = null,
    val lastSeenAtEpochMillis: Long,
)
