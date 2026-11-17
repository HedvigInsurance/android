import Foundation
@preconcurrency import HedvigShared

typealias TravelCertificateHistoryScreenOwner =
    ScreenOwner<TravelCertificateHistoryScreenModel, TravelCertificateHistoryViewState>

extension TravelCertificateHistoryScreenOwner {
    func reload() {
        sendWhileCollecting("reload") {
            ShellLog.info("TravelCertificateHistoryScreenOwner reload")
            model.reload()
        }
    }
}

/// What the travel certificate history shows, mirrored from Kotlin's `TravelCertificateHistoryScreenState`.
enum TravelCertificateHistoryViewState: ScreenViewState {
    case loading
    case failed
    case content(TravelCertificateHistoryContent)

    init(_ kotlinState: TravelCertificateHistoryScreenState) {
        self = oneOf { pick in
            kotlinState.handle(
                onLoading: { pick(.loading) },
                onFailed: { pick(.failed) },
                onContent: { content in
                    pick(
                        .content(
                            TravelCertificateHistoryContent(
                                certificates: content.certificates.map(TravelCertificateRow.init),
                                canCreate: content.canCreate,
                                mustChooseContract: content.mustChooseContract,
                                addonBanner: content.addonBanner.map(TravelAddonBannerContent.init)
                            )
                        )
                    )
                }
            )
        }
    }

    var logDescription: String {
        switch self {
        case .loading: "loading"
        case .failed: "failed"
        case let .content(content):
            "content(\(content.certificates.count) certificates, \(content.certificates.filter(\.isExpired).count) expired, canCreate \(content.canCreate), mustChooseContract \(content.mustChooseContract), banner \(content.addonBanner != nil))"
        }
    }
}

struct TravelCertificateHistoryContent: Equatable {
    let certificates: [TravelCertificateRow]
    let canCreate: Bool
    let mustChooseContract: Bool
    let addonBanner: TravelAddonBannerContent?
}

/// One certificate. The signed URL is not copied: the host never opens or shows it.
struct TravelCertificateRow: Identifiable, Equatable {
    let id: String
    let startDate: ShellDay
    let expiryDate: ShellDay
    let isExpired: Bool
}

extension TravelCertificateRow {
    init(_ item: TravelCertificateItem) {
        id = item.id
        startDate = ShellDay(item.startDate)
        expiryDate = ShellDay(item.expiryDate)
        isExpired = item.isExpired
    }
}

struct TravelAddonBannerContent: Equatable {
    let title: String
    let description: String
    let labels: [String]
    let eligibleContractCount: Int
}

extension TravelAddonBannerContent {
    init(_ banner: TravelAddonBanner) {
        title = banner.title
        description = banner.bannerDescription
        labels = banner.labels
        eligibleContractCount = banner.eligibleContractIds.count
    }
}
