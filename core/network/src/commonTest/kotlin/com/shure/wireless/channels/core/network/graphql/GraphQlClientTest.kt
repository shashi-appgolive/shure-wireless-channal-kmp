package com.shure.wireless.channels.core.network.graphql

import com.shure.wireless.channels.core.network.ApiResult
import com.shure.wireless.channels.core.network.NetworkException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GraphQlClientTest {
    @Test
    fun executeDataReturnsTypedGraphQlData() = runTest {
        val client = testClient("""{"data":{"device":{"name":"receiver-1"}}}""")

        val result = GraphQlClient(client, "https://example.test/graphql")
            .executeData<DeviceQueryData>("query Device { device { name } }")

        val success = assertIs<ApiResult.Success<DeviceQueryData>>(result)
        assertEquals("receiver-1", success.data.device.name)
        client.close()
    }

    @Test
    fun executeDataMapsGraphQlErrors() = runTest {
        val client = testClient("""{"errors":[{"message":"Device not found"}]}""")

        val result = GraphQlClient(client, "https://example.test/graphql")
            .executeData<DeviceQueryData>("query Device { device { name } }")

        val error = assertIs<ApiResult.Error>(result)
        val graphQlError = assertIs<NetworkException.GraphQl>(error.exception)
        assertEquals("Device not found", graphQlError.errors.single().message)
        client.close()
    }

    private fun testClient(responseBody: String): HttpClient = HttpClient(
        MockEngine {
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        },
    ) {
        install(ContentNegotiation) { json() }
    }
}

@Serializable
private data class DeviceQueryData(val device: TestGraphQlDevice)

@Serializable
private data class TestGraphQlDevice(val name: String)
