package com.hedvig.android.feature.help.center.navigation

import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data object HelpCenterDestination : Destination

internal sealed interface HelpCenterDestinations {
  @Serializable
  data object HelpCenter : HelpCenterDestinations, Destination

  @Serializable
  data class Topic(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer] */
    @SerialName("id")
    val topicId: String = "",
  ) : HelpCenterDestinations, Destination

  @Serializable
  data class Question(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer] */
    @SerialName("id")
    val questionId: String = "",
  ) : HelpCenterDestinations, Destination

  @Serializable
  data class Emergency(
    val emergencyNumber: String?,
    val emergencyUrl: String?,
    val preferredPartnerImageHeight: Int?,
  ) : HelpCenterDestinations, Destination

  @Serializable
  data class FirstVet(val sections: List<FirstVetSection>) : HelpCenterDestinations, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<FirstVetSection>>())
    }
  }
}

val helpCenterCrossSellBottomSheetPermittingDestinations: List<KClass<out Destination>> = listOf(
  HelpCenterDestinations.HelpCenter::class,
  HelpCenterDestinations.Topic::class,
  HelpCenterDestinations.Question::class,
)
