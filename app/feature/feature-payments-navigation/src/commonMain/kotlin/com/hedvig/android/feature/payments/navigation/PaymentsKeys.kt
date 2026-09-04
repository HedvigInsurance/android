package com.hedvig.android.feature.payments.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.TopLevelTabRoot
import kotlinx.serialization.Serializable

@Serializable
data object PaymentsKey : HedvigNavKey, TopLevelTabRoot {
  override val topLevelTab: TopLevelTab = TopLevelTab.Payments
}

/** A null [memberChargeId] shows the member's upcoming charge. */
@Serializable
data class PaymentDetailsKey(
  val memberChargeId: String?,
) : HedvigNavKey
