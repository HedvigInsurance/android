package com.hedvig.android.feature.payoutaccount.data

import octopus.type.PaymentMethodInvoiceDelivery

internal sealed interface PayoutAccount {
  val isPending: Boolean

  data class Trustly(
    val clearingNumber: String?,
    val accountNumber: String?,
    val bankName: String?,
    override val isPending: Boolean,
  ) : PayoutAccount

  data class SwishPayout(
    val phoneNumber: String?,
    override val isPending: Boolean,
  ) : PayoutAccount

  data class BankAccount(
    val clearingNumber: String?,
    val accountNumber: String?,
    val bankName: String?,
    override val isPending: Boolean,
  ) : PayoutAccount

  data class Invoice(
    val delivery: PaymentMethodInvoiceDelivery?,
    val email: String?,
    override val isPending: Boolean,
  ) : PayoutAccount
}
