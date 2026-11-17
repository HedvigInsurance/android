import Foundation
@preconcurrency import HedvigShared
import Observation
import SwiftUI

/// Kotlin's `yyyy-MM-dd` days as `Date`s at UTC midnight and back. Every date the travel screens read or send goes
/// through here, so a day never shifts with the device's time zone or calendar.
enum TravelIsoDate {
    static let utc = TimeZone(identifier: "UTC")!

    static let calendar: Calendar = {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = utc
        return calendar
    }()

    private static let formatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.calendar = calendar
        formatter.timeZone = utc
        formatter.dateFormat = "yyyy-MM-dd"
        formatter.isLenient = false
        return formatter
    }()

    static func date(_ iso: String) -> Date? {
        formatter.date(from: iso)
    }

    static func string(_ date: Date) -> String {
        formatter.string(from: date)
    }
}

/// A created certificate, reduced to what the result screen may show. The signed URL itself is not kept.
struct TravelCreatedSummary: Hashable {
    let host: String
    let length: Int

    init(_ created: TravelCertificateCreated) {
        let url = created.certificateUrl
        host = URL(string: url)?.host() ?? "unreadable"
        length = url.count
    }

    init(host: String, length: Int) {
        self.host = host
        self.length = length
    }
}

/// The travel certificate flow's screens, as values on the root `NavigationStack`'s path.
enum TravelRoute: Hashable {
    case history
    case chooseContract
    case dateInput(contractId: String?)
    case travellers(TravelCertificateTravellersStep)
    case result(TravelCreatedSummary)
}

/// The typed path of the root stack.
@MainActor
@Observable
final class TravelCertificateFlow {
    var path: [TravelRoute] = []

    /// Ends the flow on the result screen, directly above the history.
    func finish(_ summary: TravelCreatedSummary) {
        path = pathThroughHistory() + [.result(summary)]
    }

    func backToHistory() {
        path = pathThroughHistory()
    }

    private func pathThroughHistory() -> [TravelRoute] {
        guard let index = path.firstIndex(of: .history) else { return [.history] }
        return Array(path.prefix(through: index))
    }
}

/// Builds the screen for one route.
struct TravelCertificateDestination: View {
    let route: TravelRoute
    let holder: IosGraphHolder
    let flow: TravelCertificateFlow

    var body: some View {
        switch route {
        case .history:
            TravelCertificateHistoryView(holder: holder, flow: flow)
        case .chooseContract:
            TravelCertificateChooseContractView(holder: holder, flow: flow)
        case let .dateInput(contractId):
            TravelCertificateDateInputView(holder: holder, flow: flow, contractId: contractId)
        case let .travellers(step):
            TravelCertificateTravellersView(holder: holder, flow: flow, step: step)
        case let .result(summary):
            TravelCertificateResultView(summary: summary, flow: flow)
        }
    }
}

/// Holds a create call until it is confirmed. In DEBUG builds every call that can create a certificate on the
/// staging account asks first, since a created certificate cannot be removed; Release builds run it at once.
@MainActor
@Observable
final class TravelCreateGuard {
    private(set) var pending: (@MainActor () -> Void)?

    func request(_ create: @escaping @MainActor () -> Void) {
        #if DEBUG
        pending = create
        #else
        create()
        #endif
    }

    func cancel() {
        pending = nil
    }
}

extension View {
    /// The DEBUG confirmation in front of a create call held by `createGuard`.
    func travelCreateConfirmation(_ createGuard: TravelCreateGuard, message: String) -> some View {
        alert(
            "Create a real travel certificate on staging?",
            isPresented: Binding(
                get: { createGuard.pending != nil },
                set: { if !$0 { createGuard.cancel() } }
            )
        ) {
            // Captured while the alert is built: SwiftUI may reset `isPresented`, which cancels, before the action runs.
            let create = createGuard.pending
            Button("Create", role: .destructive) {
                createGuard.cancel()
                create?()
            }
            Button("Cancel", role: .cancel) { createGuard.cancel() }
        } message: {
            Text(message)
        }
    }
}
