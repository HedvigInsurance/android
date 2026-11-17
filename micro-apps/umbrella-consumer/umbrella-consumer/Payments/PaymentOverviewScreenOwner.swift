import Foundation
@preconcurrency import HedvigShared

typealias PaymentOverviewScreenOwner = ScreenOwner<PaymentOverviewScreenModel, PaymentOverviewViewState>

extension PaymentOverviewScreenOwner {
    func retry() {
        ShellLog.info("PaymentOverviewScreenOwner retry")
        model.retry()
    }
}

/// What the payments overview shows, mirrored from Kotlin's `PaymentOverviewScreenState`.
enum PaymentOverviewViewState: ScreenViewState {
    case loading
    case failed
    case content(PaymentOverviewContent)

    init(_ kotlinState: PaymentOverviewScreenState) {
        self = oneOf { pick in
            kotlinState.handle(
                onLoading: { pick(.loading) },
                onFailed: { pick(.failed) },
                onContent: { pick(.content(PaymentOverviewContent($0))) }
            )
        }
    }

    var logDescription: String {
        switch self {
        case .loading: "loading"
        case .failed: "failed"
        case let .content(content):
            "content(upcoming \(content.upcomingCharge != nil), \(content.ongoingCharges.count) ongoing)"
        }
    }
}

struct PaymentOverviewContent: Equatable {
    /// A charge on the overview. `chargeId` is the backend id; `id` is its position, for lists.
    struct Charge: Identifiable, Equatable {
        let id: Int
        let chargeId: String?
        let dueDate: ShellDay
        let netAmount: ShellMoney
    }

    enum Notice: Equatable {
        case noNotice
        case inProgress
        case chargeFailed(failedPeriodFrom: ShellDay, failedPeriodTo: ShellDay, manualChargeAmountDue: ShellMoney?)
    }

    enum ConnectionStatus: Equatable {
        case active
        case pending
        case needsPayinSetup(dueDateToConnect: ShellDay?)
        case needsPayoutSetup
        case unknown
    }

    struct PrimaryPayinMethod: Equatable {
        let method: ShellChargeMethod
        let isPending: Bool
    }

    enum MemberType: Equatable {
        case qasaOnly
        case standard
        case standardToQasa
    }

    let upcomingCharge: Charge?
    let upcomingChargeNotice: Notice
    let ongoingCharges: [Charge]
    let connectionStatus: ConnectionStatus
    let primaryPayinMethod: PrimaryPayinMethod?
    let memberType: MemberType
    let showsRetryChargeNotice: Bool
    let anyPayinMethodIsPending: Bool
}

private extension PaymentOverviewContent {
    init(_ content: PaymentOverviewScreenState.Content) {
        upcomingCharge = content.upcomingCharge.map { Charge($0, index: 0) }
        upcomingChargeNotice = Notice(content.upcomingChargeNotice)
        ongoingCharges = content.ongoingCharges.enumerated().map { Charge($1, index: $0) }
        connectionStatus = ConnectionStatus(content.connectionStatus)
        primaryPayinMethod = content.primaryPayinMethod.map {
            PrimaryPayinMethod(method: ShellChargeMethod($0.method), isPending: $0.isPending)
        }
        memberType = MemberType(content.memberType)
        showsRetryChargeNotice = content.showsRetryChargeNotice
        anyPayinMethodIsPending = content.anyPayinMethodIsPending
    }
}

private extension PaymentOverviewContent.Charge {
    init(_ charge: PaymentOverviewCharge, index: Int) {
        id = index
        chargeId = charge.id
        dueDate = ShellDay(charge.dueDate)
        netAmount = ShellMoney(charge.netAmount)
    }
}

private extension PaymentOverviewContent.Notice {
    init(_ notice: PaymentUpcomingChargeNotice) {
        self = oneOf { pick in
            notice.handle(
                onNoNotice: { pick(.noNotice) },
                onInProgress: { pick(.inProgress) },
                onChargeFailed: {
                    pick(
                        .chargeFailed(
                            failedPeriodFrom: ShellDay($0.failedPeriodFromDate),
                            failedPeriodTo: ShellDay($0.failedPeriodToDate),
                            manualChargeAmountDue: $0.manualChargeAmountDue.map(ShellMoney.init)
                        )
                    )
                }
            )
        }
    }
}

private extension PaymentOverviewContent.ConnectionStatus {
    init(_ status: PaymentConnectionStatus) {
        self = oneOf { pick in
            status.handle(
                onActive: { pick(.active) },
                onPending: { pick(.pending) },
                onNeedsPayinSetup: { pick(.needsPayinSetup(dueDateToConnect: $0.dueDateToConnect.map(ShellDay.init))) },
                onNeedsPayoutSetup: { pick(.needsPayoutSetup) },
                onUnknown: { pick(.unknown) }
            )
        }
    }
}

private extension PaymentOverviewContent.MemberType {
    init(_ memberType: PaymentMemberType) {
        self = oneOf { pick in
            memberType.handle(
                onQasaOnly: { pick(.qasaOnly) },
                onStandard: { pick(.standard) },
                onStandardToQasa: { pick(.standardToQasa) }
            )
        }
    }
}
