package com.hedvig.android.feature.claim.details.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

sealed interface ClaimDetailDestinations : Destination {
  @Serializable
  data class ClaimOverviewDestination(val claimId: String) : ClaimDetailDestinations

  @Serializable
  data class AddFilesDestination(
    val targetUploadUrl: String,
    val initialFileUri: String
  ) : ClaimDetailDestinations
}
