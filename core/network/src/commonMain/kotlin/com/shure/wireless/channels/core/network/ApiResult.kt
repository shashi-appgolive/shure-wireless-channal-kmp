package com.shure.wireless.channels.core.network

sealed interface ApiResult<out T> {
    data class Success<T>(
        val data: T,
        val statusCode: Int,
    ) : ApiResult<T>

    data class Error(
        val exception: NetworkException,
        val statusCode: Int? = null,
    ) : ApiResult<Nothing>
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data), statusCode)
    is ApiResult.Error -> this
}

inline fun <T, R> ApiResult<T>.fold(
    onSuccess: (T) -> R,
    onError: (ApiResult.Error) -> R,
): R = when (this) {
    is ApiResult.Success -> onSuccess(data)
    is ApiResult.Error -> onError(this)
}
