package com.shure.wireless.channels.core.network

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

class NetworkApiClientTest {
    @Test
    fun getDeserializesResponseAndPreservesStatusCode() = runTest {
        val engine = MockEngine { request ->
            assertEquals("https://example.test/devices", request.url.toString())
            respond(
                content = """{"name":"receiver-1"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }

        val result = NetworkApiClient(client, "https://example.test/")
            .get<TestDevice>("/devices")

        val success = assertIs<ApiResult.Success<TestDevice>>(result)
        assertEquals(TestDevice("receiver-1"), success.data)
        assertEquals(200, success.statusCode)
        client.close()
    }
}

@Serializable
private data class TestDevice(val name: String)
