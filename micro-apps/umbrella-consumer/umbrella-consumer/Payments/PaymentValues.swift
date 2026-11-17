import Foundation
@preconcurrency import HedvigShared

/// An amount of money copied out of Kotlin's `PaymentMoney`.
struct ShellMoney: Equatable {
    let amount: Double
    let currencyCode: String

    init(_ money: PaymentMoney) {
        amount = money.amount
        currencyCode = money.currencyCode
    }

    init(amount: Double, currencyCode: String) {
        self.amount = amount
        self.currencyCode = currencyCode
    }

    var formatted: String {
        amount.formatted(.currency(code: currencyCode))
    }
}

/// A calendar day from one of Kotlin's `yyyy-MM-dd` strings. The day is read and shown in UTC, so it never shifts
/// with the device's time zone.
struct ShellDay: Equatable {
    let iso: String
    let date: Date?

    init(_ iso: String) {
        self.iso = iso
        date = ShellDay.parser.date(from: iso)
    }

    var year: Int? {
        date.map { ShellDay.utcCalendar.component(.year, from: $0) }
    }

    var formatted: String {
        guard let date else { return "Unreadable date \(iso)" }
        return date.formatted(Date.FormatStyle(date: .abbreviated, time: .omitted, timeZone: ShellDay.utc))
    }

    private static let utc = TimeZone(identifier: "UTC")!

    private static let utcCalendar: Calendar = {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = utc
        return calendar
    }()

    private static let parser: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.calendar = utcCalendar
        formatter.timeZone = utc
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()
}

/// Kotlin's `PaymentChargeStatus`.
enum ShellChargeStatus: Equatable {
    case upcoming
    case success
    case pending
    case failed
    case unknown

    init(_ status: PaymentChargeStatus) {
        self = oneOf { pick in
            status.handle(
                onUpcoming: { pick(.upcoming) },
                onSuccess: { pick(.success) },
                onPending: { pick(.pending) },
                onFailed: { pick(.failed) },
                onUnknown: { pick(.unknown) }
            )
        }
    }

    var label: String {
        switch self {
        case .upcoming: "Upcoming"
        case .success: "Paid"
        case .pending: "Processing"
        case .failed: "Failed"
        case .unknown: "Unknown status"
        }
    }
}

/// Kotlin's `PaymentChargeMethod`.
enum ShellChargeMethod: Equatable {
    case trustly
    case swish
    case invoice
    case unknown

    init(_ method: PaymentChargeMethod) {
        self = oneOf { pick in
            method.handle(
                onTrustly: { pick(.trustly) },
                onSwish: { pick(.swish) },
                onInvoice: { pick(.invoice) },
                onUnknown: { pick(.unknown) }
            )
        }
    }

    var label: String {
        switch self {
        case .trustly: "Bank account (Trustly)"
        case .swish: "Swish"
        case .invoice: "Invoice"
        case .unknown: "Unknown method"
        }
    }
}
