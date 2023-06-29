package com.hedvig.android.feature.legacyclaimtriaging

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.data.claimtriaging.EntryPointId

fun NavGraphBuilder.legacyClaimTriagingGraph(
  startClaimFlow: (backStackEntry: NavBackStackEntry, entryPointId: EntryPointId?) -> Unit,
  navigateUp: () -> Unit,
) {
//  animatedComposable<AppDestination.LegacyClaimsTriaging> { backStackEntry ->
//    val viewModel: LegacyClaimTriagingViewModel = koinViewModel()
//    LegacyClaimTriagingDestination(
//      viewModel = viewModel,
//      startClaimFlow = { entryPointId -> startClaimFlow(backStackEntry, entryPointId) },
//      navigateUp = navigateUp,
//    )
//  }
}
