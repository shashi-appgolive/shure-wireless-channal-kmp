# Core DataStore

Cross-platform Preferences DataStore for Android, iOS, JavaScript, and Wasm.

`PreferencesDataStore` provides typed write, `Flow`, and one-shot read APIs for
strings, integers, booleans, longs, floats, and string sets. Android and iOS use
file storage; JS and Wasm use browser `localStorage`.

```kotlin
val module = dataStoreModule(
    factory = platformDataStoreFactory,
    config = DataStoreConfig(fileName = "shure_channels.preferences_pb"),
)

preferences.putBoolean("auto_scan", true)
preferences.getBoolean("auto_scan").collect { enabled ->
    println("Auto scan: $enabled")
}
```

The Android application initializes this module from `ChannelsApplication`.
The iOS application initializes it from `iOSApp.init()` before Compose starts.
