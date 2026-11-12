package com.hedvig.android.data.paying.member

import kotlinx.serialization.Serializable

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
