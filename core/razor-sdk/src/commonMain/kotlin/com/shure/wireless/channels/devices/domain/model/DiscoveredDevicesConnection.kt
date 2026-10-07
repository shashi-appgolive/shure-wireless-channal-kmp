package com.shure.wireless.channels.devices.domain.model

data class DiscoveredDevicesConnection(val devices: List<DiscoveredDevice> = emptyList())

data class DiscoveredDevice(
    val id: String,
    val hardwareId: String? = null,
    val status: String? = null,
    val interfaceInfo: DeviceInterfaceInfo? = null,
    val features: DeviceFeatures = DeviceFeatures(),
)

data class DeviceInterfaceInfo(val model: String? = null, val category: String? = null, val type: String? = null)
data class DeviceFeatures(
    val name: String? = null,
    val rfBand: String? = null,
    val firmware: FirmwareFeature? = null,
    val hostedFirmwareVersion: String? = null,
    val serialNumber: String? = null,
    val wirelessEncryption: WirelessEncryptionFeature? = null,
    val quadversityEnabled: Boolean? = null,
    val antennaBiasVoltageEnabled: Boolean? = null,
    val rfTransmissionMode: String? = null,
    val networkSwitchMode: String? = null,
    val controlNetwork: ControlNetworkFeature? = null,
    val danteAudioNetworkName: String? = null,
    val audioChannels: List<AudioChannel> = emptyList(),
    val rfChannels: List<RfChannel> = emptyList(),
)
data class FirmwareFeature(val valid: Boolean? = null, val version: String? = null)
data class WirelessEncryptionFeature(val mode: String? = null, val validOptions: List<String> = emptyList())
data class ControlNetworkFeature(val ipMode: String? = null, val ipAddress: String? = null, val subnetMask: String? = null, val gateway: String? = null, val macAddress: String? = null)
data class AudioChannel(
    val id: String,
    val features: AudioChannelFeatures = AudioChannelFeatures(),
    val constraints: AudioChannelConstraints = AudioChannelConstraints(),
)
data class AudioChannelConstraints(
    val rmsLevelRange: ValueRange? = null,
    val gainRange: ValueRange? = null,
)
data class ValueRange(val min: Double? = null, val max: Double? = null)
data class AudioChannelFeatures(
    val name: String? = null,
    val gain: Double? = null,
    val aes3Locked: Boolean? = null,
    val inputSource: String? = null,
    val danteEncryptionStatus: String? = null,
    val analogInputMode: String? = null,
    val toneFrequency: Double? = null,
    val toneGain: Double? = null,
)
data class RfChannel(
    val id: String,
    val tuning: RfTuning? = null,
    val assignedRfBand: AssignedRfBand? = null,
    val assignedRfProfile: String? = null,
)
data class RfTuning(val frequency: Double? = null, val group: String? = null, val channel: String? = null)
data class AssignedRfBand(val band: String? = null, val frequencyRanges: List<FrequencyRange> = emptyList())
data class FrequencyRange(val min: Double? = null, val max: Double? = null)
