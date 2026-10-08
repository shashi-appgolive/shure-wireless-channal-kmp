package com.shure.wireless.channels.devices.presentation

import com.shure.wireless.channels.devices.domain.model.AudioChannel
import com.shure.wireless.channels.devices.domain.model.AudioChannelFeatures
import com.shure.wireless.channels.devices.domain.model.DeviceFeatures
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.RfChannel
import kotlin.test.Test
import kotlin.test.assertEquals

class InventoryUiStateTest {
    private val devices = listOf(
        DiscoveredDevice("z", features = DeviceFeatures(name = "Zulu", rfChannels = listOf(RfChannel("z-1"), RfChannel("z-2")))),
        DiscoveredDevice("a", features = DeviceFeatures(name = "alpha", audioChannels = listOf(AudioChannel("a-1", features = AudioChannelFeatures(name = "Vocal"))))),
        DiscoveredDevice("bravo"),
    )

    @Test
    fun sortsByReceiverNameThenKeepsChannelsInReceiverOrder() {
        val visible = InventoryUiState().withDevices(devices).visibleDevices

        assertEquals(listOf("a", "bravo", "z"), visible.map { it.id })
        assertEquals(listOf("z-1", "z-2"), visible.last().features.rfChannels.map { it.id })
        assertEquals(listOf("z", "a", "bravo"), devices.map { it.id })
    }

    @Test
    fun searchMatchesChannelNamesIgnoringCase() {
        assertEquals(listOf("a"), InventoryUiState(searchQuery = "  VOCAL ").withDevices(devices).visibleDevices.map { it.id })
    }

    @Test
    fun onlineFilterAndReverseSortUseReceiverStatusAndName() {
        val receivers = listOf(
            DiscoveredDevice("z", status = "ONLINE", features = DeviceFeatures(name = "Zulu")),
            DiscoveredDevice("b", status = "OFFLINE", features = DeviceFeatures(name = "Beta")),
            DiscoveredDevice("a", status = "CONNECTED", features = DeviceFeatures(name = "Alpha")),
        )
        val state = InventoryUiState(onlineOnly = true, sortAscending = false).withDevices(receivers)

        assertEquals(listOf("z", "a"), state.visibleDevices.map { it.id })
        assertEquals(2, state.onlineCount)
        assertEquals(3, state.totalDevices)
        assertEquals(true, state.hasKnownStatuses)
    }

    @Test
    fun unknownStatusesAreNotReportedAsOffline() {
        val state = InventoryUiState().withDevices(devices)

        assertEquals(false, state.hasKnownStatuses)
        assertEquals(3, state.totalDevices)
    }
}
