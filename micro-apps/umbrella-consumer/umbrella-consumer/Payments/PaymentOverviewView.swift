@preconcurrency import HedvigShared
import SwiftUI

/// The payments overview. Its owner, and with it the Kotlin screen model, is created when the screen appears and lives
/// as long as the screen's state.
struct PaymentOverviewView: View {
    let holder: IosGraphHolder
    @State private var owner: PaymentOverviewScreenOwner?

    var body: some View {
        Group {
            if let owner {
                PaymentOverviewContentView(state: owner.state, holder: holder, retry: owner.retry)
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Payments")
        .toolbar {
            Button("Retry") {
                owner?.retry()
            }
            .disabled(owner == nil)
        }
        .onAppear {
            if owner == nil {
                owner = PaymentOverviewScreenOwner(holder.paymentOverviewScreenModel())
            }
        }
    }
}

struct PaymentOverviewContentView: View {
    let state: PaymentOverviewViewState
    /// Builds the screens the overview links to; nil in previews, where the links are left out.
    let holder: IosGraphHolder?
    let retry: @MainActor () -> Void

    var body: some View {
        switch state {
        case .loading:
            ProgressView()
        case .failed:
            VStack(spacing: 16) {
                Text("Could not load your payments")
                Button("Retry") {
                    retry()
                }
                .buttonStyle(.bordered)
            }
        case let .content(content):
            List {
                chargesSection(content)
                noticesSection(content)
                connectionSection(content)
                Section("Member") {
                    LabeledContent("Member type", value: content.memberType.label)
                }
                Section {
                    link("Payment history") { holder in
                        PaymentHistoryView(holder: holder)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func chargesSection(_ content: PaymentOverviewContent) -> some View {
        Section("Upcoming charge") {
            if let upcoming = content.upcomingCharge {
                // The upcoming charge opens even without an id: Kotlin then selects the upcoming charge without one.
                link(chargeTitle(upcoming)) { holder in
                    PaymentDetailsView(holder: holder, chargeId: upcoming.chargeId)
                }
            } else {
                Text("No upcoming charge")
            }
        }
        Section("Ongoing charges") {
            if content.ongoingCharges.isEmpty {
                Text("No ongoing charges")
            }
            ForEach(content.ongoingCharges) { charge in
                if let chargeId = charge.chargeId {
                    link(chargeTitle(charge)) { holder in
                        PaymentDetailsView(holder: holder, chargeId: chargeId)
                    }
                } else {
                    Text(chargeTitle(charge) + " (no id, cannot open)")
                }
            }
        }
        if content.upcomingCharge == nil, content.ongoingCharges.isEmpty, content.memberType != .qasaOnly {
            Section {
                ContentUnavailableView("No payments in progress", systemImage: "creditcard")
            }
        }
    }

    @ViewBuilder
    private func noticesSection(_ content: PaymentOverviewContent) -> some View {
        Section("Notices") {
            switch content.upcomingChargeNotice {
            case .noNotice:
                LabeledContent("Upcoming charge notice", value: "None")
            case .inProgress:
                Text("A payment is in progress")
            case let .chargeFailed(from, to, amountDue):
                VStack(alignment: .leading, spacing: 4) {
                    Text("Payment failed for \(from.formatted) to \(to.formatted)")
                    if let amountDue {
                        Text("Manual charge due: \(amountDue.formatted)")
                        Text("Paying is not available in this app")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    } else {
                        Text("No manual charge offered; the amount is added to the next charge")
                    }
                }
            }
            LabeledContent("Retry charge notice", value: content.showsRetryChargeNotice ? "Shown" : "Not shown")
            LabeledContent("Any payin method pending", value: content.anyPayinMethodIsPending ? "Yes" : "No")
        }
    }

    @ViewBuilder
    private func connectionSection(_ content: PaymentOverviewContent) -> some View {
        Section("Payment method") {
            switch content.connectionStatus {
            case .active:
                LabeledContent("Connection", value: "Active")
            case .pending:
                LabeledContent("Connection", value: "Pending")
            case let .needsPayinSetup(dueDateToConnect):
                LabeledContent("Connection", value: "Payin method missing")
                LabeledContent("Connect by", value: dueDateToConnect?.formatted ?? "No date")
            case .needsPayoutSetup:
                LabeledContent("Connection", value: "Payout method missing")
            case .unknown:
                LabeledContent("Connection", value: "Unknown")
            }
            if let primary = content.primaryPayinMethod {
                LabeledContent("Primary payin method", value: primary.method.label)
                LabeledContent("Primary method pending", value: primary.isPending ? "Yes" : "No")
            } else {
                LabeledContent("Primary payin method", value: "None")
            }
        }
    }

    private func chargeTitle(_ charge: PaymentOverviewContent.Charge) -> String {
        "\(charge.netAmount.formatted), due \(charge.dueDate.formatted)"
    }

    @ViewBuilder
    private func link(_ title: String, @ViewBuilder destination: @escaping (IosGraphHolder) -> some View) -> some View {
        if let holder {
            NavigationLink(title) {
                destination(holder)
            }
        } else {
            Text(title)
        }
    }
}

private extension PaymentOverviewContent.MemberType {
    var label: String {
        switch self {
        case .qasaOnly: "Qasa only"
        case .standard: "Standard"
        case .standardToQasa: "Standard to Qasa"
        }
    }
}

#Preview("Loading") {
    PaymentOverviewContentView(state: .loading, holder: nil, retry: {})
}

#Preview("Failed") {
    PaymentOverviewContentView(state: .failed, holder: nil, retry: {})
}

#Preview("Content") {
    PaymentOverviewContentView(
        state: .content(
            PaymentOverviewContent(
                upcomingCharge: .init(
                    id: 0, chargeId: "charge-1", dueDate: ShellDay("2026-12-27"),
                    netAmount: ShellMoney(amount: 349, currencyCode: "SEK")
                ),
                upcomingChargeNotice: .chargeFailed(
                    failedPeriodFrom: ShellDay("2026-10-01"), failedPeriodTo: ShellDay("2026-10-31"),
                    manualChargeAmountDue: ShellMoney(amount: 349, currencyCode: "SEK")
                ),
                ongoingCharges: [],
                connectionStatus: .needsPayinSetup(dueDateToConnect: ShellDay("2026-12-20")),
                primaryPayinMethod: nil,
                memberType: .standard,
                showsRetryChargeNotice: false,
                anyPayinMethodIsPending: false
            )
        ),
        holder: nil,
        retry: {}
    )
}
