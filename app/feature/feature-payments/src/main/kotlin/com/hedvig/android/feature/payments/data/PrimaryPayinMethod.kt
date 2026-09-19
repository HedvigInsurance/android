package com.hedvig.android.feature.payments.data

import com.hedvig.android.feature.payin.account.navigation.PayinMethodId
import octopus.fragment.MemberPaymentMethodFragment
import octopus.fragment.MemberPaymentMethodFragment.PaymentMethodBankAccountDetailsDetails
import octopus.fragment.MemberPaymentMethodFragment.PaymentMethodInvoiceDetailsDetails
import octopus.fragment.MemberPaymentMethodFragment.PaymentMethodSwishDetailsDetails
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery

/**
 * The method the member is currently charged with, summarised for the payments overview.
 *
 * [descriptor] is the method's account in the member's own terms — the Swish number, the masked bank account, or
 * the invoice delivery channel. Invoice has nothing else worth showing, so the row uses it as the title there and
 * as the subtitle for the other providers.
 */
data class PrimaryPayinMethod(
  val id: PayinMethodId,
  val descriptor: String?,
)

fun MemberPaymentMethodFragment.toPrimaryPayinMethod(): PrimaryPayinMethod? {
  val id = when (provider) {
    MemberPaymentProvider.TRUSTLY -> PayinMethodId.Trustly
    MemberPaymentProvider.SWISH -> PayinMethodId.Swish
    MemberPaymentProvider.INVOICE -> PayinMethodId.Invoice
    MemberPaymentProvider.NORDEA, MemberPaymentProvider.UNKNOWN__ -> return null
  }
  return PrimaryPayinMethod(id = id, descriptor = details?.descriptor())
}

private fun MemberPaymentMethodFragment.Details.descriptor(): String? = when (this) {
  is PaymentMethodSwishDetailsDetails -> {
    formatSwishPhoneNumber(phoneNumber)
  }

  is PaymentMethodBankAccountDetailsDetails -> {
    val lastFour = account.substringAfter('-', "").takeLast(4).takeIf { it.isNotBlank() }
    if (lastFour == null) bank else "$bank ···· $lastFour"
  }

  is PaymentMethodInvoiceDetailsDetails -> {
    when (delivery) {
      // TODO: Add "Kivra" / "Kivra" to Lokalise
      PaymentMethodInvoiceDelivery.KIVRA -> "Kivra"

      // TODO: Add "Email" / "E-post" to Lokalise
      PaymentMethodInvoiceDelivery.MAIL -> "Email"

      PaymentMethodInvoiceDelivery.UNKNOWN__ -> null
    }
  }

  else -> {
    null
  }
}

private fun formatSwishPhoneNumber(phoneNumber: String): String {
  val digits = phoneNumber.take(15)
  val sb = StringBuilder()
  for (i in digits.indices) {
    sb.append(digits[i])
    if (i in setOf(2, 5, 7)) {
      sb.append("-")
    }
  }
  return sb.toString()
}
