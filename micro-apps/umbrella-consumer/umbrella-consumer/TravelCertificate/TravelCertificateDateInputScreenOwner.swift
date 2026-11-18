import Foundation
@preconcurrency import HedvigShared

typealias TravelCertificateDateInputScreenOwner =
    ScreenOwner<TravelCertificateDateInputScreenModel, TravelCertificateDateInputViewState>

extension TravelCertificateDateInputScreenOwner {
    func reload() {
        sendWhileCollecting("reload") {
            ShellLog.info("TravelCertificateDateInputScreenOwner reload")
            model.reload()
        }
    }

    /// One edit of the email field. Not logged: it runs on every keystroke and carries the member's email.
    func setEmail(_ text: String) {
        sendWhileCollecting("setEmail") {
            model.setEmail(text: text)
        }
    }

    func selectTravelDate(_ date: Date) {
        sendWhileCollecting("selectTravelDate") {
            model.selectTravelDate(isoDate: TravelIsoDate.string(date))
        }
    }

    /// Sends the field's text, then submits. Both go through Kotlin's one ordered event buffer, so the submit
    /// validates exactly this text even if an earlier edit was dropped.
    func submit(fieldText: String) {
        sendWhileCollecting("submit") {
            ShellLog.info("TravelCertificateDateInputScreenOwner submit, field \(fieldText.count) characters")
            model.setEmail(text: fieldText)
            model.submit()
        }
    }
}

/// The email and date step, mirrored from Kotlin's `TravelCertificateDateInputScreenState`.
enum TravelCertificateDateInputViewState: ScreenViewState {
    case loading
    case failed
    case content(TravelDateInputContent)
    /// The summary to show and the request to leave for it, taken with `perform`.
    case created(TravelCreatedSummary, NavigationRequest<TravelCertificateCreated>)

    init(_ kotlinState: TravelCertificateDateInputScreenState) {
        self = oneOf { pick in
            kotlinState.handle(
                onLoading: { pick(.loading) },
                onFailed: { pick(.failed) },
                onContent: { content in
                    pick(
                        .content(
                            TravelDateInputContent(
                                email: content.email,
                                emailRevision: content.emailRevision,
                                emailError: content.emailError.map(TravelEmailError.init),
                                minDate: content.minDate,
                                maxDate: content.maxDate,
                                selectedDate: content.selectedDate,
                                daysValid: Int(content.daysValid),
                                hasCoInsured: content.hasCoInsured,
                                isCreating: content.isCreating,
                                createFailed: content.createFailed,
                                next: content.next
                            )
                        )
                    )
                },
                onCreated: { created in
                    pick(.created(TravelCreatedSummary(created.certificate.target), created.certificate))
                }
            )
        }
    }

    var content: TravelDateInputContent? {
        if case let .content(content) = self { content } else { nil }
    }

    var logDescription: String {
        switch self {
        case .loading: "loading"
        case .failed: "failed"
        case let .content(content):
            "content(email \(content.email.count) characters, revision \(content.emailRevision), error \(content.emailError.map { "\($0)" } ?? "none"), coInsured \(content.hasCoInsured), creating \(content.isCreating), createFailed \(content.createFailed), next \(content.next != nil))"
        case let .created(summary, _): "created(url length \(summary.length))"
        }
    }
}

struct TravelDateInputContent: Equatable {
    let email: String
    let emailRevision: Int32
    let emailError: TravelEmailError?
    let minDate: String
    let maxDate: String
    let selectedDate: String
    let daysValid: Int
    /// False: Continue creates the certificate. True: Continue offers the travellers step in `next`.
    let hasCoInsured: Bool
    let isCreating: Bool
    let createFailed: Bool
    /// The travellers step to open, taken with `perform`. Its target is opaque: Swift can only hold it and pass it to
    /// `travelCertificateTravellersScreenModel(step:)`.
    let next: NavigationRequest<TravelCertificateTravellersStep>?
}

enum TravelEmailError: Equatable {
    case empty
    case invalid

    init(_ error: TravelCertificateEmailError) {
        self = oneOf { pick in
            error.handle(onEmpty: { pick(.empty) }, onInvalid: { pick(.invalid) })
        }
    }

    var message: String {
        switch self {
        case .empty: "Enter your email"
        case .invalid: "Enter a valid email"
        }
    }
}
