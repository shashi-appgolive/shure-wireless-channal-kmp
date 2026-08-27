package com.shure.wireless.channels.core.common

interface Failure {
    val message: String?
        get() = null
}

data class NetworkConnectionFailure(
    override val message: String? = "Unable to connect to the network",
    val cause: Throwable? = null,
) : Failure

data class ServiceFailure(
    val statusCode: Int? = null,
    override val message: String? = null,
    val errorBody: String? = null,
) : Failure

data class ValidationFailure(
    override val message: String,
) : Failure

data class ExceptionFailure(
    val cause: Throwable,
) : Failure {
    override val message: String?
        get() = cause.message
}

data class UnknownFailure(
    override val message: String? = "An unknown error occurred",
) : Failure
