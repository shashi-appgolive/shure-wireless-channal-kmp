package com.shure.wireless.channels.core.network.di

import org.koin.core.qualifier.named

object NetworkQualifierNames {
    const val REST_BASE_URL = "RestBaseUrl"
    const val GRAPHQL_BASE_URL = "GraphQlBaseUrl"
    const val WEBSOCKET_BASE_URL = "WebSocketBaseUrl"
    const val REST_HTTP_CLIENT = "RestHttpClient"
    const val GRAPHQL_HTTP_CLIENT = "GraphQlHttpClient"
    const val WEBSOCKET_HTTP_CLIENT = "WebSocketHttpClient"
    const val REST_API_CLIENT = "RestNetworkApiClient"
    const val GRAPHQL_API_CLIENT = "GraphQlApiClient"
    const val WEBSOCKET_API_CLIENT = "WebSocketApiClient"
}

val RestBaseUrlQualifier = named(NetworkQualifierNames.REST_BASE_URL)
val GraphQlBaseUrlQualifier = named(NetworkQualifierNames.GRAPHQL_BASE_URL)
val WebSocketBaseUrlQualifier = named(NetworkQualifierNames.WEBSOCKET_BASE_URL)
val RestHttpClientQualifier = named(NetworkQualifierNames.REST_HTTP_CLIENT)
val GraphQlHttpClientQualifier = named(NetworkQualifierNames.GRAPHQL_HTTP_CLIENT)
val WebSocketHttpClientQualifier = named(NetworkQualifierNames.WEBSOCKET_HTTP_CLIENT)
val RestApiClientQualifier = named(NetworkQualifierNames.REST_API_CLIENT)
val GraphQlApiClientQualifier = named(NetworkQualifierNames.GRAPHQL_API_CLIENT)
val WebSocketApiClientQualifier = named(NetworkQualifierNames.WEBSOCKET_API_CLIENT)
