package com.hedvig.android.data.paying.member

import kotlinx.serialization.Serializable
import octopus.type.MemberPaymentProvider

/**
 * A payment provider the app has a screen for. Serializable so a chosen set can be carried in a nav
 * key across process death.
 *
 * @param rawValue the name the backend knows this provider by, which identifies it in a mutation.
 */
@Serializable
enum class PaymentProvider(val rawValue: String) {
  Trustly("TRUSTLY"),
  Nordea("NORDEA"),
  Swish("SWISH"),
  Invoice("INVOICE"),
  ;

  companion object {
    /** Null for a name no entry claims, which callers drop from the providers they show. */
    fun fromRawValue(rawValue: String): PaymentProvider? = entries.firstOrNull { it.rawValue == rawValue }
  }
}

/** Null for a provider the app has no screen for, which callers drop from the methods they show. */
internal fun MemberPaymentProvider.toPaymentProvider(): PaymentProvider? = when (this) {
  MemberPaymentProvider.TRUSTLY -> PaymentProvider.Trustly
  MemberPaymentProvider.NORDEA -> PaymentProvider.Nordea
  MemberPaymentProvider.SWISH -> PaymentProvider.Swish
  MemberPaymentProvider.INVOICE -> PaymentProvider.Invoice
  MemberPaymentProvider.UNKNOWN__ -> null
}
