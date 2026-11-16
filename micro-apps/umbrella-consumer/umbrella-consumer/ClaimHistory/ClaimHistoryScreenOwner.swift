import Foundation
@preconcurrency import HedvigShared

typealias ClaimHistoryScreenOwner = ScreenOwner<ClaimHistoryScreenModel, ClaimHistoryViewState>

extension ClaimHistoryScreenOwner {
    func reload() {
        ShellLog.info("ClaimHistoryScreenOwner reload")
        model.reload()
    }
}

/// What the claim history screen shows, mirrored from Kotlin's `ClaimHistoryScreenState`.
enum ClaimHistoryViewState: ScreenViewState {
    case loading
    case empty
    case failed
    case content([ClaimHistoryRow])

    init(_ kotlinState: ClaimHistoryScreenState) {
        self = oneOf { pick in
            kotlinState.handle(
                onLoading: { pick(.loading) },
                onEmpty: { pick(.empty) },
                onFailed: { pick(.failed) },
                onContent: { pick(.content($0.items.map(ClaimHistoryRow.init))) }
            )
        }
    }

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

private extension ClaimHistoryRow {
    init(_ item: ClaimHistoryItem) {
        id = item.isPartnerClaim ? "partner:\(item.id)" : "claim:\(item.id)"
        claimId = item.id
        claimType = item.claimType
        submittedAt = item.submittedAt
        isPartnerClaim = item.isPartnerClaim
        outcome = oneOf { pick in
            item.outcome.handle(
                onPaid: { pick(.paid) },
                onNotCompensated: { pick(.notCompensated) },
                onNotCovered: { pick(.notCovered) },
                onUnresponsive: { pick(.unresponsive) },
                onUnknown: { pick(.unknown) }
            )
        }
    }
}
