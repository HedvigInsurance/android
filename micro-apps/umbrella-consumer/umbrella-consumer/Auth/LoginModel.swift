import Foundation
import Observation
import Security

/// Runs BankID login attempts. An attempt that showed a QR code and then ended without a login (the code expired,
/// or BankID reported a failure) is followed by a new attempt one second later, up to five in a row; after that the
/// screen asks for a tap on "New code".
@MainActor
@Observable
final class LoginModel {
    enum State: Equatable {
        case starting
        case showingQr(String)
        case exchanging
        case failed(String)
    }

    private(set) var state: State = .starting

    @ObservationIgnored private let app: AppModel
    @ObservationIgnored private var task: Task<Void, Never>?
    private static let maxConsecutiveExpiredAttempts = 5

    init(app: AppModel) {
        self.app = app
    }

    func start() {
        task?.cancel()
        state = .starting
        task = Task { await self.run() }
    }

    func cancel() {
        task?.cancel()
        task = nil
    }

    private enum Outcome {
        case loggedIn
        case expired(String)
        case failed(String)
    }

    private func run() async {
        var expiredInARow = 0
        var attempt = 0
        while !Task.isCancelled {
            attempt += 1
            ShellLog.info("Login attempt \(attempt) starting")
            let outcome = await runAttempt()
            guard !Task.isCancelled else { return }
            switch outcome {
            case .loggedIn:
                return
            case let .expired(reason):
                expiredInARow += 1
                if expiredInARow >= LoginModel.maxConsecutiveExpiredAttempts {
                    ShellLog.info("Login attempt \(attempt) ended (\(reason)), stopping after \(expiredInARow) expired attempts")
                    state = .failed("Code expired. Tap New code.")
                    return
                }
                ShellLog.info("Login attempt \(attempt) ended (\(reason)), starting a new one")
                state = .starting
                try? await Task.sleep(for: .seconds(1))
            case let .failed(message):
                state = .failed(message)
                return
            }
        }
    }

    private func runAttempt() async -> Outcome {
        var shownQrCode = false
        var completed = false
        do {
            let statusUrl = try await app.authClient.startBankId()
            for try await step in app.authClient.loginSteps(statusUrl: statusUrl) {
                switch step {
                case .waiting:
                    if !shownQrCode {
                        state = .starting
                    }
                case let .qrCode(data):
                    if !shownQrCode {
                        ShellLog.info("QR code shown (payload length \(data.count))")
                    }
                    shownQrCode = true
                    state = .showingQr(data)
                case let .completed(authorizationCode):
                    completed = true
                    ShellLog.info("BankID completed, exchanging the authorization code")
                    state = .exchanging
                    let session = try await app.authClient.exchange(authorizationCode: authorizationCode)
                    ShellLog.info("Authorization code exchanged")
                    let status = await app.didLogIn(session: session)
                    guard status == errSecSuccess else {
                        ShellLog.error("Login not completed, Keychain write OSStatus \(status)")
                        return .failed("Could not save the session (OSStatus \(status))")
                    }
                    return .loggedIn
                case let .failed(message):
                    return shownQrCode ? .expired(message) : .failed(message)
                }
            }
            return shownQrCode ? .expired("status stream ended") : .failed("Login ended unexpectedly")
        } catch let error as AuthClientError {
            if completed {
                return .failed(error.message)
            }
            return shownQrCode ? .expired(error.message) : .failed(error.message)
        } catch {
            if Task.isCancelled {
                return .failed("Cancelled")
            }
            ShellLog.error("Login attempt failed: \(String(describing: type(of: error)))")
            if completed {
                return .failed("Login failed")
            }
            return shownQrCode ? .expired("error") : .failed("Login failed")
        }
    }
}
