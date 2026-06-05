package com.hedvig.android.feature.claim.details.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.SuppressesChatPushNotification
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimDetailsKey(
  /**
   * The ID to the claim. Must match the name of the param inside in HedvigDeepLinkContainer
   */
  @SerialName("claimId")
  val claimId: String,
) : HedvigNavKey, SuppressesChatPushNotification

@Serializable
internal data class AddFilesKey(
  val targetUploadUrl: String,
  val initialFilesUri: List<String>,
) : HedvigNavKey
