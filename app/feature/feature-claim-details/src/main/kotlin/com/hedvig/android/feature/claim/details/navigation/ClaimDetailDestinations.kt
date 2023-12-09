package com.hedvig.android.feature.claim.details.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

@Serializable
data class ClaimDetailsDestination(val claimId: String) : Destination
