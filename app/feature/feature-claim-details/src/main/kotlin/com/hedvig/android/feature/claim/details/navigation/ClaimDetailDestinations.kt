package com.hedvig.android.feature.claim.details.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ClaimDetailDestination {
  @Serializable
  data class ClaimOverviewDestination(
    /**
     * The ID to the claim. Must match the name of the param inside in HedvigDeepLinkContainer
     */
    @SerialName("claimId")
    val claimId: String,
  ) : ClaimDetailDestination, Destination
}

internal sealed interface ClaimDetailInternalDestination {
  @Serializable
  data class AddFilesDestination(
    val targetUploadUrl: String,
    val initialFilesUri: List<String>,
  ) : ClaimDetailInternalDestination, Destination
}
