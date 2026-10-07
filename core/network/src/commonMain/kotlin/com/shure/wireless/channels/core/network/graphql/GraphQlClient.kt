package com.shure.wireless.channels.core.network.graphql

import com.shure.wireless.channels.core.network.ApiResult
import com.shure.wireless.channels.core.network.GraphQlErrorSummary
import com.shure.wireless.channels.core.network.NetworkException
import com.shure.wireless.channels.core.network.safeNetworkCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class GraphQlClient(
    @PublishedApi internal val httpClient: HttpClient,
    val endpoint: String,
) {
    suspend inline fun <reified Data, reified Variables> execute(
        query: String,
        variables: Variables,
        operationName: String? = null,
        headers: Map<String, String> = emptyMap(),
        endpoint: String = this.endpoint,
    ): ApiResult<GraphQlResponse<Data>> = safeNetworkCall {
        val response = httpClient.post(endpoint) {
            headers.forEach { (name, value) -> header(name, value) }
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(
                GraphQlRequest(
                    query = query,
                    variables = variables,
                    operationName = operationName,
                ),
            )
        }

        ApiResult.Success(
            data = response.body(),
            statusCode = response.status.value,
        )
    }

    suspend inline fun <reified Data> execute(
        query: String,
        operationName: String? = null,
        headers: Map<String, String> = emptyMap(),
        endpoint: String = this.endpoint,
    ): ApiResult<GraphQlResponse<Data>> = execute(
        query = query,
        variables = buildJsonObject { },
        operationName = operationName,
        headers = headers,
        endpoint = endpoint,
    )

    suspend inline fun <reified Data, reified Variables> executeData(
        query: String,
        variables: Variables,
        operationName: String? = null,
        headers: Map<String, String> = emptyMap(),
        endpoint: String = this.endpoint,
    ): ApiResult<Data> = when (
        val result = execute<Data, Variables>(query, variables, operationName, headers, endpoint)
    ) {
        is ApiResult.Error -> result
        is ApiResult.Success -> {
            val envelope = result.data
            when {
                envelope.errors.isNotEmpty() -> ApiResult.Error(
                    NetworkException.GraphQl(
                        envelope.errors.map { error ->
                            GraphQlErrorSummary(
                                message = error.message,
                                path = error.path.map { element ->
                                    when (element) {
                                        is JsonPrimitive -> element.content
                                        else -> element.toString()
                                    }
                                },
                            )
                        },
                    ),
                    statusCode = result.statusCode,
                )

                envelope.data != null -> ApiResult.Success(envelope.data, result.statusCode)
                else -> ApiResult.Error(
                    NetworkException.GraphQl(
                        listOf(GraphQlErrorSummary("GraphQL response contained no data")),
                    ),
                    statusCode = result.statusCode,
                )
            }
        }
    }

    suspend inline fun <reified Data> executeData(
        query: String,
        operationName: String? = null,
        headers: Map<String, String> = emptyMap(),
        endpoint: String = this.endpoint,
    ): ApiResult<Data> = executeData<Data, JsonObject>(
        query = query,
        variables = buildJsonObject { },
        operationName = operationName,
        headers = headers,
        endpoint = endpoint,
    )
}
