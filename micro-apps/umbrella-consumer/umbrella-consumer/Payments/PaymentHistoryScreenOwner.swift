import Foundation
@preconcurrency import HedvigShared

typealias PaymentHistoryScreenOwner = ScreenOwner<PaymentHistoryScreenModel, PaymentHistoryViewState>

extension PaymentHistoryScreenOwner {
    func reload() {
        ShellLog.info("PaymentHistoryScreenOwner reload")
        model.reload()
    }
}

/// What the payment history shows, mirrored from Kotlin's `PaymentHistoryScreenState`.
enum PaymentHistoryViewState: ScreenViewState {
    case loading
    case empty
    case failed
    case content(charges: [PaymentHistoryRow], showsOlderChargesNotice: Bool)

    init(_ kotlinState: PaymentHistoryScreenState) {
        self = oneOf { pick in
            kotlinState.handle(
                onLoading: { pick(.loading) },
                onEmpty: { pick(.empty) },
                onFailed: { pick(.failed) },
                onContent: { content in
                    pick(
                        .content(
                            charges: content.charges.enumerated().map { PaymentHistoryRow($1, index: $0) },
                            showsOlderChargesNotice: content.showsOlderChargesNotice
                        )
                    )
                }
            )
        }
    }

    var logDescription: String {
        switch self {
        case .loading: "loading"
        case .empty: "empty"
        case .failed: "failed"
        case let .content(charges, showsOlderChargesNotice):
            "content(\(charges.count) charges, \(charges.filter { $0.chargeId == nil }.count) without id, notice \(showsOlderChargesNotice))"
        }
    }
}

/// One past charge. `chargeId` is the backend id, nil when the charge cannot be opened; `id` is its position, for lists.
struct PaymentHistoryRow: Identifiable, Equatable {
    let id: Int
    let chargeId: String?
    let dueDate: ShellDay
    let netAmount: ShellMoney
    let status: ShellChargeStatus
}

private extension PaymentHistoryRow {
    init(_ charge: PaymentHistoryCharge, index: Int) {
        id = index
        chargeId = charge.id
        dueDate = ShellDay(charge.dueDate)
        netAmount = ShellMoney(charge.netAmount)
        status = ShellChargeStatus(charge.status)
    }
}
