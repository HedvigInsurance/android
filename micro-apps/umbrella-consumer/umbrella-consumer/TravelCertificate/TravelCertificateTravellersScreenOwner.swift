import Foundation
@preconcurrency import HedvigShared

typealias TravelCertificateTravellersScreenOwner =
    ScreenOwner<TravelCertificateTravellersScreenModel, TravelCertificateTravellersViewState>

extension TravelCertificateTravellersScreenOwner {
    func reload() {
        sendWhileCollecting("reload") {
            ShellLog.info("TravelCertificateTravellersScreenOwner reload")
            model.reload()
        }
    }

    func setMemberIncluded(_ included: Bool) {
        sendWhileCollecting("setMemberIncluded") {
            ShellLog.info("TravelCertificateTravellersScreenOwner setMemberIncluded \(included)")
            model.setMemberIncluded(included: included)
        }
    }

    func setCoInsuredIncluded(id: String, included: Bool) {
        sendWhileCollecting("setCoInsuredIncluded") {
            ShellLog.info("TravelCertificateTravellersScreenOwner setCoInsuredIncluded \(included)")
            model.setCoInsuredIncluded(id: id, included: included)
        }
    }

    func create() {
        sendWhileCollecting("create") {
            ShellLog.info("TravelCertificateTravellersScreenOwner create")
            model.create()
        }
    }
}

/// Who travels, mirrored from Kotlin's `TravelCertificateTravellersScreenState`.
enum TravelCertificateTravellersViewState: ScreenViewState {
    case loading
    case failed
    case content(TravellersContent)
    /// The summary to show and the request to leave for it, taken with `perform`.
    case created(TravelCreatedSummary, NavigationRequest<TravelCertificateCreated>)

    init(_ kotlinState: TravelCertificateTravellersScreenState) {
        self = oneOf { pick in
            kotlinState.handle(
                onLoading: { pick(.loading) },
                onFailed: { pick(.failed) },
                onContent: { content in
                    pick(
                        .content(
                            TravellersContent(
                                memberFullName: content.memberFullName,
                                isMemberIncluded: content.isMemberIncluded,
                                coInsured: content.coInsured.map(TravellerRow.init),
                                coInsuredHasMissingInfo: content.coInsuredHasMissingInfo,
                                canCreate: content.canCreate,
                                isCreating: content.isCreating,
                                createFailed: content.createFailed
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

    var content: TravellersContent? {
        if case let .content(content) = self { content } else { nil }
    }

    var logDescription: String {
        switch self {
        case .loading: "loading"
        case .failed: "failed"
        case let .content(content):
            "content(member \(content.isMemberIncluded), \(content.coInsured.filter(\.isIncluded).count) of \(content.coInsured.count) co-insured, missing info \(content.coInsuredHasMissingInfo), canCreate \(content.canCreate), creating \(content.isCreating), createFailed \(content.createFailed))"
        case let .created(summary, _): "created(url length \(summary.length))"
        }
    }
}

struct TravellersContent: Equatable {
    let memberFullName: String
    let isMemberIncluded: Bool
    let coInsured: [TravellerRow]
    let coInsuredHasMissingInfo: Bool
    let canCreate: Bool
    let isCreating: Bool
    let createFailed: Bool
}

struct TravellerRow: Identifiable, Equatable {
    let id: String
    let name: String
    let dateOfBirth: ShellDay?
    let isIncluded: Bool
}

extension TravellerRow {
    init(_ traveller: TravelCertificateTraveller) {
        id = traveller.id
        name = traveller.name
        dateOfBirth = traveller.dateOfBirth.map(ShellDay.init)
        isIncluded = traveller.isIncluded
    }
}
