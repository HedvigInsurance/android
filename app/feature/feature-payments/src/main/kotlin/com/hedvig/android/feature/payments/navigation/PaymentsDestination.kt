package com.hedvig.android.feature.payments.navigation

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
    val memberChargeId: String,
  ) : PaymentsDestinations

  @Serializable
  data object History : PaymentsDestinations

  @Serializable
  data object Discounts : PaymentsDestinations

  @Serializable
  data object Forever : PaymentsDestinations
}
