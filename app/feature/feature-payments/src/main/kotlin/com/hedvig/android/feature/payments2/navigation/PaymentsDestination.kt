package com.hedvig.android.feature.payments2.navigation

import com.hedvig.android.feature.payments2.data.MemberCharge
import com.hedvig.android.feature.payments2.data.PaymentConnection
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface PaymentsDestinations2 : Destination {
  @Serializable
  data object Overview : PaymentsDestinations2

  @Serializable
  data class Details(
    val memberCharge: MemberCharge,
    val paymentConnection: PaymentConnection?,
  ) : PaymentsDestinations2
}
