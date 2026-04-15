package com.hedvig.android.feature.payoutaccount.data

internal sealed interface PayoutAccount {
  data object Trustly : PayoutAccount

  data class SwishPayout(val phoneNumber: String) : PayoutAccount

  data class BankAccount(
    val clearingNumber: String,
    val accountNumber: String,
    val bankName: String?,
  ) : PayoutAccount
}
