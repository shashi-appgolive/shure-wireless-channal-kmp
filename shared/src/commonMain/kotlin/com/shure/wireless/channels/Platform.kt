package com.shure.wireless.channels

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform