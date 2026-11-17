@preconcurrency import HedvigShared
import SwiftUI

/// Choosing the contract the certificate is for. Picking one opens the date step for it.
struct TravelCertificateChooseContractView: View {
    let holder: IosGraphHolder
    let flow: TravelCertificateFlow
    @State private var owner: TravelCertificateChooseContractScreenOwner?

    var body: some View {
        Group {
            if let owner {
                TravelCertificateChooseContractContentView(
                    state: owner.state,
                    reload: owner.reload,
                    choose: { contractId in
                        flow.path.append(.dateInput(contractId: contractId))
                    }
                )
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Choose contract")
        .onAppear {
            if owner == nil {
                owner = TravelCertificateChooseContractScreenOwner(
                    holder.travelCertificateChooseContractScreenModel()
                )
            }
        }
    }
}

struct TravelCertificateChooseContractContentView: View {
    let state: TravelCertificateChooseContractViewState
    let reload: @MainActor () -> Void
    let choose: @MainActor (String) -> Void

    var body: some View {
        switch state {
        case .loading:
            ProgressView()
        case .failed:
            VStack(spacing: 16) {
                Text("Could not load your contracts")
                Button("Retry") {
                    reload()
                }
                .buttonStyle(.bordered)
            }
        case let .content(contracts):
            if contracts.isEmpty {
                ContentUnavailableView("No contract can get a travel certificate", systemImage: "doc")
            } else {
                List(contracts) { contract in
                    Button(contract.address) {
                        choose(contract.id)
                    }
                }
            }
        }
    }
}

#Preview("Content") {
    TravelCertificateChooseContractContentView(
        state: .content([
            TravelContractRow(id: "1", address: "Exempelgatan 1"),
            TravelContractRow(id: "2", address: "Exempelvägen 2"),
        ]),
        reload: {},
        choose: { _ in }
    )
}

#Preview("Failed") {
    TravelCertificateChooseContractContentView(state: .failed, reload: {}, choose: { _ in })
}
