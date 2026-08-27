package com.shure.wireless.channels.core.common

sealed interface Result<out T, out E> {
    data class Success<T>(val data: T) : Result<T, Nothing>
    data class Failure<E>(val error: E) : Result<Nothing, E>

    val isSuccess: Boolean
        get() = this is Success<*>

    val isFailure: Boolean
        get() = this is Failure<*>

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    fun errorOrNull(): E? = when (this) {
        is Success -> null
        is Failure -> error
    }

    companion object {
        fun <T> success(value: T): Result<T, Nothing> = Success(value)
        fun <E> failure(error: E): Result<Nothing, E> = Failure(error)
    }
}

inline fun <T, E, R> Result<T, E>.fold(
    onSuccess: (T) -> R,
    onFailure: (E) -> R,
): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Failure -> onFailure(error)
}

inline fun <T, E, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> =
    fold(
        onSuccess = { Result.success(transform(it)) },
        onFailure = { Result.failure(it) },
    )

inline fun <T, E, R> Result<T, E>.mapError(transform: (E) -> R): Result<T, R> =
    fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(transform(it)) },
    )

inline fun <T, E, R> Result<T, E>.flatMap(transform: (T) -> Result<R, E>): Result<R, E> =
    fold(
        onSuccess = transform,
        onFailure = { Result.failure(it) },
    )

inline fun <T, E> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> = apply {
    if (this is Result.Success) action(data)
}

inline fun <T, E> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> = apply {
    if (this is Result.Failure) action(error)
}

inline fun <T, E> Result<T, E>.getOrElse(defaultValue: (E) -> T): T =
    fold(onSuccess = { it }, onFailure = defaultValue)
