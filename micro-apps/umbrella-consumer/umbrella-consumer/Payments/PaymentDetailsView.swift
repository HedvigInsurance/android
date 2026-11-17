@preconcurrency import HedvigShared
import SwiftUI

/// One charge's details. Its owner, and with it the Kotlin screen model for `chargeId`, is created when the screen
/// appears and lives as long as the screen's state.
struct PaymentDetailsView: View {
    let holder: IosGraphHolder
    let chargeId: String?
    @State private var owner: PaymentDetailsScreenOwner?

    var body: some View {
        Group {
            if let owner {
                PaymentDetailsContentView(state: owner.state, holder: holder, reload: owner.reload)
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Payment details")
        .toolbar {
            Button("Reload") {
                owner?.reload()
            }
            .disabled(owner == nil)
        }
        .onAppear {
            if owner == nil {
                owner = PaymentDetailsScreenOwner(holder.paymentDetailsScreenModel(chargeId: chargeId))
            }
        }
    }
}

struct PaymentDetailsContentView: View {
    let state: PaymentDetailsViewState
    /// Builds the next charge's screen; nil in previews, where the link is left out.
    let holder: IosGraphHolder?
    let reload: @MainActor () -> Void

    var body: some View {
        switch state {
        case .loading:
            ProgressView()
        case .failed:
            VStack(spacing: 16) {
                Text("Could not load this payment")
                Button("Retry") {
                    reload()
                }
                .buttonStyle(.bordered)
            }
        case let .content(content):
            List {
                summarySection(content)
                if content.breakdowns.isEmpty {
                    Section("Breakdown") {
                        Text("No contracts in this charge")
                    }
                }
                ForEach(content.breakdowns) { breakdown in
                    breakdownSection(breakdown)
                }
                adjustmentsSection(content)
                methodSection(content)
            }
        }
    }

    @ViewBuilder
    private func summarySection(_ content: PaymentDetailsContent) -> some View {
        Section("Charge") {
            LabeledContent("Status", value: content.status.label)
            LabeledContent("Due date", value: content.dueDate.formatted)
            LabeledContent("Gross", value: content.grossAmount.formatted)
            LabeledContent("Net", value: content.netAmount.formatted)
            if content.status == .failed {
                if let nextCharge = content.nextCharge {
                    Text("This amount is added to the charge due \(nextCharge.dueDate.formatted)")
                    if let holder, let nextChargeId = nextCharge.chargeId {
                        NavigationLink("Open that charge") {
                            PaymentDetailsView(holder: holder, chargeId: nextChargeId)
                        }
                    } else if nextCharge.chargeId == nil {
                        Text("That charge has no id and cannot be opened")
                            .foregroundStyle(.secondary)
                    }
                } else {
                    Text("No next charge")
                }
            }
        }
    }

    private func breakdownSection(_ breakdown: PaymentDetailsContent.Breakdown) -> some View {
        Section(breakdown.contractDisplayName) {
            Text(breakdown.contractDetails)
                .foregroundStyle(.secondary)
            LabeledContent("Gross", value: breakdown.grossAmount.formatted)
            LabeledContent("Net", value: breakdown.netAmount.formatted)
            ForEach(breakdown.periods) { period in
                VStack(alignment: .leading, spacing: 2) {
                    HStack {
                        Text("\(period.fromDate.formatted) to \(period.toDate.formatted)")
                        Spacer()
                        Text(period.amount.formatted)
                    }
                    if let description = period.periodDescription {
                        Text(description.label)
                            .font(.caption)
                    }
                    if period.isPreviouslyFailedCharge {
                        Text("Previously failed charge")
                            .font(.caption)
                            .foregroundStyle(.red)
                    }
                }
            }
            ForEach(breakdown.priceItems) { item in
                LabeledContent(item.displayTitle, value: item.amount.formatted)
            }
        }
    }

    @ViewBuilder
    private func adjustmentsSection(_ content: PaymentDetailsContent) -> some View {
        Section("Adjustments and discounts") {
            LabeledContent("Carried adjustment", value: content.carriedAdjustment?.formatted ?? "None")
            LabeledContent("Settlement adjustment", value: content.settlementAdjustment?.formatted ?? "None")
            if let referral = content.referralDiscount {
                LabeledContent("Referral code", value: referral.code)
                LabeledContent("Referral discount", value: referral.amount?.formatted ?? "No amount")
            } else {
                LabeledContent("Referral discount", value: "None")
            }
        }
    }

    @ViewBuilder
    private func methodSection(_ content: PaymentDetailsContent) -> some View {
        Section("Payment method") {
            LabeledContent("Charged with", value: content.chargeMethod.label)
            switch content.payinAccount {
            case nil:
                LabeledContent("Account", value: "Not shown for this charge")
            case let .trustly(bankName, clearingNumber, accountNumber)?:
                LabeledContent("Bank", value: bankName ?? "No bank name")
                LabeledContent(
                    "Account",
                    value: maskedAccount(clearingNumber: clearingNumber, accountNumber: accountNumber) ?? "No account"
                )
            case let .swish(phoneNumber)?:
                LabeledContent("Swish number", value: phoneNumber ?? "No number")
            case let .invoice(delivery, email)?:
                LabeledContent("Invoice delivery", value: delivery?.label ?? "No delivery")
                LabeledContent("Email", value: email ?? "No email")
            }
        }
    }

    /// The last four digits of clearing and account number together, or nil when there are 8 digits or fewer.
    private func maskedAccount(clearingNumber: String?, accountNumber: String?) -> String? {
        let whole = [clearingNumber, accountNumber]
            .compactMap { $0?.trimmingCharacters(in: .whitespaces) }
            .joined()
        guard whole.count > 8 else { return nil }
        return "**** \(whole.suffix(4))"
    }
}

private extension PaymentDetailsContent.PeriodDescription {
    var label: String {
        switch self {
        case .fullPeriod: "Full period"
        case let .betweenDays(days): "\(days) days"
        }
    }
}

private extension PaymentDetailsContent.InvoiceDelivery {
    var label: String {
        switch self {
        case .kivra: "Kivra"
        case .mail: "Email"
        }
    }
}

#Preview("Loading") {
    PaymentDetailsContentView(state: .loading, holder: nil, reload: {})
}

#Preview("Failed") {
    PaymentDetailsContentView(state: .failed, holder: nil, reload: {})
}

#Preview("Failed charge") {
    PaymentDetailsContentView(
        state: .content(
            PaymentDetailsContent(
                chargeId: "charge-2",
                status: .failed,
                dueDate: ShellDay("2026-10-27"),
                grossAmount: ShellMoney(amount: 399, currencyCode: "SEK"),
                netAmount: ShellMoney(amount: 349, currencyCode: "SEK"),
                breakdowns: [
                    .init(
                        id: 0, contractDisplayName: "Home insurance", contractDetails: "Example street 1",
                        grossAmount: ShellMoney(amount: 399, currencyCode: "SEK"),
                        netAmount: ShellMoney(amount: 349, currencyCode: "SEK"),
                        periods: [
                            .init(
                                id: 0, amount: ShellMoney(amount: 399, currencyCode: "SEK"),
                                fromDate: ShellDay("2026-10-01"), toDate: ShellDay("2026-10-31"),
                                isPreviouslyFailedCharge: false, periodDescription: .fullPeriod
                            ),
                        ],
                        priceItems: [
                            .init(id: 0, displayTitle: "Bundle discount", amount: ShellMoney(amount: -50, currencyCode: "SEK")),
                        ]
                    ),
                ],
                carriedAdjustment: nil,
                settlementAdjustment: nil,
                referralDiscount: nil,
                chargeMethod: .trustly,
                payinAccount: nil,
                nextCharge: .init(chargeId: nil, dueDate: ShellDay("2026-11-27"))
            )
        ),
        holder: nil,
        reload: {}
    )
}
