import Foundation
@preconcurrency import HedvigShared

/// Kotlin calls this before every request, from a request thread. A throw fails the request without sending it.
final class ShellAccessTokenFetcher: NSObject, Network_clientsAccessTokenFetcher, @unchecked Sendable {
    private let refresher: TokenRefresher

    init(refresher: TokenRefresher) {
        self.refresher = refresher
        super.init()
    }

    func fetch() async throws -> String? {
        try await refresher.validAccessToken()
    }
}
