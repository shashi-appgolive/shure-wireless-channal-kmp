package com.shure.wireless.channels.ui.components

import com.shure.wireless.channels.devices.domain.model.AssignedRfBand
import com.shure.wireless.channels.devices.domain.model.ControlNetworkFeature
import com.shure.wireless.channels.devices.domain.model.DeviceFeatures
import com.shure.wireless.channels.devices.domain.model.DeviceInterfaceInfo
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.FirmwareFeature
import com.shure.wireless.channels.devices.domain.model.FrequencyRange
import com.shure.wireless.channels.devices.domain.model.RfChannel
import com.shure.wireless.channels.devices.domain.model.RECEIVER_ID_MAX_LENGTH
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ReceiverSettingsUiModelTest {
    @Test
    fun psmModelsUseTransmitterTerminology() {
        listOf("ADTD", "ADTQ", "P10T", "PSM1000").forEach { model ->
            val device = DiscoveredDevice(
                id = "id",
                interfaceInfo = DeviceInterfaceInfo(model = model),
                features = DeviceFeatures(name = "Transmitter A"),
            )

            val section = device.receiverSettingsSections(0).first()
            assertEquals("TRANSMITTER INFORMATION", section.title)
            assertEquals(listOf("Transmitter ID", "Transmitter Model"), section.rows.take(2).map { it.label })
            assertEquals("Transmitter", settingsDeviceNoun(model))
        }
    }

    @Test
    fun onlyAvailableSectionsAndRowsAreShown() {
        val device = DiscoveredDevice(
            id = "id",
            interfaceInfo = DeviceInterfaceInfo(model = "ULXD4Q"),
            features = DeviceFeatures(
                name = "Receiver A",
                firmware = FirmwareFeature(version = "2.10.0"),
                antennaBiasVoltageEnabled = false,
                networkSwitchMode = "Switched",
                controlNetwork = ControlNetworkFeature(ipAddress = "192.168.1.104"),
                danteAudioNetworkName = "Dante A",
            ),
        )

        val sections = device.receiverSettingsSections(0)
        assertEquals(listOf("RECEIVER INFORMATION", "NETWORK", "SHURE CONTROL", "DANTE PRIMARY"), sections.map { it.title })
        assertEquals(listOf("Receiver ID", "Receiver Model", "Firmware Version", "Antenna Bias"), sections[0].rows.map { it.label })
        assertEquals(7, RECEIVER_ID_MAX_LENGTH)
        assertEquals(listOf(true, false, false, false), sections[0].rows.map { it.editable })
        assertEquals("Off", sections[0].rows.last().value)
        assertEquals("Switched", sections[1].rows.single().value)
        assertEquals(listOf("IP Address"), sections[2].rows.map { it.label })
        assertEquals("Dante A", sections[3].rows.single().value)
    }

    @Test
    fun missingFieldsDoNotProducePlaceholderRowsOrSections() {
        assertEquals(emptyList(), DiscoveredDevice(id = "id").receiverSettingsSections(0))
        val device = DiscoveredDevice(id = "id", features = DeviceFeatures(name = "  "))
        assertFalse(device.receiverSettingsSections(0).any { it.rows.isNotEmpty() })
    }

    @Test
    fun rangeComesFromSelectedRfChannel() {
        val device = DiscoveredDevice(
            id = "id",
            features = DeviceFeatures(rfChannels = listOf(
                RfChannel(id = "first", assignedRfBand = AssignedRfBand("A", listOf(FrequencyRange(500000.0, 550000.0)))),
                RfChannel(id = "second", assignedRfBand = AssignedRfBand("Z17HD", listOf(FrequencyRange(1492125.0, 1524875.0)))),
            )),
        )

        val rows = device.receiverSettingsSections(1).single().rows
        assertEquals(listOf("Band", "Range"), rows.map { it.label })
        assertEquals("Z17HD", rows[0].value)
        assertEquals("1492.125 - 1524.875 MHz", rows[1].value)
    }

    @Test
    fun dhcpIsDisplayedAsAutomaticOnlyForShureControl() {
        fun mode(ipMode: String) = DiscoveredDevice(
            id = "id",
            features = DeviceFeatures(controlNetwork = ControlNetworkFeature(ipMode = ipMode)),
        ).receiverSettingsSections(0).single().rows.single().value

        assertEquals("Automatic", mode("DHCP"))
        assertEquals("Automatic", mode("dhcp"))
        assertEquals("Static", mode("Static"))
    }
}
