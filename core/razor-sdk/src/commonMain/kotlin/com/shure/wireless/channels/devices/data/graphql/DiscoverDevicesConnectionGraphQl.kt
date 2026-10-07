package com.shure.wireless.channels.devices.data.graphql

import com.shure.wireless.channels.devices.domain.model.*
import com.shure.wireless.channels.devices.domain.model.DeviceFeatures as DomainDeviceFeatures
import kotlinx.serialization.Serializable

internal const val DiscoverDevicesConnectionOperationName = "DiscoverDevicesConnection"
internal const val DiscoverDevicesConnectionQuery = """
    query DiscoverDevicesConnection {
      discoveredDevicesConnection {
        edges { node {
          id hardwareId status
          interface { model category type }
          features {
            name { name }
            rfBand { band }
            firmware { valid version }
            hostedFirmware { version }
            serialNumber { serialNumber }
            wirelessEncryption { mode validOptions }
            quadversity { enabled }
            antennaBiasVoltage { enabled }
            rfTransmissionMode { mode }
            networkSwitchConfiguration { mode }
            controlNetwork { interface { ipMode ipAddress subnetMask gateway macAddress } }
            danteAudioNetwork { name }
            audioChannels { audioChannels { id
              description { constraints {
                rms { level { range { min max } } }
                gain { gain { range { min max } } }
              } }
              features {
                name { name } gain { gain } aes3Lock { locked }
                inputSelection { inputSource } danteEncryptionStatus { status }
                analogInputUse { mode } toneGenerator { frequency gain }
              }
            } }
            rfChannels { rfChannels { id features {
              tuning { frequency groupAndChannel { group channel } }
              assignedRfBand { band frequencyRanges { min max } }
              assignedRfProfile { profile }
            } } }
          }
        } }
      }
    }
"""

@Serializable internal data class ConnectionData(val discoveredDevicesConnection: Connection? = null)
@Serializable internal data class Connection(val edges: List<Edge> = emptyList())
@Serializable internal data class Edge(val node: Node? = null)
@Serializable internal data class Node(
    val id: String,
    val hardwareId: String? = null,
    val status: String? = null,
    val `interface`: InterfaceNode? = null,
    val features: FeaturesNode? = null,
)
@Serializable internal data class InterfaceNode(val model: String? = null, val category: String? = null, val type: String? = null)
@Serializable internal data class FeaturesNode(
    val name: NameNode? = null, val rfBand: BandNode? = null, val firmware: FirmwareNode? = null,
    val hostedFirmware: VersionNode? = null, val serialNumber: SerialNode? = null,
    val wirelessEncryption: EncryptionNode? = null, val quadversity: EnabledNode? = null,
    val antennaBiasVoltage: EnabledNode? = null, val rfTransmissionMode: ModeNode? = null,
    val networkSwitchConfiguration: ModeNode? = null, val controlNetwork: ControlNetworkNode? = null,
    val danteAudioNetwork: NameNode? = null, val audioChannels: AudioChannelsNode? = null,
    val rfChannels: RfChannelsNode? = null,
)
@Serializable internal data class NameNode(val name: String? = null)
@Serializable internal data class BandNode(val band: String? = null)
@Serializable internal data class FirmwareNode(val valid: Boolean? = null, val version: String? = null)
@Serializable internal data class VersionNode(val version: String? = null)
@Serializable internal data class SerialNode(val serialNumber: String? = null)
@Serializable internal data class EncryptionNode(val mode: String? = null, val validOptions: List<String> = emptyList())
@Serializable internal data class EnabledNode(val enabled: Boolean? = null)
@Serializable internal data class ModeNode(val mode: String? = null)
@Serializable internal data class ControlNetworkNode(val `interface`: NetworkInterfaceNode? = null)
@Serializable internal data class NetworkInterfaceNode(val ipMode: String? = null, val ipAddress: String? = null, val subnetMask: String? = null, val gateway: String? = null, val macAddress: String? = null)
@Serializable internal data class AudioChannelsNode(val audioChannels: List<AudioChannelNode> = emptyList())
@Serializable internal data class AudioChannelNode(val id: String, val description: AudioDescriptionNode? = null, val features: AudioFeaturesNode? = null)
@Serializable internal data class AudioDescriptionNode(val constraints: ConstraintsNode? = null)
@Serializable internal data class ConstraintsNode(val rms: ConstraintValueNode? = null, val gain: GainConstraintNode? = null)
@Serializable internal data class ConstraintValueNode(val level: LevelConstraintNode? = null)
@Serializable internal data class LevelConstraintNode(val range: RangeNode? = null)
@Serializable internal data class GainConstraintNode(val gain: GainLevelNode? = null)
@Serializable internal data class GainLevelNode(val range: RangeNode? = null)
@Serializable internal data class RangeNode(val min: Double? = null, val max: Double? = null)
@Serializable internal data class AudioFeaturesNode(val name: NameNode? = null, val gain: GainNode? = null, val aes3Lock: LockedNode? = null, val inputSelection: InputNode? = null, val danteEncryptionStatus: StatusNode? = null, val analogInputUse: ModeNode? = null, val toneGenerator: ToneNode? = null)
@Serializable internal data class GainNode(val gain: Double? = null)
@Serializable internal data class LockedNode(val locked: Boolean? = null)
@Serializable internal data class InputNode(val inputSource: String? = null)
@Serializable internal data class StatusNode(val status: String? = null)
@Serializable internal data class ToneNode(val frequency: Double? = null, val gain: Double? = null)
@Serializable internal data class RfChannelsNode(val rfChannels: List<RfChannelNode> = emptyList())
@Serializable internal data class RfChannelNode(val id: String, val features: RfFeaturesNode? = null)
@Serializable internal data class RfFeaturesNode(val tuning: TuningNode? = null, val assignedRfBand: AssignedRfBandNode? = null, val assignedRfProfile: ProfileNode? = null)
@Serializable internal data class TuningNode(val frequency: Double? = null, val groupAndChannel: GroupChannelNode? = null)
@Serializable internal data class GroupChannelNode(val group: String? = null, val channel: String? = null)
@Serializable internal data class AssignedRfBandNode(val band: String? = null, val frequencyRanges: List<FrequencyRangeNode> = emptyList())
@Serializable internal data class FrequencyRangeNode(val min: Double? = null, val max: Double? = null)
@Serializable internal data class ProfileNode(val profile: String? = null)

internal fun ConnectionData.toModel() = DiscoveredDevicesConnection(
    discoveredDevicesConnection?.edges.orEmpty().mapNotNull { edge -> edge.node?.toModel() },
)

private fun Node.toModel(): DiscoveredDevice = DiscoveredDevice(
    id = id,
    hardwareId = hardwareId,
    status = status,
    interfaceInfo = `interface`?.let { DeviceInterfaceInfo(it.model, it.category, it.type) },
    features = features?.toDeviceFeatures() ?: DomainDeviceFeatures(),
)

private fun FeaturesNode.toDeviceFeatures(): DomainDeviceFeatures = DomainDeviceFeatures(
    name = name?.name,
    rfBand = rfBand?.band,
    firmware = firmware?.let { FirmwareFeature(it.valid, it.version) },
    hostedFirmwareVersion = hostedFirmware?.version,
    serialNumber = serialNumber?.serialNumber,
    wirelessEncryption = wirelessEncryption?.let { WirelessEncryptionFeature(it.mode, it.validOptions) },
    quadversityEnabled = quadversity?.enabled,
    antennaBiasVoltageEnabled = antennaBiasVoltage?.enabled,
    rfTransmissionMode = rfTransmissionMode?.mode,
    networkSwitchMode = networkSwitchConfiguration?.mode,
    controlNetwork = controlNetwork?.`interface`?.let { ControlNetworkFeature(it.ipMode, it.ipAddress, it.subnetMask, it.gateway, it.macAddress) },
    danteAudioNetworkName = danteAudioNetwork?.name,
    audioChannels = audioChannels?.audioChannels.orEmpty().mapNotNull { channel ->
        AudioChannel(channel.id, channel.features?.let { feature ->
            AudioChannelFeatures(
                name = feature.name?.name,
                gain = feature.gain?.gain,
                aes3Locked = feature.aes3Lock?.locked,
                inputSource = feature.inputSelection?.inputSource,
                danteEncryptionStatus = feature.danteEncryptionStatus?.status,
                analogInputMode = feature.analogInputUse?.mode,
                toneFrequency = feature.toneGenerator?.frequency,
                toneGain = feature.toneGenerator?.gain,
            )
        } ?: AudioChannelFeatures(), channel.description?.constraints?.let { constraints ->
            AudioChannelConstraints(
                rmsLevelRange = constraints.rms?.level?.range?.let { range -> ValueRange(range.min, range.max) },
                gainRange = constraints.gain?.gain?.range?.let { range -> ValueRange(range.min, range.max) },
            )
        } ?: AudioChannelConstraints()).takeIf { !it.features.name.isNullOrBlank() }
    },
    rfChannels = rfChannels?.rfChannels.orEmpty().map { channel ->
        val feature = channel.features
        RfChannel(
            id = channel.id,
            tuning = feature?.tuning?.let { tuning -> RfTuning(tuning.frequency, tuning.groupAndChannel?.group, tuning.groupAndChannel?.channel) },
            assignedRfBand = feature?.assignedRfBand?.let { band -> AssignedRfBand(band.band, band.frequencyRanges.map { range -> FrequencyRange(range.min, range.max) }) },
            assignedRfProfile = feature?.assignedRfProfile?.profile,
        )
    },
)
