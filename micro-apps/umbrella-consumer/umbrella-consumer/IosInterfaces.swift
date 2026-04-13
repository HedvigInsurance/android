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
        return "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJpbXBlcnNvbmF0ZWQtYnkiOiJhZG1fNWNmOTA4ZjAtMGZhMi00ZGE0LWExNzAtMjcyNjQwNTc3MDVhIiwic3ViIjoibWVtXzc4MjU4MDMwIiwiZXhwIjoxNzc2MTAyMzMxLCJpYXQiOjE3NzYwOTg3MzF9.AIHnCfcU-Om4kxbPCVdMFe7xBLImjOsTrNckDZu866ahyH1txaS8Klfb_LYdlhmVg8wPYMxS1WlAX4iq2X4bgsPPdtFUDNiXV98eOjVhLBi3chd_fQDqzARkXDnSJTXXxPX-afg-_SmZ6NLONg3XmDfxlfeM3ROa26SMXRQcauVNo-nBUDKIVUyViC_Q53B8bUwFrjjuKPfD9Thwfr5ecmAR9990oJdGPUYUUkAT7mP2I2kyGzu0rTWRR5KMmXjdDKnBt2Lv9c-vuYGjOAcGFEUuHom13ji41YbI4zffOYZtdCXMTEj5sCEDkwpqWIA52CnNrlAKH57P-uUnzGdehw"
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
