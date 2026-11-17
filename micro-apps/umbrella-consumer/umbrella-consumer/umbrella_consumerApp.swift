@preconcurrency import HedvigShared
import SwiftUI

@main
struct umbrella_consumerApp: App {
    @State private var app: AppModel

    init() {
        #if DEBUG
        ShellDebug.installHeaderLoggingIfRequested()
        IosLogcatLogger.companion.install(minimumPriority: .verbose)
        #else
        IosLogcatLogger.companion.install(minimumPriority: .warn)
        #endif
        _app = State(initialValue: AppModel())
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(app)
        }
    }
}
