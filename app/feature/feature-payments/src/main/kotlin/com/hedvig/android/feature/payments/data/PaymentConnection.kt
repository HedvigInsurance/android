package com.hedvig.android.feature.payments.data

import kotlinx.datetime.LocalDate

internal sealed interface PaymentConnection {
  data object Active : PaymentConnection

  data object Pending : PaymentConnection

  data class NeedsSetup(
    val terminationDateIfNotConnected: LocalDate?,
  ) : PaymentConnection

  data object Unknown : PaymentConnection
}
