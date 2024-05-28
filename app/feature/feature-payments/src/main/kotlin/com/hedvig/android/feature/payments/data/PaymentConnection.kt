package com.hedvig.android.feature.payments.data

import kotlinx.serialization.Serializable

@Serializable
internal sealed interface PaymentConnection {
  @Serializable
  data class Active(
    val displayName: String,
    val displayValue: String,
  ) : PaymentConnection

  @Serializable
  data object Pending : PaymentConnection

  @Serializable
  data object NeedsSetup : PaymentConnection

  @Serializable
  data object Unknown : PaymentConnection
}
