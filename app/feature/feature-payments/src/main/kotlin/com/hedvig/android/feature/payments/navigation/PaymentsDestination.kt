package com.hedvig.android.feature.payments.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.TopLevelTabRoot
import kotlinx.serialization.Serializable

@Serializable
data object PaymentsKey : HedvigNavKey, TopLevelTabRoot {
  override val topLevelTab: TopLevelTab = TopLevelTab.Payments
}

@Serializable
internal data class PaymentDetailsKey(
  val memberChargeId: String?,
) : HedvigNavKey

@Serializable
internal data object PaymentHistoryKey : HedvigNavKey

@Serializable
internal data object DiscountsKey : HedvigNavKey

@Serializable
internal data object ForeverKey : HedvigNavKey

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
