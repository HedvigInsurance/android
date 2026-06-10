package com.hedvig.android.feature.help.center.navigation

import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.shared.partners.deflect.DeflectData
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data object HelpCenterKey : HedvigNavKey, CrossSellEligibleDestination

@Serializable
internal data class HelpCenterTopicKey(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer] */
  @SerialName("id")
  val topicId: String = "",
) : HedvigNavKey, CrossSellEligibleDestination

@Serializable
internal data class HelpCenterQuestionKey(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer] */
  @SerialName("id")
  val questionId: String = "",
) : HedvigNavKey, CrossSellEligibleDestination

@Serializable
internal data class EmergencyKey(
  val deflectData: DeflectData,
) : HedvigNavKey

@Serializable
internal data class FirstVetKey(val sections: List<FirstVetSection>) : HedvigNavKey

@Serializable
internal data object PuppyGuideKey : HedvigNavKey

@Serializable
internal data class PuppyGuideArticleKey(val storyName: String) : HedvigNavKey
