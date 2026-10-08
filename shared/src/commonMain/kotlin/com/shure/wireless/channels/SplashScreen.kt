package com.shure.wireless.channels

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import shurechannelskmp.shared.generated.resources.Res
import shurechannelskmp.shared.generated.resources.ic_splash_screen

@Composable
internal fun SplashScreen() {
    Image(
        painter = painterResource(Res.drawable.ic_splash_screen),
        contentDescription = null,
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentScale = ContentScale.Fit,
    )
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}
