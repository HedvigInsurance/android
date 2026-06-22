package com.hedvig.android.feature.payments.data

import kotlinx.datetime.LocalDate

sealed interface PaymentConnection {
  data object Active : PaymentConnection

  data object Pending : PaymentConnection

  data class NeedsPayinSetup(
    val terminationDateIfNotConnected: LocalDate?,
  ) : PaymentConnection

  data object NeedsPayoutSetup: PaymentConnection

  data object Unknown : PaymentConnection
}
