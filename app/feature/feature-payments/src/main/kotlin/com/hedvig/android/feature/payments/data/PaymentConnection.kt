package com.hedvig.android.feature.payments.data

internal sealed interface PaymentConnection {
  data class Active(
    val displayName: String,
    val displayValue: String,
  ) : PaymentConnection

  data object Pending : PaymentConnection

  data object NeedsSetup : PaymentConnection

  data object Unknown : PaymentConnection
}
