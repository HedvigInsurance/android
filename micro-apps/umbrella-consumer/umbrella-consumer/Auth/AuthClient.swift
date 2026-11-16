import Foundation
@preconcurrency import HedvigShared
@preconcurrency import KMPNativeCoroutinesAsync

enum LoginStep: Sendable {
    case waiting
    case qrCode(String)
    case completed(authorizationCode: String)
    case failed(String)
}

enum RefreshOutcome: Sendable {
    case success(StoredSession)
    /// The auth service could not be reached.
    case network
    /// The auth service answered, and not with tokens.
    case rejected
    /// A Kotlin exception surfaced as an error.
    case other(String)
}

struct AuthClientError: Error, Sendable {
    let message: String
}

/// Every call into `authlib`, against the staging auth service.
final class AuthClient: @unchecked Sendable {
    private let repository = NetworkAuthRepository(environment: .staging, additionalHttpHeadersProvider: { [:] })

    /// Starts a Swedish BankID login and returns its status URL.
    func startBankId() async throws -> String {
        let result = try await asyncFunction(
            for: repository.startLoginAttempt(loginMethod: .seBankid, market: .se, personalNumber: nil, email: nil)
        )
        switch result {
        case let properties as AuthAttemptResultBankIdProperties:
            return properties.statusUrl.url
        case let localised as AuthAttemptResultErrorLocalised:
            throw AuthClientError(message: localised.reason)
        default:
            ShellLog.error("startLoginAttempt failed: \(String(describing: type(of: result)))")
            throw AuthClientError(message: "Could not start the login")
        }
    }

    func loginSteps(statusUrl: String) -> AsyncThrowingStream<LoginStep, Error> {
        AsyncThrowingStream { continuation in
            let task = Task {
                do {
                    let statuses = asyncSequence(for: self.repository.observeLoginStatus(statusUrl: StatusUrl(url: statusUrl)))
                    for try await status in statuses {
                        continuation.yield(AuthClient.step(for: status))
                    }
                    continuation.finish()
                } catch {
                    continuation.finish(throwing: error)
                }
            }
            continuation.onTermination = { _ in task.cancel() }
        }
    }

    func exchange(authorizationCode: String) async throws -> StoredSession {
        let result = try await asyncFunction(
            for: repository.exchange(grant: AuthorizationCodeGrant(code: authorizationCode))
        )
        guard let success = result as? AuthTokenResultSuccess else {
            ShellLog.error("Authorization code exchange failed: \(String(describing: type(of: result)))")
            throw AuthClientError(message: "Login failed")
        }
        return AuthClient.session(from: success)
    }

    func refresh(refreshToken: String) async -> RefreshOutcome {
        do {
            let result = try await asyncFunction(for: repository.exchange(grant: RefreshTokenGrant(code: refreshToken)))
            switch result {
            case let success as AuthTokenResultSuccess:
                return .success(AuthClient.session(from: success))
            case _ as AuthTokenResultErrorIOError:
                return .network
            default:
                ShellLog.error("Refresh rejected: \(String(describing: type(of: result)))")
                return .rejected
            }
        } catch {
            return .other(error.localizedDescription)
        }
    }

    func revoke(refreshToken: String) async {
        do {
            let result = try await asyncFunction(for: repository.revoke(token: refreshToken))
            ShellLog.info("Revoke result: \(String(describing: type(of: result)))")
        } catch {
            ShellLog.error("Revoke failed: \(error.localizedDescription)")
        }
    }

    private static func step(for status: LoginStatusResult) -> LoginStep {
        switch status {
        case let pending as LoginStatusResultPending:
            if let qrData = pending.bankIdProperties?.liveQrCodeData {
                return .qrCode(qrData)
            }
            return .waiting
        case let completed as LoginStatusResultCompleted:
            return .completed(authorizationCode: completed.authorizationCode.code)
        case let failed as LoginStatusResultFailed:
            return .failed(failed.localisedMessage)
        default:
            return .failed("Login failed")
        }
    }

    private static func session(from success: AuthTokenResultSuccess) -> StoredSession {
        let now = Date()
        return StoredSession(
            accessToken: success.accessToken.token,
            accessTokenExpiresAt: now.addingTimeInterval(TimeInterval(success.accessToken.expiryInSeconds)),
            refreshToken: success.refreshToken.token,
            refreshTokenExpiresAt: now.addingTimeInterval(TimeInterval(success.refreshToken.expiryInSeconds))
        )
    }
}
