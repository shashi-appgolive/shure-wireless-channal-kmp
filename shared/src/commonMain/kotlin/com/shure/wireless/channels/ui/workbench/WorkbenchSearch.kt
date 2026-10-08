package com.shure.wireless.channels.ui.workbench

import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice

fun DiscoveredDevice.matchesWorkbenchSearch(query: String): Boolean {
    if (query.isBlank()) return true
    val normalizedQuery = query.trim()
    val searchableText = buildString {
        append(features.name).append(' ')
        append(interfaceInfo?.model).append(' ')
        append(interfaceInfo?.category).append(' ')
        append(interfaceInfo?.type).append(' ')
        append(status).append(' ')
        append(features.rfBand).append(' ')
        features.audioChannels.forEach { append(it.features.name).append(' ') }
        features.rfChannels.forEach {
            append(it.assignedRfProfile).append(' ')
            append(it.tuning?.channel).append(' ')
            append(it.tuning?.frequency).append(' ')
        }
    }
    return searchableText.contains(normalizedQuery, ignoreCase = true)
}
