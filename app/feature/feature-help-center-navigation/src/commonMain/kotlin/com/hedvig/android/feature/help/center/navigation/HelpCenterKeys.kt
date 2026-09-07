package com.hedvig.android.feature.help.center.navigation

import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.shared.partners.deflect.DeflectData
import kotlinx.serialization.Serializable

@Serializable
data object HelpCenterKey : HedvigNavKey, CrossSellEligibleDestination

@Serializable
data class EmergencyKey(
  val deflectData: DeflectData,
) : HedvigNavKey
