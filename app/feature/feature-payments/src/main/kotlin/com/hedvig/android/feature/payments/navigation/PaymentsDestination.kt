package com.hedvig.android.feature.payments.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
internal data object PaymentHistoryKey : HedvigNavKey

@Serializable
internal data object DiscountsKey : HedvigNavKey

@Serializable
internal data object MemberPaymentDetailsKey : HedvigNavKey

@Serializable
internal data object ManualChargeKey : HedvigNavKey

@Serializable
internal data class ManualChargeSuccessKey(
  val showCancellationWarning: Boolean,
) : HedvigNavKey

@Serializable
internal data class PaymentDetailExplanationKey(
  val title: String,
  val body: String,
) : HedvigNavKey
