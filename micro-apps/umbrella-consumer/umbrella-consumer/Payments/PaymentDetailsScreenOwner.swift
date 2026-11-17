import Foundation
@preconcurrency import HedvigShared

typealias PaymentDetailsScreenOwner = ScreenOwner<PaymentDetailsScreenModel, PaymentDetailsViewState>

extension PaymentDetailsScreenOwner {
    func reload() {
        ShellLog.info("PaymentDetailsScreenOwner reload")
        model.reload()
    }
}

/// What the payment details show, mirrored from Kotlin's `PaymentDetailsScreenState`.
enum PaymentDetailsViewState: ScreenViewState {
    case loading
    case failed
    case content(PaymentDetailsContent)

    init(_ kotlinState: PaymentDetailsScreenState) {
        self = oneOf { pick in
            kotlinState.handle(
                onLoading: { pick(.loading) },
                onFailed: { pick(.failed) },
                onContent: { pick(.content(PaymentDetailsContent($0))) }
            )
        }
    }

    var logDescription: String {
        switch self {
        case .loading: "loading"
        case .failed: "failed"
        case let .content(content):
            "content(\(content.breakdowns.count) breakdowns, account \(content.payinAccount != nil), next charge \(content.nextCharge != nil))"
        }
    }
}

struct PaymentDetailsContent: Equatable {
    /// One contract's part of the charge. `id` is its position, for lists.
    struct Breakdown: Identifiable, Equatable {
        let id: Int
        let contractDisplayName: String
        let contractDetails: String
        let grossAmount: ShellMoney
        let netAmount: ShellMoney
        let periods: [Period]
        let priceItems: [PriceItem]
    }

    struct Period: Identifiable, Equatable {
        let id: Int
        let amount: ShellMoney
        let fromDate: ShellDay
        let toDate: ShellDay
        let isPreviouslyFailedCharge: Bool
        let periodDescription: PeriodDescription?
    }

    enum PeriodDescription: Equatable {
        case fullPeriod
        case betweenDays(Int)
    }

    struct PriceItem: Identifiable, Equatable {
        let id: Int
        let displayTitle: String
        let amount: ShellMoney
    }

    struct ReferralDiscount: Equatable {
        let code: String
        let amount: ShellMoney?
    }

    enum PayinAccount: Equatable {
        case trustly(bankName: String?, clearingNumber: String?, accountNumber: String?)
        case swish(phoneNumber: String?)
        case invoice(delivery: InvoiceDelivery?, email: String?)
    }

    enum InvoiceDelivery: Equatable {
        case kivra
        case mail
    }

    /// The charge a failed charge's amount is added to. Without `chargeId` it cannot be opened.
    struct NextCharge: Equatable {
        let chargeId: String?
        let dueDate: ShellDay
    }

    let chargeId: String?
    let status: ShellChargeStatus
    let dueDate: ShellDay
    let grossAmount: ShellMoney
    let netAmount: ShellMoney
    let breakdowns: [Breakdown]
    let carriedAdjustment: ShellMoney?
    let settlementAdjustment: ShellMoney?
    let referralDiscount: ReferralDiscount?
    let chargeMethod: ShellChargeMethod
    let payinAccount: PayinAccount?
    let nextCharge: NextCharge?
}

private extension PaymentDetailsContent {
    init(_ content: PaymentDetailsScreenState.Content) {
        chargeId = content.id
        status = ShellChargeStatus(content.status)
        dueDate = ShellDay(content.dueDate)
        grossAmount = ShellMoney(content.grossAmount)
        netAmount = ShellMoney(content.netAmount)
        breakdowns = content.chargeBreakdowns.enumerated().map { Breakdown($1, index: $0) }
        carriedAdjustment = content.carriedAdjustment.map(ShellMoney.init)
        settlementAdjustment = content.settlementAdjustment.map(ShellMoney.init)
        referralDiscount = content.referralDiscount.map {
            ReferralDiscount(code: $0.code, amount: $0.amount.map(ShellMoney.init))
        }
        chargeMethod = ShellChargeMethod(content.chargeMethod)
        payinAccount = content.payinAccount.map(PayinAccount.init)
        nextCharge = content.nextChargeDueDate.map {
            NextCharge(chargeId: content.nextChargeId, dueDate: ShellDay($0))
        }
    }
}

private extension PaymentDetailsContent.Breakdown {
    init(_ breakdown: PaymentChargeBreakdown, index: Int) {
        id = index
        contractDisplayName = breakdown.contractDisplayName
        contractDetails = breakdown.contractDetails
        grossAmount = ShellMoney(breakdown.grossAmount)
        netAmount = ShellMoney(breakdown.netAmount)
        periods = breakdown.periods.enumerated().map { index, period in
            PaymentDetailsContent.Period(
                id: index,
                amount: ShellMoney(period.amount),
                fromDate: ShellDay(period.fromDate),
                toDate: ShellDay(period.toDate),
                isPreviouslyFailedCharge: period.isPreviouslyFailedCharge,
                periodDescription: period.periodDescription.map(PaymentDetailsContent.PeriodDescription.init)
            )
        }
        priceItems = breakdown.priceBreakdown.enumerated().map { index, item in
            PaymentDetailsContent.PriceItem(id: index, displayTitle: item.displayTitle, amount: ShellMoney(item.amount))
        }
    }
}

private extension PaymentDetailsContent.PeriodDescription {
    init(_ description: PaymentPeriodDescription) {
        self = oneOf { pick in
            description.handle(
                onFullPeriod: { pick(.fullPeriod) },
                onBetweenDays: { pick(.betweenDays(Int($0.daysBetween))) }
            )
        }
    }
}

private extension PaymentDetailsContent.PayinAccount {
    init(_ account: PaymentPayinAccount) {
        self = oneOf { pick in
            account.handle(
                onTrustly: {
                    pick(.trustly(bankName: $0.bankName, clearingNumber: $0.clearingNumber, accountNumber: $0.accountNumber))
                },
                onSwish: { pick(.swish(phoneNumber: $0.phoneNumber)) },
                onInvoice: {
                    pick(.invoice(delivery: $0.delivery.map(PaymentDetailsContent.InvoiceDelivery.init), email: $0.email))
                }
            )
        }
    }
}

private extension PaymentDetailsContent.InvoiceDelivery {
    init(_ delivery: PaymentInvoiceDelivery) {
        self = oneOf { pick in
            delivery.handle(
                onKivra: { pick(.kivra) },
                onMail: { pick(.mail) }
            )
        }
    }
}
