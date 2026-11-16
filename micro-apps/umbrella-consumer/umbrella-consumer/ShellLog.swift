import os

/// The shell's own log lines, written in full to the unified log. Never pass a token, an authorization code or QR
/// data to them.
enum ShellLog {
    private static let logger = Logger(subsystem: "com.hedvig.umbrella-consumer", category: "shell")

    static func info(_ message: String) {
        logger.info("\(message, privacy: .public)")
    }

    static func error(_ message: String) {
        logger.error("\(message, privacy: .public)")
    }
}
