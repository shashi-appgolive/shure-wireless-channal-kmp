# Core Common

Shared architecture primitives for Android, iOS, JavaScript, and Wasm under the
`com.shure.wireless.channels.core.common` package.

The module contains:

- `Result<T, E>` and reusable `Failure` types.
- `AsyncOperation<T>` with loading, success, and failure states.
- Convo-compatible `FlowUseCase2`, `FlowUseCase3`, `FlowContextUseCase`, and
  `ResultUseCase` contracts.
- `SuspendingUseCase`, `FlowUseCase`, and `ObservableUseCase` alternatives.
- A platform logger backed by Logcat, `NSLog`, or the browser console.
- Managed foreground and background application coroutine scopes.
- A Koin `commonModule` for the application-level scopes.

## One-shot use case

```kotlin
class SaveDeviceUseCase(
    private val repository: DeviceRepository,
) : FlowUseCase3<Unit, Device>() {
    override suspend fun executeInternal(params: Device): Completed<Unit> {
        repository.save(params)
        return success(Unit)
    }
}

saveDeviceUseCase.execute("SaveDevice", device).collect { operation ->
    // Loading, followed by Completed success/failure.
}
```

The shared device sample also includes `GetStoredDevicesUseCase`, implemented
with `FlowUseCase2`, and `ObserveStoredDevicesUseCase` for continuous Room
updates.

## Demo operation path

The wireless console demonstrates the full clean-architecture path:

```text
Compose UI
  → DeviceViewModel
  → ConnectDeviceUseCase / DiscoverDevicesUseCase
  → DeviceOperationsRepository
  → SaveStoredDevicesUseCase
  → StoredDeviceRepository
  → StoredDeviceDao / Room
  → ObserveStoredDevicesUseCase
  → DeviceViewModel UI state
```

Every layer writes through `Logger`; `AppLogStore` mirrors those platform logs
into the on-screen operation console.

Exceptions are logged and converted to `ExceptionFailure`. Coroutine
cancellation is always rethrown and is never presented as an application error.

## Observable use case

```kotlin
observeStoredDevices(NoParams).collect { operation ->
    when {
        operation.isLoading -> showLoading()
        operation.isSuccess -> showDevices(operation.data.orEmpty())
        operation.isFailure -> showError(operation.error?.message)
    }
}
```

The working sample is in `shared/devices`: the domain layer owns the model,
repository contract, and use cases; the data layer implements that contract
using `StoredDeviceDao`. Koin connects the layers without exposing Room to the
domain.
