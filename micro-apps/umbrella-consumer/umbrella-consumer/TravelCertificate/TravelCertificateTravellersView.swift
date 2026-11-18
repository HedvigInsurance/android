@preconcurrency import HedvigShared
import SwiftUI

/// Choosing who travels, then creating the certificate.
struct TravelCertificateTravellersView: View {
    let holder: IosGraphHolder
    let flow: TravelCertificateFlow
    let step: TravelCertificateTravellersStep

    @State private var owner: TravelCertificateTravellersScreenOwner?
    @State private var createGuard = TravelCreateGuard()

    var body: some View {
        Group {
            if let owner {
                switch owner.state {
                case .loading, .created:
                    ProgressView()
                case .failed:
                    VStack(spacing: 16) {
                        Text("Could not load the travellers")
                        Button("Retry") {
                            owner.reload()
                        }
                        .buttonStyle(.bordered)
                    }
                case let .content(content):
                    TravelCertificateTravellersContentView(
                        content: content,
                        isCollecting: owner.isCollecting,
                        setMemberIncluded: owner.setMemberIncluded,
                        setCoInsuredIncluded: owner.setCoInsuredIncluded,
                        create: { createGuard.request(owner.create) }
                    )
                }
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Travellers")
        // Leaving would stop the collection and with it the create's request (owner rule 1).
        .navigationBarBackButtonHidden(owner?.state.content?.isCreating == true)
        .travelCreateConfirmation(createGuard, message: "A created certificate cannot be removed.")
        .onAppear {
            if owner == nil {
                owner = TravelCertificateTravellersScreenOwner(holder.travelCertificateTravellersScreenModel(step: step))
            }
        }
        .interactiveDismissDisabled(owner?.state.content?.isCreating == true)
        .onDisappear {
            if owner?.state.content?.isCreating == true {
                ShellLog.error("TravelCertificateTravellersView left while creating; the history reloads when shown")
            }
        }
        .onChange(of: owner?.state, initial: true) { _, state in
            if case let .created(summary, request) = state {
                owner?.perform(request) { _ in
                    flow.finish(summary)
                }
            }
        }
    }
}

struct TravelCertificateTravellersContentView: View {
    let content: TravellersContent
    let isCollecting: Bool
    let setMemberIncluded: @MainActor (Bool) -> Void
    let setCoInsuredIncluded: @MainActor (_ id: String, _ included: Bool) -> Void
    let create: @MainActor () -> Void

    var body: some View {
        Form {
            Section("Travellers") {
                Toggle(
                    content.memberFullName,
                    isOn: Binding(get: { content.isMemberIncluded }, set: { setMemberIncluded($0) })
                )
                // By position: ids are not guaranteed distinct. A backend id passes through as given, and co-insured
                // without one who share name, SSN and birth date get the same generated id.
                ForEach(Array(content.coInsured.enumerated()), id: \.offset) { _, traveller in
                    Toggle(
                        isOn: Binding(
                            get: { traveller.isIncluded },
                            set: { setCoInsuredIncluded(traveller.id, $0) }
                        )
                    ) {
                        VStack(alignment: .leading) {
                            Text(traveller.name)
                            if let dateOfBirth = traveller.dateOfBirth {
                                Text(dateOfBirth.formatted)
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }
            }
            .disabled(content.isCreating || !isCollecting)
            if content.coInsuredHasMissingInfo {
                Section {
                    Text("Some co-insured are missing information, so they cannot be added here.")
                        .foregroundStyle(.secondary)
                }
            }
            Section {
                if content.isCreating {
                    HStack {
                        ProgressView()
                        Text("Creating certificate")
                    }
                } else {
                    Button("Create certificate") {
                        create()
                    }
                    .disabled(!content.canCreate || !isCollecting)
                }
                if content.createFailed {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Could not create the certificate")
                            .foregroundStyle(.red)
                        Button("Try again") {
                            create()
                        }
                        .disabled(!content.canCreate || content.isCreating || !isCollecting)
                    }
                }
            }
        }
    }
}

#Preview("Content") {
    TravelCertificateTravellersContentView(
        content: TravellersContent(
            memberFullName: "Member Name",
            isMemberIncluded: true,
            coInsured: [
                TravellerRow(id: "1", name: "Co-insured One", dateOfBirth: ShellDay("2015-03-01"), isIncluded: true),
                TravellerRow(id: "2", name: "Co-insured Two", dateOfBirth: nil, isIncluded: false),
            ],
            coInsuredHasMissingInfo: true,
            canCreate: true,
            isCreating: false,
            createFailed: true
        ),
        isCollecting: true,
        setMemberIncluded: { _ in },
        setCoInsuredIncluded: { _, _ in },
        create: {}
    )
}
