package com.shure.wireless.channels.core.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "stored_devices")
data class StoredDeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val model: String? = null,
    val ipAddress: String? = null,
    val firmwareVersion: String? = null,
    val lastSeenAtEpochMillis: Long,
)
