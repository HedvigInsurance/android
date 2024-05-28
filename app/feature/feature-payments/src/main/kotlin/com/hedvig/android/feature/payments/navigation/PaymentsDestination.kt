package com.hedvig.android.feature.payments.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface PaymentsDestination {
  @Serializable
  data object Graph : PaymentsDestination, Destination

  @Serializable
  data object Payments : PaymentsDestination, Destination
}

internal sealed interface PaymentsDestinations : Destination {
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
