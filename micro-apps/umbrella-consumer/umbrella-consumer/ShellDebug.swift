#if DEBUG
import Foundation
import ObjectiveC

/// Launch arguments for exercising failure and refresh paths by hand, read from the argument domain of
/// `UserDefaults` (`xcrun simctl launch <udid> com.hedvig.umbrella-consumer -shellFailTokenFetch YES`).
enum ShellDebug {
    /// `-shellFailTokenFetch YES`: every access token fetch throws, so Kotlin sends no request.
    static var failTokenFetch: Bool { UserDefaults.standard.bool(forKey: "shellFailTokenFetch") }

    /// `-shellExpireAccessToken YES`: the access token stored at the first request counts as expiring now, and its refresh
    /// waits 3 seconds first so a second request can arrive while it runs.
    static var expireAccessToken: Bool { UserDefaults.standard.bool(forKey: "shellExpireAccessToken") }

    /// `-shellLogRequestHeaders YES`: logs the headers of every URLSession request the process makes, with the
    /// `Authorization` value and the device id reduced to their shape.
    static var logRequestHeaders: Bool { UserDefaults.standard.bool(forKey: "shellLogRequestHeaders") }

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

    private static func shape(name: String, value: String) -> String {
        switch name.lowercased() {
        case "authorization":
            let parts = value.split(separator: " ", maxSplits: 1)
            let scheme = parts.first.map(String.init) ?? ""
            let credential = parts.count > 1 ? String(parts[1]) : ""
            let segments = credential.split(separator: ".", omittingEmptySubsequences: false).count
            return "<\(scheme), credential length \(credential.count), \(segments) dot-separated parts>"
        case "hedvig-device-id":
            return "<length \(value.count)>"
        default:
            return value
        }
    }
}
#endif
