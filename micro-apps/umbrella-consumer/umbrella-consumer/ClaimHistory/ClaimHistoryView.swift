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
                ClaimHistoryContent(state: owner.state, reload: owner.reload)
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
                owner = ClaimHistoryScreenOwner(holder.claimHistoryScreenModel())
            }
        }
    }
}

struct ClaimHistoryContent: View {
    let state: ClaimHistoryViewState
    let reload: @MainActor () -> Void

    var body: some View {
        switch state {
        case .loading:
            ProgressView()
        case .empty:
            ContentUnavailableView("No claims", systemImage: "tray")
        case .failed:
            VStack(spacing: 16) {
                Text("Could not load your claims")
                Button("Retry") {
                    reload()
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

#if DEBUG
/// Renders a fixed state with the real screen's content view, for checking how Kotlin content crosses the bridge.
struct ClaimHistorySampleView: View {
    let state: ClaimHistoryViewState

    var body: some View {
        ClaimHistoryContent(state: state, reload: {})
            .navigationTitle("Sample content")
            .onAppear {
                ShellDebug.logSample(state)
            }
    }
}
#endif

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

#Preview("Loading") {
    ClaimHistoryContent(state: .loading, reload: {})
}

#Preview("Empty") {
    ClaimHistoryContent(state: .empty, reload: {})
}

#Preview("Failed") {
    ClaimHistoryContent(state: .failed, reload: {})
}

#Preview("Content") {
    ClaimHistoryContent(
        state: .content([
            ClaimHistoryRow(
                id: "claim:1001", claimId: "1001", claimType: "Water damage", submittedAt: .now, outcome: .paid,
                isPartnerClaim: false
            ),
            ClaimHistoryRow(
                id: "partner:1001", claimId: "1001", claimType: nil, submittedAt: nil, outcome: .unknown,
                isPartnerClaim: true
            ),
        ]),
        reload: {}
    )
}
