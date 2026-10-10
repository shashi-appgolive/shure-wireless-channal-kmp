package com.shure.wireless.channels.ui.components

import com.shure.wireless.channels.devices.domain.model.AssignedRfBand
import com.shure.wireless.channels.devices.domain.model.FrequencyRange
import com.shure.wireless.channels.devices.domain.model.RfChannel
import com.shure.wireless.channels.devices.domain.model.RfTuning
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChannelRfSettingsUiModelTest {
    @Test
    fun discoveredRfSettingsAreFormattedForDisplay() {
        val model = RfChannel(
            id = "rf-1",
            tuning = RfTuning(frequency = 507375.0, group = "1", channel = "1"),
            assignedRfBand = AssignedRfBand(
                band = "G53",
                frequencyRanges = listOf(FrequencyRange(min = 470000.0, max = 534000.0)),
            ),
            assignedRfProfile = "Profile A",
        ).toChannelRfSettingsUiModel()

        assertEquals("507.375 MHz", model.currentFrequency)
        assertEquals("G 1 C 1", model.groupAndChannel)
        assertEquals("470.000 - 534.000 MHz", model.frequencyRange)
        assertEquals("G53", model.rfBand)
        assertEquals("Profile A", model.rfProfile)
    }

    @Test
    fun missingRfSettingsRemainAbsentAndDeviceBandIsUsedAsFallback() {
        val model = RfChannel(id = "rf-1").toChannelRfSettingsUiModel(deviceRfBand = "G53")

        assertNull(model.currentFrequency)
        assertNull(model.groupAndChannel)
        assertNull(model.frequencyRange)
        assertEquals("G53", model.rfBand)
        assertNull(model.rfProfile)
    }
}

