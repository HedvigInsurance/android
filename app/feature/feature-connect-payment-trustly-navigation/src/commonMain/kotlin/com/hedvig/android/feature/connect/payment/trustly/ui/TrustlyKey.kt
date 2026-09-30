package com.hedvig.android.feature.connect.payment.trustly.ui

import com.hedvig.android.feature.payin.account.navigation.PayinAccountKey
import com.hedvig.android.navigation.common.DeepLinkAncestry
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import kotlinx.serialization.Serializable

/**
 * @param showSuccessScreen whether connecting ends on a confirmation the member dismisses. A caller
 *   that confirms the connection itself, as the onboarding step does, sets this false and gets the
 *   member handed straight back instead. Deep links leave it at the default.
 */
@Serializable
data class TrustlyKey(
  val showSuccessScreen: Boolean = true,
) : HedvigNavKey, DeepLinkAncestry {
  override val owningTab = TopLevelTab.Payments
  override val syntheticParents = listOf(PayinAccountKey)
}
