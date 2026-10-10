package com.shure.wireless.channels.ui.components

import com.shure.wireless.channels.devices.domain.model.AudioChannel
import com.shure.wireless.channels.devices.domain.model.AudioChannelFeatures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChannelAudioSettingsUiModelTest {
    @Test
    fun availableToneValuesAreMappedWithoutInventingOtherSettings() {
        val model = AudioChannel(
            id = "audio-2",
            features = AudioChannelFeatures(gain = 10.0, inputSource = "DANTE", toneGain = -12.5, toneFrequency = 400.0),
        ).toAudioSettingsUiModel()

        assertEquals(10, model.gain)
        assertEquals("DANTE", model.audioOutput)
        assertEquals("-12.5 dB", model.toneVolume)
        assertEquals(400.0, model.toneFrequency)
    }

    @Test
    fun missingToneFieldsStayAbsent() {
        val model = AudioChannel(id = "audio-1").toAudioSettingsUiModel()
        assertEquals(0, model.gain)
        assertNull(model.audioOutput)
        assertNull(model.toneVolume)
        assertNull(model.toneFrequency)
    }
}
