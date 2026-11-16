@preconcurrency import HedvigShared
import SwiftUI

/// The claim history screen. Its owner, and with it the Kotlin screen model, is created when the screen appears, not
/// when SwiftUI builds the view value, and lives as long as the screen's state.
struct ClaimHistoryView: View {
    let holder: IosGraphHolder
    @State private var owner: ClaimHistoryScreenOwner?

    var body: some View {
        Group {
            if let owner {
                ClaimHistoryContent(owner: owner)
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Claim history")
        .toolbar {
            Button("Reload") {
                owner?.reload()
            }
            .disabled(owner == nil)
        }
        .onAppear {
            if owner == nil {
                owner = ClaimHistoryScreenOwner(holder: holder)
            }
        }
        #if DEBUG
        .onDisappear {
            let reference = WeakReference(owner)
            Task {
                try? await Task.sleep(for: .seconds(1))
                ShellLog.info("ClaimHistoryScreenOwner released: \(reference.value == nil)")
            }
        }
        #endif
    }
}

private struct ClaimHistoryContent: View {
    let owner: ClaimHistoryScreenOwner

    var body: some View {
        switch owner.state {
        case .loading:
            ProgressView()
        case .empty:
            ContentUnavailableView("No claims", systemImage: "tray")
        case .failed:
            VStack(spacing: 16) {
                Text("Could not load your claims")
                Button("Retry") {
                    owner.reload()
                }
                .buttonStyle(.bordered)
            }
        case let .content(rows):
            List(rows) { row in
                ClaimHistoryRowView(row: row)
            }
        }
    }
}

private struct ClaimHistoryRowView: View {
    let row: ClaimHistoryRow

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(row.claimType ?? "Claim")
                    .font(.headline)
                if row.isPartnerClaim {
                    Text("Partner")
                        .font(.caption)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(.secondary.opacity(0.2), in: Capsule())
                }
            }
            HStack {
                if let submittedAt = row.submittedAt {
                    Text(submittedAt, format: .dateTime.year().month().day())
                } else {
                    Text("No date")
                }
                Spacer()
                Text(row.outcome.label)
            }
            .font(.subheadline)
            .foregroundStyle(.secondary)
        }
    }
}

private extension ClaimHistoryRow.Outcome {
    var label: String {
        switch self {
        case .paid: "Paid"
        case .notCompensated: "Not compensated"
        case .notCovered: "Not covered"
        case .unresponsive: "Unresponsive"
        case .unknown: "Unknown"
        }
    }
}
