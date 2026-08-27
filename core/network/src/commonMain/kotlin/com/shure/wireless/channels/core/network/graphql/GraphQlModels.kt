package com.shure.wireless.channels.core.network.graphql

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class GraphQlRequest<Variables>(
    val query: String,
    val variables: Variables,
    val operationName: String? = null,
)

@Serializable
data class GraphQlResponse<Data>(
    val data: Data? = null,
    val errors: List<GraphQlError> = emptyList(),
    val extensions: JsonObject? = null,
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()
}

@Serializable
data class GraphQlError(
    val message: String,
    val locations: List<GraphQlErrorLocation> = emptyList(),
    val path: List<JsonElement> = emptyList(),
    val extensions: JsonObject? = null,
)

@Serializable
data class GraphQlErrorLocation(
    val line: Int,
    val column: Int,
)
