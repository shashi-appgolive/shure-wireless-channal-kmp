package com.shure.wireless.channels.ui.components

import com.shure.wireless.channels.devices.domain.model.RfChannel
import com.shure.wireless.channels.formatWorkbenchFrequency

internal data class ChannelRfSettingsUiModel(
    val currentFrequency: String?,
    val groupAndChannel: String?,
    val frequencyRange: String?,
    val rfBand: String?,
    val rfProfile: String?,
)

internal fun RfChannel.toChannelRfSettingsUiModel(deviceRfBand: String? = null): ChannelRfSettingsUiModel {
    val range = assignedRfBand?.frequencyRanges?.firstOrNull { it.min != null && it.max != null }
    val rangeText = range?.min?.let { min ->
        range.max?.let { max ->
            "${formatWorkbenchFrequency(min).removeSuffix(" MHz")} - ${formatWorkbenchFrequency(max)}"
        }
    }
    val tuning = tuning
    val groupAndChannel = listOfNotNull(
        tuning?.group?.takeIf { it.isNotBlank() }?.let { "G $it" },
        tuning?.channel?.takeIf { it.isNotBlank() }?.let { "C $it" },
    ).joinToString(" ").takeIf { it.isNotBlank() }

    return ChannelRfSettingsUiModel(
        currentFrequency = tuning?.frequency?.let(::formatWorkbenchFrequency),
        groupAndChannel = groupAndChannel,
        frequencyRange = rangeText,
        rfBand = assignedRfBand?.band ?: deviceRfBand,
        rfProfile = assignedRfProfile,
    )
}

