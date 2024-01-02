package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.feature.home.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.emergency.EmergencyData
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface HomeDestinations : Destination {
  @Serializable
  data class CommonClaimDestination(val claimsData: CommonClaimsData) : Destination

  @Serializable
  data class EmergencyDestination(val emergencyData: EmergencyData) : Destination
}
