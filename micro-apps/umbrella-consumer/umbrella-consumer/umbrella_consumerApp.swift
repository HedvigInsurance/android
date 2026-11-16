@preconcurrency import HedvigShared
import SwiftUI

@main
struct umbrella_consumerApp: App {
    @State private var app: AppModel

    init() {
        IosLogcatLogger.companion.install()
        _app = State(initialValue: AppModel())
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(app)
        }
    }
}
