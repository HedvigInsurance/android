import HedvigShared
import SwiftUI

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
        return "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJpbXBlcnNvbmF0ZWQtYnkiOiJhZG1fNWNmOTA4ZjAtMGZhMi00ZGE0LWExNzAtMjcyNjQwNTc3MDVhIiwic3ViIjoibWVtXzMzMDY3MTM3NiIsImV4cCI6MTc3ODA4MzQ2NSwiaWF0IjoxNzc4MDc5ODY1fQ.Tw40qwWJ9P3C8zxUYB4EKPXEbE3UfeCvTvF0fDh5bkhl0g6kZLO847ePJiBYEq9IGg6UXLEJfKx7veBNnU5aSIAtVrftKv167IANvyZBIEJR20rYi4qU_eayLaaCkFbp2L-9bVb3ZWNbErHlF9-f1tnEddajm3WPlrJ_eXklLdnXfA3dX6JTAkY-D-O5NdgVcjMVFJWuKMEv72fpHL1XMM51jWuAE4f6-ezJH3yviynBzIdxMAYuOPo8weeMPN7hieCFF95AFY8quwN8K2T9a6rE4-U8lTKr88Q6YLq_OBAq3RfSoG22WBFRb1WYj4yK2L9er4PWtzVNj3KbCkUIhA"
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
            switch feature {
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
