package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AdaptiveWorkbenchLayout(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    listContent: @Composable () -> Unit,
    detailsContent: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize().background(backgroundColor)) {
        if (maxWidth >= 600.dp) {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                androidx.compose.foundation.layout.Box(Modifier.weight(0.42f).fillMaxSize().background(backgroundColor)) { listContent() }
                androidx.compose.foundation.layout.Box(Modifier.weight(0.58f).fillMaxSize().background(backgroundColor)) { detailsContent() }
            }
        } else {
            detailsContent()
        }
    }
}
