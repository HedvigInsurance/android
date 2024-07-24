package com.hedvig.android.feature.claimtriaging

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.ClaimGroup
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.feature.claimtriaging.claimentrypointoptions.ClaimEntryPointOptionsDestination
import com.hedvig.android.feature.claimtriaging.claimentrypointoptions.ClaimEntryPointOptionsViewModel
import com.hedvig.android.feature.claimtriaging.claimentrypoints.ClaimEntryPointsDestination
import com.hedvig.android.feature.claimtriaging.claimentrypoints.ClaimEntryPointsViewModel
import com.hedvig.android.feature.claimtriaging.claimgroups.ClaimGroupsDestination
import com.hedvig.android.feature.claimtriaging.claimgroups.ClaimGroupsViewModel
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.composable
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

sealed interface ClaimTriagingDestination : Destination {
  @Serializable
  object ClaimGroups : ClaimTriagingDestination

  @Serializable
  data class ClaimEntryPoints(
    val entryPoints: List<EntryPoint>,
  ) : ClaimTriagingDestination

  @Serializable
  data class ClaimEntryPointOptions(
    val entryPointId: EntryPointId,
    val entryPointOptions: List<EntryPointOption>,
  ) : ClaimTriagingDestination
}

fun NavGraphBuilder.claimTriagingDestinations(
  navigator: Navigator,
  windowSizeClass: WindowSizeClass,
  startClaimFlow: (NavBackStackEntry, ClaimFlowStep) -> Unit,
  closeClaimFlow: () -> Unit,
) {
  composable<ClaimTriagingDestination.ClaimGroups> { backStackEntry ->
    val viewModel: ClaimGroupsViewModel = koinViewModel()
    ClaimGroupsDestination(
      viewModel = viewModel,
      onClaimGroupWithEntryPointsSubmit = { claimGroup: ClaimGroup ->
        with(navigator) {
          backStackEntry.navigate(ClaimTriagingDestination.ClaimEntryPoints(claimGroup.entryPoints))
        }
      },
      startClaimFlow = { claimFlowStep ->
        viewModel.handledNextStepNavigation()
        startClaimFlow(backStackEntry, claimFlowStep)
      },
      navigateUp = navigator::navigateUp,
      closeClaimFlow = closeClaimFlow,
      windowSizeClass = windowSizeClass,
    )
  }
  composable<ClaimTriagingDestination.ClaimEntryPoints> { backStackEntry ->
    val entryPoints: List<EntryPoint> = this.entryPoints
    val viewModel: ClaimEntryPointsViewModel = koinViewModel { parametersOf(entryPoints) }
    ClaimEntryPointsDestination(
      viewModel = viewModel,
      onEntryPointWithOptionsSubmit = { entryPointId, entryPointOptions ->
        with(navigator) {
          backStackEntry.navigate(ClaimTriagingDestination.ClaimEntryPointOptions(entryPointId, entryPointOptions))
        }
      },
      startClaimFlow = { claimFlowStep ->
        viewModel.handledNextStepNavigation()
        startClaimFlow(backStackEntry, claimFlowStep)
      },
      navigateUp = navigator::navigateUp,
      closeClaimFlow = closeClaimFlow,
      windowSizeClass = windowSizeClass,
    )
  }
  composable<ClaimTriagingDestination.ClaimEntryPointOptions> { backStackEntry ->
    val entryPointId: EntryPointId = this.entryPointId
    val entryPointOptions: List<EntryPointOption> = this.entryPointOptions
    val viewModel: ClaimEntryPointOptionsViewModel = koinViewModel { parametersOf(entryPointId, entryPointOptions) }
    ClaimEntryPointOptionsDestination(
      viewModel = viewModel,
      startClaimFlow = { claimFlowStep ->
        viewModel.handledNextStepNavigation()
        startClaimFlow(backStackEntry, claimFlowStep)
      },
      navigateUp = navigator::navigateUp,
      closeClaimFlow = closeClaimFlow,
      windowSizeClass = windowSizeClass,
    )
  }
}
