package com.hedvig.android.feature.connect.payment.trustly.ui

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
/**
 * @param showSuccessScreen whether connecting ends on a confirmation the member dismisses. A caller
 *   that confirms the connection itself, as the onboarding step does, sets this false and gets the
 *   member handed straight back instead. Deep links leave it at the default.
 */
data class TrustlyKey(
  val showSuccessScreen: Boolean = true,
) : HedvigNavKey
