#if DEBUG
import Foundation
@preconcurrency import HedvigShared
import ObjectiveC

/// Launch arguments for exercising failure and refresh paths by hand, read from the argument domain of
/// `UserDefaults` (`xcrun simctl launch <udid> com.hedvig.umbrella-consumer -shellFailTokenFetch YES`).
enum ShellDebug {
    /// `-shellFailTokenFetch YES`: every access token fetch throws, so Kotlin sends no request.
    static var failTokenFetch: Bool { UserDefaults.standard.bool(forKey: "shellFailTokenFetch") }

    /// `-shellExpireAccessToken YES`: the access token stored at the first request counts as expiring now, and its refresh
    /// waits 3 seconds first so a second request can arrive while it runs.
    static var expireAccessToken: Bool { UserDefaults.standard.bool(forKey: "shellExpireAccessToken") }

    /// `-shellLogRequestHeaders YES`: logs the headers of every URLSession request the process makes. Only the
    /// parity headers print their values; `Authorization` prints its shape and the rest their length.
    static var logRequestHeaders: Bool { UserDefaults.standard.bool(forKey: "shellLogRequestHeaders") }

    /// `-kotlinLogNetworkBodies YES`: Kotlin logs HTTP request and response bodies and GraphQL response data.
    static var logNetworkBodies: Bool { UserDefaults.standard.bool(forKey: "kotlinLogNetworkBodies") }

    /// `-shellClaimHistorySample YES`: the root screen offers "Sample content", a claim history built in Swift from
    /// Kotlin types and mapped by the real screen's mapper.
    static var claimHistorySample: Bool { UserDefaults.standard.bool(forKey: "shellClaimHistorySample") }

    /// Five claims covering every outcome, a nil date, a nil claim type, a regular and a partner claim sharing a raw
    /// id, and a date at 00:30 UTC today.
    static func claimHistorySampleState() -> ClaimHistoryScreenState {
        var utc = Calendar(identifier: .gregorian)
        utc.timeZone = TimeZone(identifier: "UTC")!
        let earlyToday = utc.startOfDay(for: Date()).addingTimeInterval(30 * 60)
        let october = utc.date(from: DateComponents(year: 2026, month: 10, day: 2, hour: 10))!
        let september = utc.date(from: DateComponents(year: 2026, month: 9, day: 15, hour: 12))!
        let items = [
            ClaimHistoryItem(id: "1001", claimType: "Water damage", submittedAt: october, outcome: .paid,
                             isPartnerClaim: false),
            ClaimHistoryItem(id: "P-77", claimType: "Travel", submittedAt: nil, outcome: .unknown,
                             isPartnerClaim: true),
            ClaimHistoryItem(id: "2002", claimType: "Theft", submittedAt: september, outcome: .notCompensated,
                             isPartnerClaim: false),
            ClaimHistoryItem(id: "2002", claimType: "Bicycle", submittedAt: september, outcome: .notCovered,
                             isPartnerClaim: true),
            ClaimHistoryItem(id: "3003", claimType: nil, submittedAt: earlyToday, outcome: .unresponsive,
                             isPartnerClaim: false),
        ]
        ShellLog.info("Sample built: \(items.count) Kotlin items, time zone \(TimeZone.current.identifier)")
        return ClaimHistoryScreenState.Content(items: items)
    }

    static func logSample(_ state: ClaimHistoryViewState) {
        ShellLog.info("Sample state: \(state.logDescription)")
        guard case let .content(rows) = state else { return }
        for row in rows {
            let date = row.submittedAt?.formatted(.dateTime.year().month().day()) ?? "No date"
            ShellLog.info(
                "Sample row \(row.id): \(row.claimType ?? "Claim"), \(date), \(row.outcome), partner \(row.isPartnerClaim)"
            )
        }
    }

    static func describe(_ session: StoredSession) -> String {
        "access token length \(session.accessToken.count), expires \(session.accessTokenExpiresAt); "
            + "refresh token length \(session.refreshToken.count), expires \(session.refreshTokenExpiresAt)"
    }

    /// Adds `HeaderLoggingProtocol` to every configuration made from `URLSessionConfiguration.default`. Call once,
    /// before any session exists.
    static func installHeaderLoggingIfRequested() {
        guard logRequestHeaders else { return }
        let configurationClass: AnyClass = URLSessionConfiguration.self
        guard
            let original = class_getClassMethod(configurationClass, NSSelectorFromString("defaultSessionConfiguration")),
            let replacement = class_getClassMethod(
                configurationClass,
                #selector(URLSessionConfiguration.shellDebugDefaultSessionConfiguration)
            )
        else {
            ShellLog.error("Header logging not installed")
            return
        }
        method_exchangeImplementations(original, replacement)
        ShellLog.info("Header logging installed")
    }
}

extension URLSessionConfiguration {
    /// After the exchange this name runs the original `defaultSessionConfiguration`.
    @objc class func shellDebugDefaultSessionConfiguration() -> URLSessionConfiguration {
        let configuration = shellDebugDefaultSessionConfiguration()
        configuration.protocolClasses = [HeaderLoggingProtocol.self] + (configuration.protocolClasses ?? [])
        return configuration
    }
}

/// Never handles a request; it only logs the request's headers when URLSession asks whether it would.
final class HeaderLoggingProtocol: URLProtocol {
    override class func canInit(with request: URLRequest) -> Bool {
        let host = request.url?.host() ?? "?"
        let headers = (request.allHTTPHeaderFields ?? [:]).sorted { $0.key.lowercased() < $1.key.lowercased() }
        let rendered = headers.map { name, value in "\(name)=\(shape(name: name, value: value))" }
        ShellLog.info("Request headers \(request.httpMethod ?? "?") \(host): \(rendered.joined(separator: " | "))")
        return false
    }

    /// Headers whose values are printed in full; every other value is reduced to its length.
    private static let printableHeaders: Set<String> = [
        "accept-language", "hedvig-language", "user-agent", "hedvig-app-version", "hedvig-timezone",
        "x-build-version", "x-app-version", "x-system-version", "x-platform", "x-model",
        "apollographql-client-name", "apollographql-client-version", "content-type",
    ]

    private static func shape(name: String, value: String) -> String {
        let lowercased = name.lowercased()
        if printableHeaders.contains(lowercased) {
            return value
        }
        switch lowercased {
        case "authorization":
            let parts = value.split(separator: " ", maxSplits: 1)
            let scheme = parts.first.map(String.init) ?? ""
            let credential = parts.count > 1 ? String(parts[1]) : ""
            let segments = credential.split(separator: ".", omittingEmptySubsequences: false).count
            return "<\(scheme), credential length \(credential.count), \(segments) dot-separated parts>"
        default:
            return "<length \(value.count)>"
        }
    }
}
#endif
