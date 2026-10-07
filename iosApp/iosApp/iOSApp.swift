import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinBootstrapKt.startChannelsAppGraph()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
