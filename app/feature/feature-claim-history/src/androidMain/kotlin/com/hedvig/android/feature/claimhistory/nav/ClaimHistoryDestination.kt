package com.hedvig.android.feature.claimhistory.nav

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.feature.claimhistory.ClaimHistoryDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.serialization.Serializable

@Serializable
data object ClaimHistoryKey : HedvigNavKey

fun EntryProviderScope<HedvigNavKey>.claimHistoryEntries(
  navigateUp: () -> Unit,
  navigateToClaimDetails: (claimId: String) -> Unit,
) {
  entry<ClaimHistoryKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    ClaimHistoryDestination(
      claimHistoryViewModel = metroViewModel(),
      navigateUp = navigateUp,
      navigateToClaimDetails = navigateToClaimDetails,
    )
  }
}
