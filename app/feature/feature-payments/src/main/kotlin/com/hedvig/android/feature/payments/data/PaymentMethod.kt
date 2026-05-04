package com.hedvig.android.feature.payments.data

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

  data class BankAccount(val account: String, val bank: String) : PaymentAccount
}
