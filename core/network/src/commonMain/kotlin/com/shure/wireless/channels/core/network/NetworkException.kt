package com.shure.wireless.channels.core.network

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.websocket.WebSocketException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlin.coroutines.cancellation.CancellationException

sealed class NetworkException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class Http(
        val statusCode: Int,
        val responseBody: String?,
    ) : NetworkException("HTTP $statusCode${responseBody?.let { ": $it" }.orEmpty()}")

    class Timeout(cause: Throwable) : NetworkException("The request timed out", cause)
    class Connectivity(cause: Throwable) : NetworkException("Unable to reach the server", cause)
    class Serialization(cause: Throwable) : NetworkException("Unable to read the server response", cause)
    class WebSocket(cause: Throwable) : NetworkException("WebSocket connection failed", cause)

    class GraphQl(
        val errors: List<GraphQlErrorSummary>,
    ) : NetworkException(errors.joinToString(separator = "; ") { it.message })

    class Unknown(cause: Throwable) : NetworkException(cause.message ?: "Unknown network error", cause)
}

data class GraphQlErrorSummary(
    val message: String,
    val path: List<String> = emptyList(),
)

internal suspend fun HttpResponse.toNetworkException(): NetworkException.Http =
    NetworkException.Http(
        statusCode = status.value,
        responseBody = runCatching { bodyAsText() }.getOrNull(),
    )

@PublishedApi
internal suspend inline fun <T> safeNetworkCall(
    crossinline block: suspend () -> ApiResult<T>,
): ApiResult<T> = try {
    block()
} catch (exception: CancellationException) {
    throw exception
} catch (exception: NetworkException) {
    ApiResult.Error(
        exception = exception,
        statusCode = (exception as? NetworkException.Http)?.statusCode,
    )
} catch (exception: HttpRequestTimeoutException) {
    ApiResult.Error(NetworkException.Timeout(exception))
} catch (exception: ConnectTimeoutException) {
    ApiResult.Error(NetworkException.Timeout(exception))
} catch (exception: SocketTimeoutException) {
    ApiResult.Error(NetworkException.Timeout(exception))
} catch (exception: kotlinx.serialization.SerializationException) {
    ApiResult.Error(NetworkException.Serialization(exception))
} catch (exception: WebSocketException) {
    ApiResult.Error(NetworkException.WebSocket(exception))
} catch (exception: Throwable) {
    ApiResult.Error(NetworkException.Connectivity(exception))
}
