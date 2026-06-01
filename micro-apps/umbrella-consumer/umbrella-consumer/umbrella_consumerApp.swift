import SwiftUI
import HedvigShared

@main
struct umbrella_consumerApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
}

class AppDelegate: UIResponder, UIApplicationDelegate {
    // Run initializers on app launch
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        Main_nativeKt.doInitKoin(
            accessTokenFetcher: IosAccessTokenFetcher(IosKeychainAbstraction()),
            deviceIdFetcher: IosDeviceIdFetcher(),
            featureManager: iosFeatureManager(),
            appBuildConfig: IosAppBuildConfig(),
        )
        IosLogcatLogger.companion.install()
        return true
    }
}
