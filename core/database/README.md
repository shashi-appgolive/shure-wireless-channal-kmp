# Core Database

Room 3.0 database shared by Android, iOS, JavaScript, and Wasm.

The initial schema contains `StoredDeviceEntity` and `StoredDeviceDao` for
persisting and observing discovered Shure devices. Android and iOS use the
bundled SQLite driver. Web callers provide a `WebWorkerSQLiteDriver` through
`createDatabaseFactory(driver)`.

```kotlin
val module = databaseModule(platformDatabaseFactory)

val devices: Flow<List<StoredDeviceEntity>> = storedDeviceDao.observeAll()
storedDeviceDao.upsert(
    StoredDeviceEntity(
        id = "device-id",
        name = "ULXD4",
        ipAddress = "192.168.1.20",
        lastSeenAtEpochMillis = now,
    ),
)
```

For JS/Wasm, Room 3 requires a worker implementing the AndroidX SQLite worker
protocol. Construct `WebWorkerSQLiteDriver(worker)` in the web application and
pass that driver to `initializeWebPersistence`. Room 3.0 does not currently
provide a default worker implementation.

Android creates the database eagerly from `ChannelsApplication`. iOS creates it
from `iOSApp.init()` before the first Compose screen is presented.
