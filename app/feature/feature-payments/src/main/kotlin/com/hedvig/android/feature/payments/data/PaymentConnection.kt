package com.hedvig.android.feature.payments.data

import kotlinx.datetime.LocalDate

internal sealed interface PaymentConnection {
  data class Active(
    val chargeMethod: MemberPaymentChargeMethod,
  ) : PaymentConnection

  data object Pending : PaymentConnection

  data class NeedsSetup(
    val terminationDateIfNotConnected: LocalDate?,
  ) : PaymentConnection

  data object Unknown : PaymentConnection
}
