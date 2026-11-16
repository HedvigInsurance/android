import Foundation
@preconcurrency import HedvigShared

/// Kotlin calls this before every request, from a request thread. A throw fails the request without sending it.
final class ShellAccessTokenFetcher: NSObject, Network_clientsAccessTokenFetcher, @unchecked Sendable {
    private let refresher: TokenRefresher

    init(refresher: TokenRefresher) {
        self.refresher = refresher
        super.init()
    }

    // SKIE exposes the Kotlin requirement as `__fetch` and adds `fetch()` for callers.
    func __fetch() async throws -> String? {
        #if DEBUG
        if ShellDebug.failTokenFetch {
            ShellLog.info("Access token fetch throwing (-shellFailTokenFetch)")
            throw TokenRefreshError.other("forced by -shellFailTokenFetch")
        }
        #endif
        return try await refresher.validAccessToken()
    }
}
