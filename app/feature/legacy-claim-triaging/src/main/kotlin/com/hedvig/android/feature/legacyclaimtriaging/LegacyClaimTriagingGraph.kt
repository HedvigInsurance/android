package com.hedvig.android.feature.legacyclaimtriaging

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.core.AppDestination
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.legacyClaimTriagingGraph(
  startClaimFlow: (backStackEntry: NavBackStackEntry, entryPointId: EntryPointId?) -> Unit,
  navigateUp: () -> Unit,
) {
  animatedComposable<AppDestination.LegacyClaimsTriaging> { backStackEntry ->
    val viewModel: LegacyClaimTriagingViewModel = koinViewModel()
    LegacyClaimTriagingDestination(
      viewModel = viewModel,
      startClaimFlow = { entryPointId -> startClaimFlow(backStackEntry, entryPointId) },
      navigateUp = navigateUp,
    )
  }
}
