import SwiftUI
import HedvigShared

@main
struct umbrella_consumerApp: App {
    init() {
        let keychain = IosKeychainAbstraction()
        Main_nativeKt.doInitKoin(
            accessTokenFetcher: IosAccessTokenFetcher(keychain),
            deviceIdFetcher: IosDeviceIdFetcher(),
            featureManager: iosFeatureManager(),
            appBuildConfig: IosAppBuildConfig(),
        )
        LogcatLogger.install(AndroidLogcatLogger())
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class IosDeviceIdFetcher: DeviceIdFetcher {
    func fetch() async throws -> String? {
        return UIDevice.current.identifierForVendor?.uuidString
    }
}

func iosFeatureManager() -> FeatureManager {
    return IosFeatureManager(
        isFeatureEnabledBlock: { feature in
            switch(feature) {
                default: false
            }
        }
    )
}

class IosAppBuildConfig: AppBuildConfig {
    var appFlavor: Flavor = Flavor.develop
    
    var applicationId: String = ""
    
    var brand: String = ""
    
    var buildType: String = ""
    
    var debug: Bool = true
    
    var device: String = ""
    
    var manufacturer: String = ""
    
    var model: String = ""
    
    var osReleaseVersion: String = ""
    
    var osSdkVersion: Int32 = 0
    
    var versionCode: Int32 = 0
    
    var versionName: String = ""
}

class IosAccessTokenFetcher: AccessTokenFetcher {
    let keychainAbstraction: KeychainAbstraction
    
    init(_ keychainAbstraction: KeychainAbstraction) {
        self.keychainAbstraction = keychainAbstraction
    }
    
    func fetch() async throws -> String? {
        return try await keychainAbstraction.getToken()
    }
}

protocol KeychainAbstraction {
    func getToken() async throws -> String
}

class IosKeychainAbstraction: KeychainAbstraction {
    func getToken() async throws -> String {
        try await Task.sleep(for: .seconds(1))
        return "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJpbXBlcnNvbmF0ZWQtYnkiOiJhZG1fNWNmOTA4ZjAtMGZhMi00ZGE0LWExNzAtMjcyNjQwNTc3MDVhIiwic3ViIjoibWVtXzc4MjU4MDMwIiwiZXhwIjoxNzc0NjI3MzU5LCJpYXQiOjE3NzQ2MjM3NTl9.DmkEGYfndhhIdzx-quWF5hVBPiV6XTyivryStxt58mrSiGMtITp_6wCLMC9czUNyjnRc-_UpxbSccZ_vcdvT72BwVHpxNijZ94RCKYXWjziBpcjBwt01LgOPc4vJMGY770XkrqhX43qJ8bjgTIyO93qZXUHo5Y8TIliTMjOVemuDsEuhoSdXNIpudZ9uFvXTJZfy-LypwVxJ9IqWi3DopI7JxpfaWUL3ywfZWYxEDcObiqP0d49nVtSYSIXsZPXoNNbBWHfqnTetaZ60l1ebY90mIsPIDsvEVSZZPTH4WoC2mMps_LgWO5hHfD2CNn-TTpwMN1QJsaXpCBDFtwPulg"
    }
}
