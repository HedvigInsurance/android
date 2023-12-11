package com.hedvig.android.feature.payments2.navigation

import com.hedvig.android.feature.payments2.data.Discount
import com.hedvig.android.feature.payments2.data.MemberCharge
import com.hedvig.android.feature.payments2.data.PaymentOverview
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface PaymentsDestinations2 : Destination {
  @Serializable
  data object Overview : PaymentsDestinations2

  @Serializable
  data class Details(
    val selectedMemberCharge: MemberCharge,
    val paymentOverview: PaymentOverview,
  ) : PaymentsDestinations2

  @Serializable
  data class History(
    val paymentOverview: PaymentOverview,
  ) : PaymentsDestinations2

  @Serializable
  data class Discounts(
    val discounts: List<Discount>,
  ) : PaymentsDestinations2
}