import Foundation
@preconcurrency import HedvigShared

typealias PublicImportantMessagesScreenOwner =
    ScreenOwner<PublicImportantMessagesScreenModel, PublicImportantMessagesViewState>

extension PublicImportantMessagesScreenOwner {
    func reload() {
        ShellLog.info("PublicImportantMessagesScreenOwner reload")
        model.reload()
    }
}

/// The messages shown while logged out, mirrored from Kotlin's `PublicImportantMessagesScreenState`.
struct PublicImportantMessagesViewState: ScreenViewState {
    let messages: [PublicImportantMessageRow]

    init(_ kotlinState: PublicImportantMessagesScreenState) {
        messages = kotlinState.messages.map(PublicImportantMessageRow.init)
    }

    var logDescription: String {
        "\(messages.count) messages"
    }
}

/// One message, copied out of Kotlin's `PublicImportantMessageItem`, which Kotlin compares by identity.
struct PublicImportantMessageRow: Identifiable, Equatable {
    enum Severity: Equatable {
        case info
        case warning
        case error
    }

    struct Link: Equatable {
        let url: URL
        let buttonText: String
    }

    let id: String
    let message: String
    let severity: Severity
    let link: Link?
}

private extension PublicImportantMessageRow {
    init(_ item: PublicImportantMessageItem) {
        id = item.id
        message = item.message
        severity = oneOf { pick in
            item.severity.handle(
                onInfo: { pick(.info) },
                onWarning: { pick(.warning) },
                onError: { pick(.error) }
            )
        }
        if let urlString = item.linkUrl, let url = URL(string: urlString), let buttonText = item.linkButtonText {
            link = Link(url: url, buttonText: buttonText)
        } else {
            link = nil
        }
    }
}
