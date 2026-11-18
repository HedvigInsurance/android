@preconcurrency import HedvigShared
import SwiftUI

/// The email and travel date step. The email field owns its text (design D1): every edit is sent with `setEmail`,
/// and Kotlin's email is copied in only when `emailRevision` changes. In DEBUG builds a switch swaps the field to
/// design D2, a binding read from and written to Kotlin's state, for the typing probe.
struct TravelCertificateDateInputView: View {
    let holder: IosGraphHolder
    let flow: TravelCertificateFlow
    let contractId: String?

    @State private var owner: TravelCertificateDateInputScreenOwner?
    @State private var createGuard = TravelCreateGuard()
    /// The D1 field's text.
    @State private var emailText = ""
    /// The revision whose email was last copied into the field.
    @State private var copiedRevision: Int32?
    /// Kotlin's email while it is being copied into the field, so that copy is not sent back as an edit.
    @State private var copyInFlight: String?
    #if DEBUG
    @State private var usesBindingDesign = false
    /// What the D2 field last reported through its binding's setter.
    @State private var bindingFieldText = ""
    #endif

    var body: some View {
        Group {
            if let owner {
                switch owner.state {
                case .loading, .created:
                    ProgressView()
                case .failed:
                    VStack(spacing: 16) {
                        Text("Could not load the form")
                        Button("Retry") {
                            owner.reload()
                        }
                        .buttonStyle(.bordered)
                    }
                case let .content(content):
                    form(owner: owner, content: content)
                }
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Dates and email")
        // Leaving would stop the collection and with it the create's request (owner rule 1).
        .navigationBarBackButtonHidden(owner?.state.content?.isCreating == true)
        .interactiveDismissDisabled(owner?.state.content?.isCreating == true)
        .onDisappear {
            if owner?.state.content?.isCreating == true {
                ShellLog.error("TravelCertificateDateInputView left while creating; the history reloads when shown")
            }
        }
        .travelCreateConfirmation(
            createGuard,
            message: "Continue creates the certificate at once. It cannot be removed."
        )
        .onAppear {
            if owner == nil {
                owner = TravelCertificateDateInputScreenOwner(
                    holder.travelCertificateDateInputScreenModel(contractId: contractId)
                )
            }
        }
        .onChange(of: owner?.state, initial: true) { _, state in
            if let state {
                react(to: state)
            }
        }
    }

    private func react(to state: TravelCertificateDateInputViewState) {
        switch state {
        case .loading, .failed:
            break
        case let .content(content):
            if content.emailRevision != copiedRevision {
                copiedRevision = content.emailRevision
                if emailText != content.email {
                    copyInFlight = content.email
                    emailText = content.email
                }
            }
            owner?.perform(content.next) { step in
                flow.path.append(.travellers(step))
            }
        case let .created(summary, request):
            owner?.perform(request) { _ in
                flow.finish(summary)
            }
        }
    }

    @ViewBuilder
    private func form(owner: TravelCertificateDateInputScreenOwner, content: TravelDateInputContent) -> some View {
        Form {
            Section("Email") {
                emailField(owner: owner)
                if let error = content.emailError {
                    Text(error.message)
                        .foregroundStyle(.red)
                }
                #if DEBUG
                Toggle("D2: bind the field to Kotlin's email", isOn: $usesBindingDesign)
                    .onChange(of: usesBindingDesign) { _, usesBinding in
                        if usesBinding {
                            bindingFieldText = content.email
                        } else if emailText != content.email {
                            copyInFlight = content.email
                            emailText = content.email
                        }
                    }
                Text("Field \(fieldText.count) characters, Kotlin \(content.email.count) characters")
                    .font(.caption.monospacedDigit())
                    .foregroundStyle(fieldText.count == content.email.count ? Color.secondary : Color.red)
                    .accessibilityIdentifier("emailCharacterCounter")
                #endif
            }
            Section("Travel date") {
                datePicker(owner: owner, content: content)
                Text("Valid for \(content.daysValid) days")
                    .foregroundStyle(.secondary)
            }
            Section {
                if content.isCreating {
                    HStack {
                        ProgressView()
                        Text("Creating certificate")
                    }
                } else {
                    Button(content.hasCoInsured ? "Continue" : "Create certificate") {
                        let text = fieldText
                        if content.hasCoInsured {
                            owner.submit(fieldText: text)
                        } else {
                            createGuard.request { owner.submit(fieldText: text) }
                        }
                    }
                    .disabled(!owner.isCollecting)
                }
                if content.createFailed {
                    Text("Could not create the certificate. Create certificate tries again.")
                        .foregroundStyle(.red)
                }
            }
        }
    }

    @ViewBuilder
    private func emailField(owner: TravelCertificateDateInputScreenOwner) -> some View {
        #if DEBUG
        if usesBindingDesign {
            TextField(
                "Email",
                text: Binding(
                    get: { owner.state.content?.email ?? "" },
                    set: { text in
                        bindingFieldText = text
                        owner.setEmail(text)
                    }
                )
            )
            .emailInput()
            .accessibilityIdentifier("emailFieldD2")
        } else {
            ownedEmailField(owner: owner)
        }
        #else
        ownedEmailField(owner: owner)
        #endif
    }

    private func ownedEmailField(owner: TravelCertificateDateInputScreenOwner) -> some View {
        TextField("Email", text: $emailText)
            .emailInput()
            .accessibilityIdentifier("emailFieldD1")
            .onChange(of: emailText) { _, text in
                if let copied = copyInFlight {
                    copyInFlight = nil
                    if copied == text { return }
                }
                owner.setEmail(text)
            }
    }

    /// The text the email field holds, which Continue sends before submitting (owner rule 2).
    private var fieldText: String {
        #if DEBUG
        usesBindingDesign ? bindingFieldText : emailText
        #else
        emailText
        #endif
    }

    @ViewBuilder
    private func datePicker(owner: TravelCertificateDateInputScreenOwner, content: TravelDateInputContent) -> some View {
        if let minDate = TravelIsoDate.date(content.minDate),
           let maxDate = TravelIsoDate.date(content.maxDate),
           minDate <= maxDate
        {
            DatePicker(
                "Travel date",
                selection: Binding(
                    get: { owner.state.content.flatMap { TravelIsoDate.date($0.selectedDate) } ?? minDate },
                    set: { owner.selectTravelDate($0) }
                ),
                in: minDate ... maxDate,
                displayedComponents: .date
            )
            // The picker shows days in the same calendar and zone TravelIsoDate reads and writes, so the day on screen
            // is the day sent, whatever the device's zone.
            .environment(\.calendar, TravelIsoDate.calendar)
            .environment(\.timeZone, TravelIsoDate.utc)
        } else {
            Text("The allowed dates could not be read")
                .foregroundStyle(.red)
        }
    }
}

private extension View {
    func emailInput() -> some View {
        textContentType(.emailAddress)
            .keyboardType(.emailAddress)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
    }
}
