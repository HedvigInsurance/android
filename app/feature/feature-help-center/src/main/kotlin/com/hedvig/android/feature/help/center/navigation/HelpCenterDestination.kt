package com.hedvig.android.feature.help.center.navigation

import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data object HelpCenterDestination : Destination

internal sealed interface HelpCenterDestinations {
  @Serializable
  data object HelpCenter : HelpCenterDestinations, Destination

  @Serializable
  data object Choose

  @Serializable
  data class Topic(
    val displayName: String,
    val topic: com.hedvig.android.feature.help.center.model.Topic,
  ) : HelpCenterDestinations, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<com.hedvig.android.feature.help.center.model.Topic>())
    }
  }

  @Serializable
  data class Question(
    val displayName: String,
    val question: com.hedvig.android.feature.help.center.model.Question,
  ) : HelpCenterDestinations, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<com.hedvig.android.feature.help.center.model.Question>())
    }
  }

  @Serializable
  data class Emergency(val emergencyNumber: String?, val emergencyUrl: String?) : HelpCenterDestinations, Destination

  @Serializable
  data class FirstVet(val sections: List<FirstVetSection>) : HelpCenterDestinations, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<FirstVetSection>>())
    }
  }
}
