package com.shure.wireless.channels

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ChannelMetersTest {
    @Test
    fun rfSignalLevelsClampToMeterRange() {
        assertEquals(0f, rfSignalProgress(null))
        assertEquals(0f, rfSignalProgress(-100.0))
        assertEquals(0.5f, rfSignalProgress(-45.0))
        assertEquals(1f, rfSignalProgress(0.0))
    }

    @Test
    fun inventoryRfBarsChangeColorOnlyAtFullScale() {
        assertEquals(Color.White, inventoryRfMeterColor(null))
        assertEquals(Color.White, inventoryRfMeterColor(-45.0))
        assertEquals(Color(0xFFFF4D4F), inventoryRfMeterColor(0.0))
    }
}
