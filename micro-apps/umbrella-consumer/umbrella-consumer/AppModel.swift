@preconcurrency import HedvigShared
import Observation
import Security

/// Owns the process-wide Kotlin graph holder and the session. Created once, by the app.
@MainActor
@Observable
final class AppModel {
    private(set) var isLoggedIn: Bool

    @ObservationIgnored let holder: IosGraphHolder
    @ObservationIgnored let authClient: AuthClient
    @ObservationIgnored private let refresher: TokenRefresher

    init() {
        let store = KeychainTokenStore()
        let authClient = AuthClient()
        let forcedLogout = ForcedLogoutRelay()
        let refresher = TokenRefresher(store: store, authClient: authClient) { forcedLogout.fire() }
        self.authClient = authClient
        self.refresher = refresher
        self.holder = ShellGraph.makeHolder(accessTokenFetcher: ShellAccessTokenFetcher(refresher: refresher))
        let stored = (try? store.read()) ?? nil
        self.isLoggedIn = stored != nil
        ShellLog.info("Launched, stored session present: \(stored != nil)")
        forcedLogout.handler = { [weak self] in self?.logOut() }
    }

    /// Stores the session and enters the logged-in UI. Returns the Keychain status; on failure nothing else happens.
    func didLogIn(session: StoredSession) async -> OSStatus {
        let status = await refresher.startSession(session)
        guard status == errSecSuccess else { return status }
        holder.sessionChanged(loggedIn: true)
        ShellLog.info("sessionChanged(loggedIn: true)")
        isLoggedIn = true
        return status
    }

    /// Leaves the logged-in UI first, so screen models are closed, then forgets the session in the Keychain and in
    /// Kotlin, then revokes the refresh token.
    func logOut() {
        guard isLoggedIn else { return }
        isLoggedIn = false
        let refresher = refresher
        let authClient = authClient
        Task {
            let previous = await refresher.endSession()
            holder.sessionChanged(loggedIn: false)
            ShellLog.info("sessionChanged(loggedIn: false)")
            if let refreshToken = previous?.refreshToken {
                await authClient.revoke(refreshToken: refreshToken)
            }
        }
    }
}

@MainActor
private final class ForcedLogoutRelay {
    var handler: (() -> Void)?

    func fire() {
        handler?()
    }
}
