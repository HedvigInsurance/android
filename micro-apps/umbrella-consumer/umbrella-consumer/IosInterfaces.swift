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
        return "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJpbXBlcnNvbmF0ZWQtYnkiOiJhZG1fNWNmOTA4ZjAtMGZhMi00ZGE0LWExNzAtMjcyNjQwNTc3MDVhIiwic3ViIjoibWVtXzc4MjU4MDMwIiwiZXhwIjoxNzc2MTY4NjM2LCJpYXQiOjE3NzYxNjUwMzZ9.e7kF2qY3vBJyhrklZEIZ46AUp3AnmnEifgAF2ZzM7s6d9DfiQV4twpzXKWBt-ad376rUVndF2C0i--7lu0lYcje0460Hoo6ta929R3fFpeHpqJZ-Tqg-GHSRIeouZFpKXCrgbAjxzW3Vgtdkt6yG_NRER809PtwzpcIf66dJ3FrQ-p5hc7IyzF5KJ7aOAIJQIHlRuPK-1eKSwgRYBbO98f1LjewMrG_bXPlabZ2s7VBokiWPLX0TkDTI8A7CydMDP3lqWBwKvSbjnhSE6jPuRBoDTN0hSAbF1tCF5tibH6pE0EJcyBdO3IyX75AfqKAqeH3WMmIV7FcCXY3X4B4_6A"
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
