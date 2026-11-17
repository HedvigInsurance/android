@preconcurrency import HedvigShared
import SwiftUI

/// The payment history. Its owner, and with it the Kotlin screen model, is created when the screen appears and lives
/// as long as the screen's state.
struct PaymentHistoryView: View {
    let holder: IosGraphHolder
    @State private var owner: PaymentHistoryScreenOwner?

    var body: some View {
        Group {
            if let owner {
                PaymentHistoryContentView(state: owner.state, holder: holder, reload: owner.reload)
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Payment history")
        .toolbar {
            Button("Reload") {
                owner?.reload()
            }
            .disabled(owner == nil)
        }
        .onAppear {
            if owner == nil {
                owner = PaymentHistoryScreenOwner(holder.paymentHistoryScreenModel())
            }
        }
    }
}

struct PaymentHistoryContentView: View {
    let state: PaymentHistoryViewState
    /// Builds the details screens; nil in previews, where the rows do not open.
    let holder: IosGraphHolder?
    let reload: @MainActor () -> Void

    var body: some View {
        switch state {
        case .loading:
            ProgressView()
        case .empty:
            ContentUnavailableView("No payment history", systemImage: "tray")
        case .failed:
            VStack(spacing: 16) {
                Text("Could not load your payment history")
                Button("Retry") {
                    reload()
                }
                .buttonStyle(.bordered)
            }
        case let .content(charges, showsOlderChargesNotice):
            List {
                ForEach(yearGroups(charges), id: \.title) { group in
                    Section(group.title) {
                        ForEach(group.rows) { row in
                            rowView(row)
                        }
                    }
                }
                if showsOlderChargesNotice {
                    Section {
                        Text("Older charges are not shown here")
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private func rowView(_ row: PaymentHistoryRow) -> some View {
        if let holder, let chargeId = row.chargeId {
            NavigationLink {
                PaymentDetailsView(holder: holder, chargeId: chargeId)
            } label: {
                PaymentHistoryRowView(row: row)
            }
        } else {
            PaymentHistoryRowView(row: row)
        }
    }

    /// Consecutive rows sharing a due date year, in the order Kotlin gives them.
    private func yearGroups(_ rows: [PaymentHistoryRow]) -> [(title: String, rows: [PaymentHistoryRow])] {
        var groups: [(title: String, rows: [PaymentHistoryRow])] = []
        for row in rows {
            let title = row.dueDate.year.map(String.init) ?? "Unknown year"
            if groups.last?.title == title {
                groups[groups.count - 1].rows.append(row)
            } else {
                groups.append((title: title, rows: [row]))
            }
        }
        return groups
    }
}

private struct PaymentHistoryRowView: View {
    let row: PaymentHistoryRow

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(row.dueDate.formatted)
                Spacer()
                Text(row.netAmount.formatted)
            }
            HStack {
                Text(row.status.label)
                    .foregroundStyle(row.status == .failed ? .red : .secondary)
                if row.chargeId == nil {
                    Spacer()
                    Text("No id, cannot open")
                        .foregroundStyle(.secondary)
                }
            }
            .font(.subheadline)
        }
    }
}

#Preview("Loading") {
    PaymentHistoryContentView(state: .loading, holder: nil, reload: {})
}

#Preview("Empty") {
    PaymentHistoryContentView(state: .empty, holder: nil, reload: {})
}

#Preview("Failed") {
    PaymentHistoryContentView(state: .failed, holder: nil, reload: {})
}

#Preview("Content") {
    PaymentHistoryContentView(
        state: .content(
            charges: [
                PaymentHistoryRow(
                    id: 0, chargeId: "charge-2", dueDate: ShellDay("2026-11-27"),
                    netAmount: ShellMoney(amount: 349, currencyCode: "SEK"), status: .failed
                ),
                PaymentHistoryRow(
                    id: 1, chargeId: nil, dueDate: ShellDay("2025-12-27"),
                    netAmount: ShellMoney(amount: 299, currencyCode: "SEK"), status: .success
                ),
            ],
            showsOlderChargesNotice: true
        ),
        holder: nil,
        reload: {}
    )
}
