import Foundation
@preconcurrency import HedvigShared

typealias TravelCertificateChooseContractScreenOwner =
    ScreenOwner<TravelCertificateChooseContractScreenModel, TravelCertificateChooseContractViewState>

extension TravelCertificateChooseContractScreenOwner {
    func reload() {
        sendWhileCollecting("reload") {
            ShellLog.info("TravelCertificateChooseContractScreenOwner reload")
            model.reload()
        }
    }
}

/// The contracts a certificate can be created for, mirrored from Kotlin's
/// `TravelCertificateChooseContractScreenState`.
enum TravelCertificateChooseContractViewState: ScreenViewState {
    case loading
    case failed
    case content([TravelContractRow])

    init(_ kotlinState: TravelCertificateChooseContractScreenState) {
        self = oneOf { pick in
            kotlinState.handle(
                onLoading: { pick(.loading) },
                onFailed: { pick(.failed) },
                onContent: { content in
                    pick(.content(content.contracts.map { TravelContractRow(id: $0.id, address: $0.address) }))
                }
            )
        }
    }

    var logDescription: String {
        switch self {
        case .loading: "loading"
        case .failed: "failed"
        case let .content(contracts): "content(\(contracts.count) contracts)"
        }
    }
}

struct TravelContractRow: Identifiable, Equatable {
    let id: String
    let address: String
}
