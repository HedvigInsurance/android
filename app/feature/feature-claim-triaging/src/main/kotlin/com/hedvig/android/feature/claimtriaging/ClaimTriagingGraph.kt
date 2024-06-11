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
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.typeMapOf
import com.hedvig.android.navigation.compose.typePairOf
import com.hedvig.android.navigation.core.Navigator
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

sealed interface ClaimTriagingDestination {
  @Serializable
  object ClaimGroups : ClaimTriagingDestination

  @Serializable
  data class ClaimEntryPoints(
    val entryPoints: List<EntryPoint>,
  ) : ClaimTriagingDestination {
    companion object {
      val typeMap = typeMapOf<List<EntryPoint>>()
    }
  }

  @Serializable
  data class ClaimEntryPointOptions(
    val entryPointId: EntryPointId,
    val entryPointOptions: List<EntryPointOption>,
  ) : ClaimTriagingDestination {
    companion object {
      val typeMap = mapOf(
        typePairOf<EntryPointId>(),
        typePairOf<List<EntryPointOption>>(),
      )
    }
  }
}

fun NavGraphBuilder.claimTriagingDestinations(
  navigator: Navigator,
  windowSizeClass: WindowSizeClass,
  startClaimFlow: (NavBackStackEntry, ClaimFlowStep) -> Unit,
  closeClaimFlow: () -> Unit,
) {
  navdestination<ClaimTriagingDestination.ClaimGroups> { backStackEntry ->
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
  navdestination<ClaimTriagingDestination.ClaimEntryPoints>(
    typeMap = ClaimTriagingDestination.ClaimEntryPoints.typeMap,
  ) { backStackEntry ->
    val viewModel: ClaimEntryPointsViewModel = koinViewModel()
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
  navdestination<ClaimTriagingDestination.ClaimEntryPointOptions>(
    typeMap = ClaimTriagingDestination.ClaimEntryPointOptions.typeMap,
  ) { backStackEntry ->
    val viewModel: ClaimEntryPointOptionsViewModel = koinViewModel()
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
