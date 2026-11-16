import Foundation
import Observation

/// Runs BankID login attempts. An attempt that showed a QR code and then ended without a login (the code expired,
/// or BankID reported a failure) is followed by a new attempt one second later, so a fresh code is always up.
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
        var attempt = 0
        while !Task.isCancelled {
            attempt += 1
            ShellLog.info("Login attempt \(attempt) starting")
            switch await runAttempt() {
            case .loggedIn:
                return
            case let .expired(reason):
                ShellLog.info("Login attempt \(attempt) ended (\(reason)), starting a new one")
                state = .starting
                try? await Task.sleep(for: .seconds(1))
            case let .failed(message):
                if !Task.isCancelled {
                    state = .failed(message)
                }
                return
            }
        }
    }

    private func runAttempt() async -> Outcome {
        var shownQrCode = false
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
                    ShellLog.info("BankID completed, exchanging the authorization code")
                    state = .exchanging
                    let session = try await app.authClient.exchange(authorizationCode: authorizationCode)
                    ShellLog.info("Authorization code exchanged")
                    app.didLogIn(session: session)
                    return .loggedIn
                case let .failed(message):
                    return shownQrCode ? .expired(message) : .failed(message)
                }
            }
            return shownQrCode ? .expired("status stream ended") : .failed("Login ended unexpectedly")
        } catch let error as AuthClientError {
            return shownQrCode ? .expired(error.message) : .failed(error.message)
        } catch {
            if Task.isCancelled {
                return .failed("Cancelled")
            }
            ShellLog.error("Login attempt failed: \(error.localizedDescription)")
            return shownQrCode ? .expired("error") : .failed("Login failed")
        }
    }
}
