package com.hedvig.android.feature.claimhistory.nav

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.claimhistory.ClaimHistoryDestination
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.navdestination
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

@Serializable
data object ClaimHistoryDestination : Destination

fun NavGraphBuilder.claimHistoryGraph(navigateToClaimDetails: (String) -> Unit) {
  navdestination<ClaimHistoryDestination> {
    ClaimHistoryDestination(
      navigateToClaimDetails = navigateToClaimDetails,
    )
  }
}

val profileBottomNavPermittedDestinations: List<KClass<out Destination>> = listOf(
  ClaimHistoryDestination::class,
)
