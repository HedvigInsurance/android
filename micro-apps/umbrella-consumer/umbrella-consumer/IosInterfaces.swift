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
        return "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJpbXBlcnNvbmF0ZWQtYnkiOiJhZG1fNWNmOTA4ZjAtMGZhMi00ZGE0LWExNzAtMjcyNjQwNTc3MDVhIiwic3ViIjoibWVtXzMzMDY3MTM3NiIsImV4cCI6MTc4MDY1NjI5MywiaWF0IjoxNzgwNjUyNjkzfQ.HGpV9q1YxhrP8eNlgQmD-qvpfCLEezXXaU1qKzczrTsDz4Qj1J-ZRuAR59jycIjdu9PQjXumSzNAJRzTUTGyQfqzIJdaKytgtXm07tsy19tlZpIvmRD5AHlNhSxPK5NkiqFuCOreTO2fdL0F0gH4fwZfslUDA7sGdvgvijFXYkbqnLeatH0CvfGuLvkdxY_yyiOkUjY825rBLgYeqYPr421uzClLY9639LR1VSEu-XKS1vxEriXwcALp4n8JGMrh1NUqVYrbDVifZGXcElBiA30HG6C6hbqbwTSjWefCcITJyB8tYMetIUaLFnEWc7srf28KQyI_EjNrtyftCXq2qg"
    }
}

class IosDeviceIdFetcher: DeviceIdFetcher {
    func fetch() async throws -> String? {
        return UIDevice.current.identifierForVendor?.uuidString
    }
}

class IosLanguageStorage: LanguageStorage {
    func getCurrentLanguageTag() -> String { "en-SE" }
    func getSelectedLanguageTag() -> String? { "en-SE" }
    func setLanguageTag(tag: String) {}
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
