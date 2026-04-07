import SwiftUI
import HedvigShared

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
        return "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJpbXBlcnNvbmF0ZWQtYnkiOiJhZG1fNWNmOTA4ZjAtMGZhMi00ZGE0LWExNzAtMjcyNjQwNTc3MDVhIiwic3ViIjoibWVtXzc4MjU4MDMwIiwiZXhwIjoxNzc1MTQxNTY5LCJpYXQiOjE3NzUxMzc5Njl9.mMD7dmawceSYL43UHeyzvRpnwfeK1HDjG32fVyJuFsHvcmJYQWeFJyDnfodllaW6kv5LzrPZoNZNcWU7qFLoIyr3PNfXsf-Wa11za2RTKnevlZlh3OEMFhlJrOz6znppg518ed9kCOJwyJ1KtPLyLi9w1RhUGaFHX5AEKPYXUgaWc3HipyEAcB74eyPlIdo102HzllCpONdhIPX_HjCMubRWzv6uJUT-i8SicOmXgaaamE4eMokZ2Zht3Iz77tSFZf8SooaXmrDcC2ELLdNNwvIkMiFhXpTJ9zmEv2vCy2DFmKOnRjU8FokbrwFGA8Nakt3EV70gjk3YskcaKlj10g"
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
