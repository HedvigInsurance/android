package com.hedvig.android.feature.help.center.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import com.hedvig.android.shared.partners.deflect.DeflectData
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data object HelpCenterDestination : HedvigNavKey

internal sealed interface HelpCenterDestinations {
  @Serializable
  data object HelpCenter : HelpCenterDestinations, HedvigNavKey

  @Serializable
  data class Topic(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer] */
    @SerialName("id")
    val topicId: String = "",
  ) : HelpCenterDestinations, HedvigNavKey

  @Serializable
  data class Question(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer] */
    @SerialName("id")
    val questionId: String = "",
  ) : HelpCenterDestinations, HedvigNavKey

  @Serializable
  data class Emergency(
    val deflectData: DeflectData,
  ) : HelpCenterDestinations, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<DeflectData>())
    }
  }

  @Serializable
  data class FirstVet(val sections: List<FirstVetSection>) : HelpCenterDestinations, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<FirstVetSection>>())
    }
  }

  @Serializable
  data object PuppyGuide : HelpCenterDestinations, HedvigNavKey

  @Serializable
  data class PuppyGuideArticle(val storyName: String) : HelpCenterDestinations, HedvigNavKey
}

val helpCenterCrossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = listOf(
  HelpCenterDestinations.HelpCenter::class,
  HelpCenterDestinations.Topic::class,
  HelpCenterDestinations.Question::class,
)
