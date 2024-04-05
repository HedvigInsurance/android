package com.hedvig.android.feature.payments.navigation

import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentOverview
import kotlinx.serialization.Serializable

sealed interface PaymentsDestination {
  @Serializable
  data object Graph : PaymentsDestination

  @Serializable
  data object Payments : PaymentsDestination
}

internal sealed interface PaymentsDestinations {
  @Serializable
  data class Details(
    val selectedMemberCharge: MemberCharge,
    val paymentOverview: PaymentOverview,
  ) : PaymentsDestinations

  @Serializable
  data class History(
    val paymentOverview: PaymentOverview,
  ) : PaymentsDestinations

  @Serializable
  data class Discounts(
    val discounts: List<Discount>,
  ) : PaymentsDestinations
}
