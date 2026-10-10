package com.shure.wireless.channels.ui.components

import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.formatWorkbenchFrequency

internal data class ReceiverSettingRow(
    val label: String,
    val value: String,
    val highlighted: Boolean = false,
    val editable: Boolean = false,
)
internal data class ReceiverSettingSection(val title: String, val rows: List<ReceiverSettingRow>)

private val transmitterModels = setOf("ADTD", "ADTQ", "P10T", "PSM1000")

internal fun settingsDeviceNoun(model: String?): String =
    if (model?.trim()?.uppercase() in transmitterModels) "Transmitter" else "Receiver"

internal fun DiscoveredDevice.settingsDeviceNoun(): String = settingsDeviceNoun(interfaceInfo?.model)

internal fun DiscoveredDevice.receiverSettingsSections(channelIndex: Int): List<ReceiverSettingSection> {
    val deviceNoun = settingsDeviceNoun()
    val band = features.rfChannels.getOrNull(channelIndex)?.assignedRfBand
    val range = band?.frequencyRanges?.firstOrNull { it.min != null && it.max != null }
    val rangeText = range?.min?.let { min ->
        range.max?.let { max ->
            "${formatWorkbenchFrequency(min).removeSuffix(" MHz")} - ${formatWorkbenchFrequency(max)}"
        }
    }

    val receiver = buildList {
        addSetting("$deviceNoun ID", features.name, highlighted = true, editable = true)
        addSetting("$deviceNoun Model", interfaceInfo?.model)
        addSetting("Band", band?.band ?: features.rfBand)
        addSetting("Range", rangeText)
        addSetting("Firmware Version", features.firmware?.version)
        addSetting("Hosted Firmware Version", features.hostedFirmwareVersion)
        addSetting("Serial Number", features.serialNumber)
        addSetting("Encryption", features.wirelessEncryption?.mode)
        addSetting("Antenna Bias", features.antennaBiasVoltageEnabled?.let { if (it) "On" else "Off" })
        addSetting("Transmission Mode", features.rfTransmissionMode)
        addSetting("Quadversity", features.quadversityEnabled?.let { if (it) "On" else "Off" })
    }
    val network = buildList {
        addSetting("Switch Configuration", features.networkSwitchMode)
    }
    val control = buildList {
        addSetting("Network Mode", features.controlNetwork?.ipMode?.let { if (it.equals("DHCP", ignoreCase = true)) "Automatic" else it })
        addSetting("IP Address", features.controlNetwork?.ipAddress)
        addSetting("Subnet Mask", features.controlNetwork?.subnetMask)
        addSetting("Gateway", features.controlNetwork?.gateway)
        addSetting("MAC Address", features.controlNetwork?.macAddress)
    }
    val dante = buildList {
        addSetting("Dante Name", features.danteAudioNetworkName)
    }
    return listOf(
        ReceiverSettingSection("${deviceNoun.uppercase()} INFORMATION", receiver),
        ReceiverSettingSection("NETWORK", network),
        ReceiverSettingSection("SHURE CONTROL", control),
        ReceiverSettingSection("DANTE PRIMARY", dante),
    ).filter { it.rows.isNotEmpty() }
}

private fun MutableList<ReceiverSettingRow>.addSetting(label: String, value: String?, highlighted: Boolean = false, editable: Boolean = false) {
    value?.takeIf { it.isNotBlank() }?.let { add(ReceiverSettingRow(label, it, highlighted, editable)) }
}
