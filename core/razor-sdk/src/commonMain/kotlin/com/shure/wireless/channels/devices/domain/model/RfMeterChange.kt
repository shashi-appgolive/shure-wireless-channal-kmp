package com.shure.wireless.channels.devices.domain.model

data class RfMeterChange(
    val id: String,
    val antennas: List<RfAntennaLevel> = emptyList(),
)

data class RfAntennaLevel(
    val antenna: String,
    val level: Double? = null,
)
