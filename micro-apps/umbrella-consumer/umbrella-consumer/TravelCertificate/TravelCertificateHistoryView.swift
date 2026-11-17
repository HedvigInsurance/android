@preconcurrency import HedvigShared
import SwiftUI

/// The travel certificate history, the flow's first screen. Its owner keeps collecting while the create flow is on
/// top, so Kotlin never reloads the list by itself: it reloads each time the history is shown again, however the
/// create flow ended (created, backed out of, or torn down while creating).
struct TravelCertificateHistoryView: View {
    let holder: IosGraphHolder
    let flow: TravelCertificateFlow
    @State private var owner: TravelCertificateHistoryScreenOwner?

    var body: some View {
        Group {
            if let owner {
                TravelCertificateHistoryContentView(
                    state: owner.state,
                    reload: owner.reload,
                    create: { mustChooseContract in
                        flow.path.append(mustChooseContract ? .chooseContract : .dateInput(contractId: nil))
                    }
                )
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Travel certificate")
        .toolbar {
            Button("Reload") {
                owner?.reload()
            }
            .disabled(owner?.isCollecting != true)
        }
        .onAppear {
            if let owner {
                owner.reload()
            } else {
                owner = TravelCertificateHistoryScreenOwner(holder.travelCertificateHistoryScreenModel())
            }
        }
    }
}

struct TravelCertificateHistoryContentView: View {
    let state: TravelCertificateHistoryViewState
    let reload: @MainActor () -> Void
    /// Starts creating; true when a contract must be chosen first.
    let create: @MainActor (Bool) -> Void

    var body: some View {
        switch state {
        case .loading:
            ProgressView()
        case .failed:
            VStack(spacing: 16) {
                Text("Could not load your travel certificates")
                Button("Retry") {
                    reload()
                }
                .buttonStyle(.bordered)
            }
        case let .content(content):
            List {
                if let banner = content.addonBanner {
                    Section("Travel add-on") {
                        VStack(alignment: .leading, spacing: 6) {
                            Text(banner.title).font(.headline)
                            Text(banner.description).foregroundStyle(.secondary)
                            if !banner.labels.isEmpty {
                                Text(banner.labels.joined(separator: " · "))
                                    .font(.caption)
                            }
                            Text("Eligible contracts: \(banner.eligibleContractCount)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                Section("Certificates") {
                    if content.certificates.isEmpty {
                        Text("No travel certificates")
                            .foregroundStyle(.secondary)
                    }
                    ForEach(content.certificates) { row in
                        TravelCertificateRowView(row: row)
                    }
                }
                Section {
                    Button("Create travel certificate") {
                        create(content.mustChooseContract)
                    }
                    .disabled(!content.canCreate)
                    if !content.canCreate {
                        Text("You cannot create a travel certificate")
                            .foregroundStyle(.secondary)
                    } else if content.mustChooseContract {
                        Text("You choose a contract first")
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
    }
}

private struct TravelCertificateRowView: View {
    let row: TravelCertificateRow

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("\(row.startDate.formatted) to \(row.expiryDate.formatted)")
            Text(row.isExpired ? "Expired" : "Active")
                .font(.subheadline)
                .foregroundStyle(row.isExpired ? Color.secondary : Color.green)
        }
    }
}

#Preview("Loading") {
    TravelCertificateHistoryContentView(state: .loading, reload: {}, create: { _ in })
}

#Preview("Failed") {
    TravelCertificateHistoryContentView(state: .failed, reload: {}, create: { _ in })
}

#Preview("Content") {
    TravelCertificateHistoryContentView(
        state: .content(
            TravelCertificateHistoryContent(
                certificates: [
                    TravelCertificateRow(
                        id: "1", startDate: ShellDay("2026-12-20"), expiryDate: ShellDay("2027-02-03"),
                        isExpired: false
                    ),
                    TravelCertificateRow(
                        id: "2", startDate: ShellDay("2025-06-01"), expiryDate: ShellDay("2025-07-16"),
                        isExpired: true
                    ),
                ],
                canCreate: true,
                mustChooseContract: true,
                addonBanner: TravelAddonBannerContent(
                    title: "Travel add-on", description: "Cover for longer trips", labels: ["45 days", "60 days"],
                    eligibleContractCount: 1
                )
            )
        ),
        reload: {},
        create: { _ in }
    )
}
