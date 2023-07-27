package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimsData
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface HomeDestinations : Destination {
  @Serializable
  data class ClaimDetailDestination(val claimId: String) : HomeDestinations

  @Serializable
  data class CommonClaimDestination(val claimsData: CommonClaimsData) : Destination
}
