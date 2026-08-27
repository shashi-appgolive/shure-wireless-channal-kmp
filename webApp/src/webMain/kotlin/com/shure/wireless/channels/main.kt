package com.shure.wireless.channels

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.shure.wireless.channels.di.initializeWebInMemoryPersistence

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeWebInMemoryPersistence()
    ComposeViewport {
        App()
    }
}
