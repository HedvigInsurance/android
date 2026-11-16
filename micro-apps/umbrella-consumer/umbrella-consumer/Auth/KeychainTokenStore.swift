import Foundation
import Security

struct StoredSession: Codable, Sendable {
    var accessToken: String
    var accessTokenExpiresAt: Date
    var refreshToken: String
    var refreshTokenExpiresAt: Date
}

/// One generic password item holding the session as JSON. Safe from any thread.
struct KeychainTokenStore: Sendable {
    private let service = "com.hedvig.umbrella-consumer.session"
    private let account = "hedvig"

    private var baseQuery: [CFString: Any] {
        [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account,
        ]
    }

    func read() throws -> StoredSession? {
        var query = baseQuery
        query[kSecReturnData] = true
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        switch status {
        case errSecSuccess:
            guard let data = result as? Data else { return nil }
            return try? JSONDecoder().decode(StoredSession.self, from: data)
        case errSecItemNotFound:
            return nil
        default:
            ShellLog.error("Keychain read OSStatus \(status)")
            throw NSError(domain: NSOSStatusErrorDomain, code: Int(status))
        }
    }

    @discardableResult
    func save(_ session: StoredSession) -> OSStatus {
        guard let data = try? JSONEncoder().encode(session) else { return errSecParam }
        var addQuery = baseQuery
        addQuery[kSecValueData] = data
        addQuery[kSecAttrAccessible] = kSecAttrAccessibleAfterFirstUnlock
        var status = SecItemAdd(addQuery as CFDictionary, nil)
        if status == errSecDuplicateItem {
            status = SecItemUpdate(baseQuery as CFDictionary, [kSecValueData: data] as CFDictionary)
        }
        ShellLog.info("Keychain write OSStatus \(status)")
        return status
    }

    func delete() {
        let status = SecItemDelete(baseQuery as CFDictionary)
        ShellLog.info("Keychain delete OSStatus \(status)")
    }
}
