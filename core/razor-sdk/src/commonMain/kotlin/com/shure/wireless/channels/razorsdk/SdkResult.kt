package com.shure.wireless.channels.razorsdk

sealed interface SdkResult<out T> {
    data class Success<T>(val value: T) : SdkResult<T>
    data class Failure(val error: SdkError) : SdkResult<Nothing>
}

sealed interface SdkError {
    data object InvalidInput : SdkError
    data class DeviceOperation(val code: String?, val message: String) : SdkError
    data class Network(val message: String) : SdkError
    data class Unknown(val message: String) : SdkError
}
