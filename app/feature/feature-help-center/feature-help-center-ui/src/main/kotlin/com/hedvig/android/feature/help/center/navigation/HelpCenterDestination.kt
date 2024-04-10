package com.hedvig.android.feature.help.center.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

@Serializable
data object HelpCenterDestination : Destination

internal sealed interface HelpCenterDestinations : Destination {
  @Serializable
  data object HelpCenter : HelpCenterDestinations

  @Serializable
  data class Topic(
    val displayName: String,
    val topic: com.hedvig.android.feature.help.center.model.Topic,
  ) : HelpCenterDestinations

  @Serializable
  data class Question(
    val displayName: String,
    val question: com.hedvig.android.feature.help.center.model.Question,
  ) : HelpCenterDestinations

  @Serializable
  data class Emergency(val emergency: com.hedvig.android.feature.help.center.commonclaim.CommonClaim.Emergency) :
    HelpCenterDestinations

  @Serializable
  data class CommonClaim(val commonClaim: com.hedvig.android.feature.help.center.commonclaim.CommonClaim.Generic) :
    HelpCenterDestinations
}
