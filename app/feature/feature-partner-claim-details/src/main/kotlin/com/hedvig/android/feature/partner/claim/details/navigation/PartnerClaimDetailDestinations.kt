package com.hedvig.android.feature.partner.claim.details.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data class PartnerClaimOverviewDestination(
  val claimId: String,
) : Destination
