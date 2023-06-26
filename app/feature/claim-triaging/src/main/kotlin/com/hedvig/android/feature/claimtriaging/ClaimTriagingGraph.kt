package com.hedvig.android.feature.claimtriaging

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.data.claimtriaging.ClaimGroup
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.data.claimtriaging.EntryPointOptionId
import com.hedvig.android.feature.claimtriaging.claimentrypointoptions.ClaimEntryPointOptionsDestination
import com.hedvig.android.feature.claimtriaging.claimentrypointoptions.ClaimEntryPointOptionsViewModel
import com.hedvig.android.feature.claimtriaging.claimentrypoints.ClaimEntryPointsDestination
import com.hedvig.android.feature.claimtriaging.claimentrypoints.ClaimEntryPointsViewModel
import com.hedvig.android.feature.claimtriaging.claimgroups.ClaimGroupsDestination
import com.hedvig.android.feature.claimtriaging.claimgroups.ClaimGroupsViewModel
import com.hedvig.android.navigation.compose.typed.ImmutableListSerializer
 import com.hedvig.android.navigation.compose.typed.SerializableImmutableList
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal sealed interface ClaimTriagingDestination : Destination {
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

fun NavGraphBuilder.claimTriagingGraph(
  navigator: Navigator,
  startClaimFlow: (NavBackStackEntry, EntryPointId?, EntryPointOptionId?) -> Unit,
) {
  animatedNavigation<AppDestination.ClaimsTriaging>(
    startDestination = createRoutePattern<ClaimTriagingDestination.ClaimGroups>(),
  ) {
    animatedComposable<ClaimTriagingDestination.ClaimGroups> { backStackEntry ->
      val viewModel: ClaimGroupsViewModel = koinViewModel()
      ClaimGroupsDestination(
        viewModel = viewModel,
        onClaimGroupWithEntryPointsSubmit = { claimGroup: ClaimGroup ->
          with(navigator) {
            backStackEntry.navigate(ClaimTriagingDestination.ClaimEntryPoints(claimGroup.entryPoints))
          }
        },
        startClaimFlow = { startClaimFlow(backStackEntry, null, null) },
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimTriagingDestination.ClaimEntryPoints> { backStackEntry ->
      val entryPoints: ImmutableList<EntryPoint> = this.entryPoints
      val viewModel: ClaimEntryPointsViewModel = koinViewModel { parametersOf(entryPoints) }
      ClaimEntryPointsDestination(
        viewModel = viewModel,
        onEntryPointWithOptionsSubmit = { entryPointId, entryPointOptions ->
          with(navigator) {
            backStackEntry.navigate(ClaimTriagingDestination.ClaimEntryPointOptions(entryPointId, entryPointOptions))
          }
        },
        startClaimFlow = { entryPointId: EntryPointId -> startClaimFlow(backStackEntry, entryPointId, null) },
        navigateUp = navigator::navigateUp,
      )
    }
    animatedComposable<ClaimTriagingDestination.ClaimEntryPointOptions> { backStackEntry ->
      val entryPointOptions: ImmutableList<EntryPointOption> = this.entryPointOptions
      val viewModel: ClaimEntryPointOptionsViewModel = koinViewModel { parametersOf(entryPointOptions) }
      ClaimEntryPointOptionsDestination(
        viewModel = viewModel,
        startClaimFlow = { entryPointOptionId ->
          startClaimFlow(backStackEntry, entryPointId, entryPointOptionId)
        },
        navigateUp = navigator::navigateUp,
      )
    }
  }
}
