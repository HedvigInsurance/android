package com.hedvig.android.feature.claimtriaging

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.uidata.SerializableImmutableList
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
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.Navigator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

sealed interface ClaimTriagingDestination {
  @Serializable
  object ClaimGroups : ClaimTriagingDestination

  @Serializable
  data class ClaimEntryPoints(
    val entryPoints: SerializableImmutableList<EntryPoint>,
  ) : ClaimTriagingDestination

  @Serializable
  data class ClaimEntryPointOptions(
    val entryPointId: EntryPointId,
    val entryPointOptions: SerializableImmutableList<EntryPointOption>,
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
  composable<ClaimTriagingDestination.ClaimEntryPoints> { backStackEntry, destination ->
    val entryPoints: ImmutableList<EntryPoint> = destination.entryPoints
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
  composable<ClaimTriagingDestination.ClaimEntryPointOptions> { backStackEntry, destination ->
    val entryPointId: EntryPointId = destination.entryPointId
    val entryPointOptions: ImmutableList<EntryPointOption> = destination.entryPointOptions
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
