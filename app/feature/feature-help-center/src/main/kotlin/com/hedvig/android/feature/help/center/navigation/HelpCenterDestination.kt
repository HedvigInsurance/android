package com.hedvig.android.feature.help.center.navigation

import com.hedvig.android.feature.help.center.data.FirstVetSection
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
  data class Emergency(val emergencyNumber: String?) :
    HelpCenterDestinations

  @Serializable
  data class FirstVet(val sections: List<FirstVetSection>) :
    HelpCenterDestinations
}
