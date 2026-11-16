import Foundation

enum TokenRefreshError: Error, Sendable {
    case notLoggedIn
    case refreshTokenExpired
    case network
    case rejected
    case other(String)
}

/// Hands out an access token that is valid for at least another 60 seconds, refreshing it through `authlib` first
/// when needed. Concurrent callers share one in-flight attempt and its result, failures included. Logs out when there
/// is no session, when the refresh token has expired, or when the auth service rejects the refresh; a network
/// failure or a Kotlin exception only fails the current request.
actor TokenRefresher {
    private let store: KeychainTokenStore
    private let authClient: AuthClient
    private let onForcedLogout: @MainActor @Sendable () -> Void
    private var inFlight: Task<String, Error>?
    private var hasForcedLogout = false

    init(
        store: KeychainTokenStore,
        authClient: AuthClient,
        onForcedLogout: @escaping @MainActor @Sendable () -> Void
    ) {
        self.store = store
        self.authClient = authClient
        self.onForcedLogout = onForcedLogout
    }

    func sessionStarted() {
        hasForcedLogout = false
    }

    func validAccessToken() async throws -> String {
        if let inFlight {
            return try await inFlight.value
        }
        // Nothing suspends between the check above and this assignment, so every caller arriving while the body
        // runs joins it. The body clears the slot itself, so a caller arriving after it ends starts afresh.
        let task = Task {
            defer { self.inFlight = nil }
            return try await self.resolve()
        }
        inFlight = task
        return try await task.value
    }

    private func resolve() async throws -> String {
        let session: StoredSession?
        do {
            session = try store.read()
        } catch {
            throw TokenRefreshError.other("Keychain read failed")
        }
        guard let session else {
            await forceLogout(reason: "no stored session")
            throw TokenRefreshError.notLoggedIn
        }
        if session.accessTokenExpiresAt > Date().addingTimeInterval(60) {
            return session.accessToken
        }
        guard Date() < session.refreshTokenExpiresAt else {
            await forceLogout(reason: "refresh token expired")
            throw TokenRefreshError.refreshTokenExpired
        }
        ShellLog.info("Refreshing the access token")
        switch await authClient.refresh(refreshToken: session.refreshToken) {
        case let .success(refreshed):
            store.save(refreshed)
            ShellLog.info("Access token refreshed")
            return refreshed.accessToken
        case .network:
            ShellLog.info("Refresh postponed, network unavailable")
            throw TokenRefreshError.network
        case .rejected:
            await forceLogout(reason: "refresh rejected")
            throw TokenRefreshError.rejected
        case let .other(message):
            ShellLog.error("Refresh failed: \(message)")
            throw TokenRefreshError.other(message)
        }
    }

    private func forceLogout(reason: String) async {
        guard !hasForcedLogout else { return }
        hasForcedLogout = true
        ShellLog.info("Forcing logout: \(reason)")
        await onForcedLogout()
    }
}
