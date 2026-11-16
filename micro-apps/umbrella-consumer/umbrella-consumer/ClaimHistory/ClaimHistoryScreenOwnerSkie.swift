import Foundation
@preconcurrency import HedvigShared
import Observation

/// What the claim history screen shows, mirrored from Kotlin's `ClaimHistoryScreenState`.
enum ClaimHistoryViewState {
    case loading
    case empty
    case failed
    case content([ClaimHistoryRow])

    var logDescription: String {
        switch self {
        case .loading: "loading"
        case .empty: "empty"
        case .failed: "failed"
        case let .content(rows):
            "content(\(rows.count) items, \(rows.filter(\.isPartnerClaim).count) partner)"
        }
    }
}

/// One claim, copied out of Kotlin's `ClaimHistoryItem`. Kotlin compares those by identity, so lists diff by `id`.
/// Regular and partner claims come from separate id spaces, so `id` carries the kind and `claimId` the backend id.
struct ClaimHistoryRow: Identifiable, Equatable {
    enum Outcome: Equatable {
        case paid
        case notCompensated
        case notCovered
        case unresponsive
        case unknown
    }

    let id: String
    let claimId: String
    let claimType: String?
    let submittedAt: Date?
    let outcome: Outcome
    let isPartnerClaim: Bool
}

/// Owns one Kotlin `ClaimHistoryScreenModel` for as long as the screen exists: seeds from the `states` flow's value,
/// iterates the flow in a main-actor `Task` that captures the owner weakly, and cancels the task and closes the model
/// on the main actor in `deinit`.
@MainActor
@Observable
final class ClaimHistoryScreenOwner {
    private(set) var state: ClaimHistoryViewState

    @ObservationIgnored private let model: ClaimHistoryScreenModel
    @ObservationIgnored private var collection: Task<Void, Never>?

    init(holder: IosGraphHolder) {
        let model = holder.claimHistoryScreenModel()
        self.model = model
        let states = model.states
        state = ClaimHistoryViewState(states.value)
        ShellLog.info("ClaimHistoryScreenOwner seeded: \(state.logDescription)")
        collection = Task { [weak self] in
            for await kotlinState in states {
                #if DEBUG
                dispatchPrecondition(condition: .onQueue(.main))
                #endif
                self?.receive(kotlinState)
            }
        }
    }

    func reload() {
        ShellLog.info("ClaimHistoryScreenOwner reload")
        model.reload()
    }

    private func receive(_ kotlinState: ClaimHistoryScreenState) {
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

private extension ClaimHistoryViewState {
    init(_ kotlinState: ClaimHistoryScreenState) {
        switch onEnum(of: kotlinState) {
        case let .content(content):
            self = .content(content.items.map(ClaimHistoryRow.init))
        case .empty:
            self = .empty
        case .failed:
            self = .failed
        case .loading:
            self = .loading
        }
    }
}

private extension ClaimHistoryRow {
    init(_ item: ClaimHistoryItem) {
        id = item.isPartnerClaim ? "partner:\(item.id)" : "claim:\(item.id)"
        claimId = item.id
        claimType = item.claimType
        submittedAt = item.submittedAt
        isPartnerClaim = item.isPartnerClaim
        outcome = switch item.outcome {
        case .paid: .paid
        case .notCompensated: .notCompensated
        case .notCovered: .notCovered
        case .unresponsive: .unresponsive
        case .unknown: .unknown
        }
    }
}
