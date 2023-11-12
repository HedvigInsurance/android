package com.hedvig.android.feature.payments.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface PaymentsDestinations : Destination {
  @Serializable
  data object PaymentInfo : PaymentsDestinations

  @Serializable
  data object PaymentHistory : PaymentsDestinations
}
