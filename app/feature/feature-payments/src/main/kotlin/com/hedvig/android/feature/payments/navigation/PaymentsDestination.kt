package com.hedvig.android.feature.payments.navigation

import com.hedvig.android.navigation.compose.Destination
import kotlinx.serialization.Serializable

sealed interface PaymentsDestination {
  @Serializable
  data object Graph : PaymentsDestination, Destination

  @Serializable
  data object Payments : PaymentsDestination, Destination
}

internal sealed interface PaymentsDestinations {
  @Serializable
  data class Details(
    val memberChargeId: String?,
  ) : PaymentsDestinations, Destination

  @Serializable
  data object History : PaymentsDestinations, Destination

  @Serializable
  data object Discounts : PaymentsDestinations, Destination

  @Serializable
  data object Forever : PaymentsDestinations, Destination
}
