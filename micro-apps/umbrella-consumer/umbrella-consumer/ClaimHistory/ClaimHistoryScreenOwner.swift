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

/// Owns one Kotlin `ClaimHistoryScreenModel` for as long as the screen exists, following the model's rules: seed from
/// `currentState`, observe with a weak capture, and cancel and close on the main actor in `deinit`.
@MainActor
@Observable
final class ClaimHistoryScreenOwner {
    private(set) var state: ClaimHistoryViewState

    @ObservationIgnored private let model: ClaimHistoryScreenModel
    @ObservationIgnored private var subscription: ClaimHistorySubscription?

    init(holder: IosGraphHolder) {
        let model = holder.claimHistoryScreenModel()
        self.model = model
        state = ClaimHistoryViewState(model.currentState)
        ShellLog.info("ClaimHistoryScreenOwner seeded: \(state.logDescription)")
        subscription = model.observe { [weak self] kotlinState in
            #if DEBUG
            dispatchPrecondition(condition: .onQueue(.main))
            #endif
            MainActor.assumeIsolated {
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
        subscription?.cancel()
        model.close()
    }
}

#if DEBUG
extension ClaimHistoryViewState {
    /// Converts a Kotlin state the same way the owner converts a delivered one.
    static func mapped(_ kotlinState: ClaimHistoryScreenState) -> ClaimHistoryViewState {
        ClaimHistoryViewState(kotlinState)
    }
}
#endif

private extension ClaimHistoryViewState {
    init(_ kotlinState: ClaimHistoryScreenState) {
        switch kotlinState {
        case let content as ClaimHistoryScreenState.Content:
            self = .content(content.items.map(ClaimHistoryRow.init))
        case is ClaimHistoryScreenState.Empty:
            self = .empty
        case is ClaimHistoryScreenState.Failed:
            self = .failed
        case is ClaimHistoryScreenState.Loading:
            self = .loading
        default:
            ShellLog.error("Unknown ClaimHistoryScreenState \(type(of: kotlinState)), showing failed")
            self = .failed
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
        default: .unknown
        }
    }
}
