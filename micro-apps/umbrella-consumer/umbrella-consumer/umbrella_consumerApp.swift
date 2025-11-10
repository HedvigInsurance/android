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
    
    func fetch(completionHandler: @escaping (String) -> Void) {
        let semaphore = DispatchSemaphore(value: 0)
        Task {
            let token = try await keychainAbstraction.getToken()
            completionHandler(token)
            semaphore.signal()
        }
        semaphore.wait()
    }
}

protocol KeychainAbstraction {
    func getToken() async throws -> String
}

class IosKeychainAbstraction: KeychainAbstraction {
    func getToken() async throws -> String {
        try await Task.sleep(for: .seconds(2))
        return "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJpbXBlcnNvbmF0ZWQtYnkiOiJhZG1fNWNmOTA4ZjAtMGZhMi00ZGE0LWExNzAtMjcyNjQwNTc3MDVhIiwic3ViIjoibWVtXzQ1NTQ2OTk5NyIsImV4cCI6MTc2Mjc3Njk5NywiaWF0IjoxNzYyNzczMzk3fQ.MnpLETxgaw46hUT9R_GyUn3GJli2CCqVoulvkILIa2qSki8cwKmN_y0HKRFbqjLVGumjyyuMO5vcJZljjtm-zhcwjkf44xSTdPFdi4dkWgbWAvQXL9mTytsGuLcoJoShTd5ZPkh0xHGKVj7VAmVagnveBFluuEeVM8-J2gt-XjehD2XGzJaqIp_rOJLn5Y8pmChj1b3zGfNhnzn9BKDZkLIzuy-qaeX5gsTOc4Wy2FsF1YyiQMWBUtds1Drkjdz20Arpu1_4QNSAnPd4i0qPstS0lULBp52vkP784BX0nV61oDHWULr7e3fhbLq3o_pNVl2KgLqeen3QrecNaGqdmA"
    }
}
