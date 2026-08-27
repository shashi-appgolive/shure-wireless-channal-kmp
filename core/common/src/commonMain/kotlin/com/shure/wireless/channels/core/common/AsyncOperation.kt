package com.shure.wireless.channels.core.common

sealed interface AsyncOperation<out T> {
    val isLoading: Boolean
        get() = this is Loading

    val isSuccess: Boolean
        get() = this is Completed<*> && result.isSuccess

    val isFailure: Boolean
        get() = this is Completed<*> && result.isFailure

    val data: T?
        get() = when (this) {
            is Loading -> null
            is Completed -> result.getOrNull()
        }

    val error: Failure?
        get() = when (this) {
            is Loading -> null
            is Completed -> result.errorOrNull()
        }
}

data object Loading : AsyncOperation<Nothing>

data class Completed<T>(val result: UseCaseResult<T>) : AsyncOperation<T>

fun <T> loading(): AsyncOperation<T> = Loading

fun <T> success(data: T): Completed<T> = Completed(Result.success(data))

fun <T> failure(error: Failure): Completed<T> = Completed(Result.failure(error))

fun <T> UseCaseResult<T>.toAsyncOperation(): Completed<T> = Completed(this)

inline fun <T> AsyncOperation<T>.onSuccess(action: (T) -> Unit): AsyncOperation<T> = apply {
    if (this is Completed) result.onSuccess(action)
}

inline fun <T> AsyncOperation<T>.onFailure(action: (Failure) -> Unit): AsyncOperation<T> = apply {
    if (this is Completed) result.onFailure(action)
}
