import Foundation
@preconcurrency import HedvigShared
@preconcurrency import KMPNativeCoroutinesAsync
import Observation

/// Owns one Kotlin `ClaimHistoryScreenModel` for as long as the screen exists, reading its states through
/// KMP-NativeCoroutines: a main-actor task iterates the state flow as an async sequence, and `deinit` cancels the task
/// and closes the model.
@MainActor
@Observable
final class ClaimHistoryScreenOwnerFlows {
    private(set) var state: ClaimHistoryViewState

    @ObservationIgnored private let model: ClaimHistoryScreenModel
    @ObservationIgnored private var collection: Task<Void, Never>?

    init(holder: IosGraphHolder) {
        let model = holder.claimHistoryScreenModel()
        let flows = ClaimHistoryScreenModelFlows(model: model)
        self.model = model
        state = ClaimHistoryViewState(flows.state)
        ShellLog.info("ClaimHistoryScreenOwner seeded: \(state.logDescription)")
        collection = Task { [weak self] in
            do {
                for try await kotlinState in asyncSequence(for: flows.stateFlow) {
                    self?.receive(kotlinState)
                }
            } catch is CancellationError {
            } catch {
                ShellLog.error("ClaimHistoryScreenOwner state collection failed: \(type(of: error))")
            }
        }
    }

    func reload() {
        ShellLog.info("ClaimHistoryScreenOwner reload")
        model.reload()
    }

    private func receive(_ kotlinState: ClaimHistoryScreenState) {
        dispatchPrecondition(condition: .onQueue(.main))
        state = ClaimHistoryViewState(kotlinState)
        ShellLog.info("ClaimHistoryScreenOwner state: \(state.logDescription)")
    }

    // Runs on the main actor; verified on iOS 27.2 only, while the deployment target is 17.0.
    isolated deinit {
        ShellLog.info("ClaimHistoryScreenOwner deinit")
        collection?.cancel()
        model.close()
    }
}
