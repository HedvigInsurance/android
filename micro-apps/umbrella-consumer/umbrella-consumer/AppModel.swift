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
    @ObservationIgnored private let store: KeychainTokenStore
    @ObservationIgnored private let refresher: TokenRefresher

    init() {
        let store = KeychainTokenStore()
        let authClient = AuthClient()
        let forcedLogout = ForcedLogoutRelay()
        let refresher = TokenRefresher(store: store, authClient: authClient) { forcedLogout.fire() }
        self.store = store
        self.authClient = authClient
        self.refresher = refresher
        self.holder = ShellGraph.makeHolder(accessTokenFetcher: ShellAccessTokenFetcher(refresher: refresher))
        let stored = (try? store.read()) ?? nil
        self.isLoggedIn = stored != nil
        ShellLog.info("Launched, stored session present: \(stored != nil)")
        forcedLogout.handler = { [weak self] in self?.logOut() }
    }

    func didLogIn(session: StoredSession) {
        store.save(session)
        let refresher = refresher
        Task { await refresher.sessionStarted() }
        holder.sessionChanged(loggedIn: true)
        ShellLog.info("sessionChanged(loggedIn: true)")
        isLoggedIn = true
    }

    /// Leaves the logged-in UI first, so screen models are closed, then forgets the session in the Keychain and in
    /// Kotlin, then revokes the refresh token without waiting for the result.
    func logOut() {
        guard isLoggedIn else { return }
        let previous = (try? store.read()) ?? nil
        isLoggedIn = false
        store.delete()
        holder.sessionChanged(loggedIn: false)
        ShellLog.info("sessionChanged(loggedIn: false)")
        if let refreshToken = previous?.refreshToken {
            let authClient = authClient
            Task.detached { await authClient.revoke(refreshToken: refreshToken) }
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
