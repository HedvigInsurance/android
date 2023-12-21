package com.hedvig.android.feature.help.center.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

@Serializable
data object HelpCenterDestination : Destination

internal sealed interface HelpCenterDestinations : Destination {
  @Serializable
  data object HelpCenter : HelpCenterDestinations

  @Serializable
  data class Topic(val id: String) : HelpCenterDestinations

  @Serializable
  data class Question(val id: String) : HelpCenterDestinations
}
