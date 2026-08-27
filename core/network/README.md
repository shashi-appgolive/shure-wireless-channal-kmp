# Core Network

Shared Ktor networking for Android, iOS, JavaScript, and Wasm.

The module provides:

- `networkModule` for Koin-managed, independently configured REST and GraphQL clients.
- `NetworkApiClient` for typed REST GET, POST, PUT, and DELETE requests.
- `GraphQlClient` for GraphQL queries and mutations over HTTP.
- `WebSocketClient` for full-duplex WebSocket sessions.
- `ApiResult` and `NetworkException` for consistent transport error handling.

```kotlin
val networkConfig = NetworkModuleConfig(
    rest = NetworkEndpointConfig(
        baseUrl = "https://rest.example.com",
    ),
    graphQl = NetworkEndpointConfig(
        baseUrl = "https://graphql.example.com/graphql",
    ),
    webSocket = WebSocketEndpointConfig(
        baseUrl = "wss://events.example.com/socket",
    ),
)

startKoin {
    modules(networkModule(networkConfig))
    createEagerInstances()
}

val restClient = getKoin().get<NetworkApiClient>(RestApiClientQualifier)
val graphQlClient = getKoin().get<GraphQlClient>(GraphQlApiClientQualifier)
val webSocketClient = getKoin().get<WebSocketClient>(WebSocketApiClientQualifier)

val devices = restClient.get<List<DeviceDto>>("devices")
val inventory = graphQlClient.executeData<InventoryData>(
    query = "query Inventory { devices { id name } }",
)

webSocketClient.connect("devices") {
    send(Frame.Text("scan"))
    for (frame in incoming) {
        if (frame is Frame.Text) {
            println(frame.readText())
        }
    }
}
```

For app-level lifecycle control, extend `WebSocketCallback` and obtain a
`WebSocketController` with the app-owned `CoroutineScope`:

```kotlin
class AppSocketCallback : WebSocketCallback() {
    override fun onConnected() {
        println("Socket connected")
    }

    override fun onTextMessage(text: String) {
        println("Changed: $text")
    }

    override fun onFailure(exception: Throwable) {
        println("Socket failed: ${exception.message}")
    }
}

val controller = getKoin().get<WebSocketController> {
    parametersOf(appScope, AppSocketCallback())
}

controller.connect("devices")
appScope.launch { controller.sendText("scan") }
controller.disconnect()
```

The callback also exposes `onConnecting`, `onBinaryMessage`, `onClosing`, and
`onClosed`. Call `cancel()` when the owning app-level component is disposed.

The named qualifiers also expose `RestHttpClientQualifier`,
`GraphQlHttpClientQualifier`, `WebSocketHttpClientQualifier`, their API client
qualifiers, and their base URL qualifiers for APIs that need direct access.
Koin closes each underlying `HttpClient` (and its active sessions) when the
application is stopped.

Use `openSession()` instead of `connect()` when the caller needs to own a
long-lived `DefaultClientWebSocketSession`. The caller must close that session.
Ktor's typed `sendSerialized()` and `receiveDeserialized()` APIs are available
because the Kotlinx JSON WebSocket converter is installed.

Browser WebSocket APIs do not allow arbitrary handshake headers. For JS/Wasm,
use secure cookies, query parameters, or a negotiated subprotocol for browser
authentication. Automatic ping support is engine-dependent, so
`pingIntervalMillis` is optional and disabled by default.

`NetworkClientFactory` remains available for callers that do not use Koin;
provide `webSocketUrl` to receive its independently owned WebSocket client.

GraphQL subscriptions and schema-driven code generation remain separate concerns.
The generic WebSocket transport can host a subscription protocol once its
schema and protocol details are available.
