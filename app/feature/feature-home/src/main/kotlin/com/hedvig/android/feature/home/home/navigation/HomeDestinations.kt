package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.SuppressesChatPushNotification
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : HedvigNavKey, CrossSellEligibleDestination, SuppressesChatPushNotification

@Serializable
internal data class FirstVetKey(val sections: List<FirstVetSection>) : HedvigNavKey
