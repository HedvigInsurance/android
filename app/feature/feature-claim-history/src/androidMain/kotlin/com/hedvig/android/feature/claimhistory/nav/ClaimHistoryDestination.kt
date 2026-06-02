package com.hedvig.android.feature.claimhistory.nav

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.claimhistory.ClaimHistoryDestination
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.navdestination
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable

@Serializable
data object ClaimHistoryDestination : Destination

fun EntryProviderScope<Destination>.claimHistoryGraph(
  navigateUp: () -> Unit,
  navigateToClaimDetails: (claimId: String) -> Unit,
) {
  navdestination<ClaimHistoryDestination> {
    ClaimHistoryDestination(
      claimHistoryViewModel = metroViewModel(),
      navigateUp = navigateUp,
      navigateToClaimDetails = navigateToClaimDetails,
    )
  }
}

val profileBottomNavPermittedDestinations: List<KClass<out Destination>> = listOf(
  ClaimHistoryDestination::class,
)
