package com.hedvig.android.feature.claim.details.navigation

import com.hedvig.android.navigation.compose.Destination
import kotlinx.serialization.Serializable

sealed interface ClaimDetailDestinations {
  @Serializable
  data class ClaimOverviewDestination(val claimId: String) : ClaimDetailDestinations, Destination

  @Serializable
  data class AddFilesDestination(
    val targetUploadUrl: String,
    val initialFilesUri: List<String>,
  ) : ClaimDetailDestinations, Destination
}
