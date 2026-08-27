package com.shure.wireless.channels.core.datastore

data class DataStoreConfig(
    val fileName: String = "shure_channels.preferences_pb",
    val customPath: String? = null,
) {
    init {
        require(fileName.isNotBlank()) { "fileName cannot be blank" }
    }
}
