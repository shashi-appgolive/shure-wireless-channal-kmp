import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinBootstrapKt.startChannelsPersistence()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
