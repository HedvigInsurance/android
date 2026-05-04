package com.hedvig.android.feature.payments.data

import kotlinx.datetime.LocalDate

internal sealed interface PaymentConnection {
  data class Active(
    val paymentMethod: PaymentMethod,
    val chargingDay: Int?,
    val account: PaymentAccount?,
  ) : PaymentConnection

  data object Pending : PaymentConnection

  data class NeedsSetup(
    val terminationDateIfNotConnected: LocalDate?,
  ) : PaymentConnection

  data object Unknown : PaymentConnection
}

internal enum class PaymentMethod {
  TRUSTLY,
  SWISH,
  NORDEA,
  INVOICE,
}

internal sealed interface PaymentAccount {
  data object Kivra : PaymentAccount

  data class Email(val email: String) : PaymentAccount

  data class PhoneNumber(val phoneNumber: String) : PaymentAccount

  data class BankAccount(val account: String) : PaymentAccount
}
