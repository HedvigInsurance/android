import Foundation
@preconcurrency import HedvigShared
import UIKit

/// Identifies the shell to the backend as an iOS staging build. Kotlin reads it once, while the graph is built.
final class ShellAppBuildConfig: NSObject, Core_build_constantsAppBuildConfig, @unchecked Sendable {
    let appFlavor: Core_build_constantsFlavor = .staging
    let applicationId: String
    let brand: String = "Apple"
    let buildType: String = "debug"
    let debug: Bool = true
    let device: String
    let manufacturer: String = "Apple"
    let model: String
    let osReleaseVersion: String
    let osSdkVersion: Int32
    let platformName: String = "ios"
    let versionCode: Int32
    let versionName: String

    @MainActor
    override init() {
        let info = Bundle.main.infoDictionary ?? [:]
        applicationId = Bundle.main.bundleIdentifier ?? "com.hedvig.umbrella-consumer"
        versionName = info["CFBundleShortVersionString"] as? String ?? "1.0"
        versionCode = Int32(info["CFBundleVersion"] as? String ?? "") ?? 1
        osReleaseVersion = UIDevice.current.systemVersion
        osSdkVersion = Int32(osReleaseVersion.split(separator: ".").first ?? "") ?? 0
        let machine = ShellAppBuildConfig.machineIdentifier()
        device = machine
        model = ProcessInfo.processInfo.environment["SIMULATOR_MODEL_IDENTIFIER"] ?? machine
        super.init()
    }

    private static func machineIdentifier() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)
        return withUnsafeBytes(of: &systemInfo.machine) { buffer in
            String(decoding: buffer.prefix(while: { $0 != 0 }), as: UTF8.self)
        }
    }
}

/// A UUID kept in `UserDefaults` under the key the Hedvig iOS app uses. Kotlin calls it from request threads.
final class ShellDeviceIdFetcher: NSObject, Core_datastore_publicDeviceIdFetcher, @unchecked Sendable {
    private let key = "hedvig-device-identifier"

    func fetch() async throws -> String? {
        let defaults = UserDefaults.standard
        if let existing = defaults.string(forKey: key) {
            return existing
        }
        let created = UUID().uuidString
        defaults.set(created, forKey: key)
        return created
    }
}

/// Always English in Sweden. Kotlin reads it from request threads.
final class ShellLanguageStorage: NSObject, Language_coreLanguageStorage, @unchecked Sendable {
    func getCurrentLanguageTag() -> String { "en-SE" }
    func getSelectedLanguageTag() -> String? { "en-SE" }
    func setLanguageTag(tag: String) {}
}

/// The shell has no demo mode.
final class ShellDemoMode: NSObject, Core_demo_modeIosDemoMode, @unchecked Sendable {
    func isDemoMode() -> Bool { false }
    func setDemoMode(demoMode: Bool) {}
}
