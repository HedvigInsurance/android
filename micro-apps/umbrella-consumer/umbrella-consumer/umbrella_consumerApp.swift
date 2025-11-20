import SwiftUI
import HedvigShared

@main
struct umbrella_consumerApp: App {
    init() {
        let keychain = IosKeychainAbstraction()
        Main_nativeKt.doInitKoin(accessTokenFetcher: IosAccessTokenFetcher(keychain))
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
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
        try await Task.sleep(for: .seconds(2))
        return "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJpbXBlcnNvbmF0ZWQtYnkiOiJhZG1fNWNmOTA4ZjAtMGZhMi00ZGE0LWExNzAtMjcyNjQwNTc3MDVhIiwic3ViIjoibWVtXzQ1NTQ2OTk5NyIsImV4cCI6MTc2Mjc4NDQyMiwiaWF0IjoxNzYyNzgwODIyfQ.OiSA4vQvWUunCV91RkwyW7zbsjQNjjl-SzTavkDLHr8m_xlZzAnnawHvssp8Lbhx880rSVJi-fE-akrZUYucnvlrP99Tht-G6L_igoeIIG66yFrvyUV6UAtqEyENJFptr_p-7BN-M5zkWuy1y3cunJDUXS0GGj8xsQ9CF9RAu0XkzVa-9Qeo7c2Ya73rHylR8yV8_AVaZ9fVl8bgeJGBnjjK_QT8Nbi7EuS2BKfB4TGPFp2phJnHu1Xio4Nx1CKNt4MiAsm2YYF8apEaquajyDngsstvjcUrnRU2ajmvY5s8UN4ErMAswSgKd_XondVZ_OokJHNTDXIE2Kj-StOYig"
    }
}
