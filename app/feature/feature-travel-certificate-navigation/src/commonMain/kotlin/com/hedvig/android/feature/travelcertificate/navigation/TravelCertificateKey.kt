package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object TravelCertificateKey : HedvigNavKey, CrossSellEligibleDestination
