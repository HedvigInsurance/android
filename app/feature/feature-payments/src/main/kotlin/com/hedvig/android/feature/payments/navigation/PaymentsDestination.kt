package com.hedvig.android.feature.payments.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

sealed interface PaymentsDestination {
  @Serializable
  data object Graph : PaymentsDestination, HedvigNavKey

  @Serializable
  data object Payments : PaymentsDestination, HedvigNavKey
}

internal sealed interface PaymentsDestinations {
  @Serializable
  data class Details(
    val memberChargeId: String?,
  ) : PaymentsDestinations, HedvigNavKey

  @Serializable
  data object History : PaymentsDestinations, HedvigNavKey

  @Serializable
  data object Discounts : PaymentsDestinations, HedvigNavKey

  @Serializable
  data object Forever : PaymentsDestinations, HedvigNavKey

  @Serializable
  data object MemberPaymentDetails : PaymentsDestinations, HedvigNavKey

  @Serializable
  data object ManualCharge : PaymentsDestinations, HedvigNavKey

  @Serializable
  data class ManualChargeSuccess(
    val showCancellationWarning: Boolean,
  ) : PaymentsDestinations, HedvigNavKey
}
