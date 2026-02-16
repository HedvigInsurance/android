import SwiftUI
import HedvigShared

@main
struct umbrella_consumerApp: App {
    init() {
        let keychain = IosKeychainAbstraction()
//        Main_nativeKt.doInitKoin(
//            accessTokenFetcher: IosAccessTokenFetcher(keychain),
//        )
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

//class IosAccessTokenFetcher: AccessTokenFetcher {
//    let keychainAbstraction: KeychainAbstraction
//    
//    init(_ keychainAbstraction: KeychainAbstraction) {
//        self.keychainAbstraction = keychainAbstraction
//    }
//    
//    func fetch() async throws -> String? {
//        return try await keychainAbstraction.getToken()
//    }
//}

protocol KeychainAbstraction {
    func getToken() async throws -> String
}

class IosKeychainAbstraction: KeychainAbstraction {
    func getToken() async throws -> String {
        try await Task.sleep(for: .seconds(2))
        return "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJtZW1fNzgyNTgwMzAiLCJleHAiOjE3NzEyNTMwMjQsImlhdCI6MTc3MTI0OTQyNH0.bS-rmQpIp1CtY5uBWmyAeW1MYfrwdI8W6iYcfD2XYXssBZv8dPs--49asTXE-7ycsYaTZQDhFz0bJ57pckEdxp5TGhFi7O7jKXUQygptUOwRtLKF2oG5p3T8CB8b1QA4sD8JiP14VwNjRGcRfE037Q3lKqq8UVVv3vukTIEO4MUIzidtnmud8SxorsQPrqiOXliCQrqhut9abfGrvHAVONiRYJEwmjPUiIspLhZZl-GsLLWH0c0oSJXdZYQFHDN9951jTUdt5_0s9ToePBNK6XZWwr_5Y5A3p9Cjc07MQYQQeVJoN9whues7-RV9QsPNXlPYanYxXjvysWC1sEZWaw"
    }
}
