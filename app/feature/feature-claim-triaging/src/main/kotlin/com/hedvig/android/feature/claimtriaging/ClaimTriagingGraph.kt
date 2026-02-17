package com.hedvig.android.feature.claimtriaging

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.compose.ui.dropUnlessResumed
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
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navdestination
import androidx.navigation.NavController
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

sealed interface ClaimTriagingDestination {
  @Serializable
  object ClaimGroups : ClaimTriagingDestination, Destination

  @Serializable
  data class ClaimEntryPoints(
    val entryPoints: List<EntryPoint>,
  ) : ClaimTriagingDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<EntryPoint>>())
    }
  }

  @Serializable
  data class ClaimEntryPointOptions(
    val entryPointId: EntryPointId,
    val entryPointOptions: List<EntryPointOption>,
  ) : ClaimTriagingDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<EntryPointId>(),
        typeOf<List<EntryPointOption>>(),
      )
    }
  }
}

fun NavGraphBuilder.claimTriagingDestinations(
  navController: NavController,
  windowSizeClass: WindowSizeClass,
  startClaimFlow: (ClaimFlowStep) -> Unit,
  closeClaimFlow: () -> Unit,
) {
  navdestination<ClaimTriagingDestination.ClaimGroups> {
    val viewModel: ClaimGroupsViewModel = koinViewModel()
    ClaimGroupsDestination(
      viewModel = viewModel,
      onClaimGroupWithEntryPointsSubmit = dropUnlessResumed { claimGroup: ClaimGroup ->
        navController.navigate(ClaimTriagingDestination.ClaimEntryPoints(claimGroup.entryPoints))
      },
      startClaimFlow = { claimFlowStep ->
        viewModel.handledNextStepNavigation()
        startClaimFlow(claimFlowStep)
      },
      navigateUp = navController::navigateUp,
      closeClaimFlow = dropUnlessResumed { closeClaimFlow() },
      windowSizeClass = windowSizeClass,
    )
  }
  navdestination<ClaimTriagingDestination.ClaimEntryPoints>(
    ClaimTriagingDestination.ClaimEntryPoints,
  ) {
    val entryPoints: List<EntryPoint> = this.entryPoints
    val viewModel: ClaimEntryPointsViewModel = koinViewModel { parametersOf(entryPoints) }
    ClaimEntryPointsDestination(
      viewModel = viewModel,
      onEntryPointWithOptionsSubmit = dropUnlessResumed { entryPointId, entryPointOptions ->
        navController.navigate(ClaimTriagingDestination.ClaimEntryPointOptions(entryPointId, entryPointOptions))
      },
      startClaimFlow = { claimFlowStep ->
        viewModel.handledNextStepNavigation()
        startClaimFlow(claimFlowStep)
      },
      navigateUp = navController::navigateUp,
      closeClaimFlow = dropUnlessResumed { closeClaimFlow() },
      windowSizeClass = windowSizeClass,
    )
  }
  navdestination<ClaimTriagingDestination.ClaimEntryPointOptions>(
    ClaimTriagingDestination.ClaimEntryPointOptions,
  ) {
    val entryPointId: EntryPointId = this.entryPointId
    val entryPointOptions: List<EntryPointOption> = this.entryPointOptions
    val viewModel: ClaimEntryPointOptionsViewModel = koinViewModel { parametersOf(entryPointId, entryPointOptions) }
    ClaimEntryPointOptionsDestination(
      viewModel = viewModel,
      startClaimFlow = { claimFlowStep ->
        viewModel.handledNextStepNavigation()
        startClaimFlow(claimFlowStep)
      },
      navigateUp = navController::navigateUp,
      closeClaimFlow = dropUnlessResumed { closeClaimFlow() },
      windowSizeClass = windowSizeClass,
    )
  }
}
