package com.hedvig.android.feature.claimhistory.nav

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.claimhistory.ClaimHistoryDestination
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.navdestination
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data object ClaimHistoryDestination : Destination

fun NavGraphBuilder.claimHistoryGraph(navigateUp: () -> Unit, navigateToClaimDetails: (String) -> Unit) {
  navdestination<ClaimHistoryDestination> {
    ClaimHistoryDestination(
      claimHistoryViewModel = koinViewModel(),
      navigateUp = navigateUp,
      navigateToClaimDetails = navigateToClaimDetails,
    )
  }
}

val profileBottomNavPermittedDestinations: List<KClass<out Destination>> = listOf(
  ClaimHistoryDestination::class,
)
