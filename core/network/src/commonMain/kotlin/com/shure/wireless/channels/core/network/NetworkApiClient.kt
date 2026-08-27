package com.shure.wireless.channels.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class NetworkApiClient(
    @PublishedApi internal val httpClient: HttpClient,
    baseUrl: String,
) {
    val baseUrl: String = baseUrl.trimEnd('/')

    suspend inline fun <reified Response> get(
        path: String,
        headers: Map<String, String> = emptyMap(),
        queryParameters: Map<String, String> = emptyMap(),
    ): ApiResult<Response> = safeNetworkCall {
        val response = httpClient.get(resolve(path)) {
            headers.forEach { (name, value) -> header(name, value) }
            queryParameters.forEach { (name, value) -> url.parameters.append(name, value) }
            accept(ContentType.Application.Json)
        }
        ApiResult.Success(response.body(), response.status.value)
    }

    suspend inline fun <reified Request, reified Response> post(
        path: String,
        body: Request,
        headers: Map<String, String> = emptyMap(),
    ): ApiResult<Response> = safeNetworkCall {
        val response = httpClient.post(resolve(path)) {
            headers.forEach { (name, value) -> header(name, value) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ApiResult.Success(response.body(), response.status.value)
    }

    suspend inline fun <reified Request, reified Response> put(
        path: String,
        body: Request,
        headers: Map<String, String> = emptyMap(),
    ): ApiResult<Response> = safeNetworkCall {
        val response = httpClient.put(resolve(path)) {
            headers.forEach { (name, value) -> header(name, value) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ApiResult.Success(response.body(), response.status.value)
    }

    suspend inline fun <reified Response> delete(
        path: String,
        headers: Map<String, String> = emptyMap(),
    ): ApiResult<Response> = safeNetworkCall {
        val response = httpClient.delete(resolve(path)) {
            headers.forEach { (name, value) -> header(name, value) }
            accept(ContentType.Application.Json)
        }
        ApiResult.Success(response.body(), response.status.value)
    }

    suspend inline fun <reified Request, reified Response> delete(
        path: String,
        body: Request,
        headers: Map<String, String> = emptyMap(),
    ): ApiResult<Response> = safeNetworkCall {
        val response = httpClient.delete(resolve(path)) {
            headers.forEach { (name, value) -> header(name, value) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        ApiResult.Success(response.body(), response.status.value)
    }

    @PublishedApi
    internal fun resolve(path: String): String =
        if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            "$baseUrl/${path.trimStart('/')}"
        }
}
