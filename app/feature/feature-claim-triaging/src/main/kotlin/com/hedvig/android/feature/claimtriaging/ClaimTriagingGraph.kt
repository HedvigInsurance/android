package com.hedvig.android.feature.claimtriaging

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
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
import com.hedvig.android.navigation.compose.typed.SerializableImmutableList
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createNavArguments
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.decodeArguments
import com.kiwi.navigationcompose.typed.registerDestinationType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

sealed interface ClaimTriagingDestination : Destination {
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
  sharedTransitionScope: SharedTransitionScope,
  navigator: Navigator,
  windowSizeClass: WindowSizeClass,
  startClaimFlow: (NavBackStackEntry, ClaimFlowStep) -> Unit,
  closeClaimFlow: () -> Unit,
) {
  with(sharedTransitionScope) {
    animatedComposable<ClaimTriagingDestination.ClaimGroups> { backStackEntry, _ ->
      val viewModel: ClaimGroupsViewModel = koinViewModel()
      ClaimGroupsDestination(
        animatedContentScope = this,
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
    animatedComposable<ClaimTriagingDestination.ClaimEntryPoints> { backStackEntry, destination ->
      val entryPoints: ImmutableList<EntryPoint> = destination.entryPoints
      val viewModel: ClaimEntryPointsViewModel = koinViewModel { parametersOf(entryPoints) }
      ClaimEntryPointsDestination(
        animatedContentScope = this,
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
    animatedComposable<ClaimTriagingDestination.ClaimEntryPointOptions> { backStackEntry, destination ->
      val entryPointId: EntryPointId = destination.entryPointId
      val entryPointOptions: ImmutableList<EntryPointOption> = destination.entryPointOptions
      val viewModel: ClaimEntryPointOptionsViewModel = koinViewModel { parametersOf(entryPointId, entryPointOptions) }
      ClaimEntryPointOptionsDestination(
        animatedContentScope = this,
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
}

internal inline fun <reified T : Destination> NavGraphBuilder.animatedComposable(
  deepLinks: List<NavDeepLink> = emptyList(),
  noinline enterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
  noinline exitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
  noinline popEnterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
  noinline popExitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
  noinline content: @Composable AnimatedContentScope.(NavBackStackEntry, T) -> Unit,
) {
  val serializer = serializer<T>()
  registerDestinationType(T::class, serializer)
  composable(
    route = createRoutePattern(serializer),
    arguments = createNavArguments(serializer),
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    popEnterTransition = popEnterTransition,
    popExitTransition = popExitTransition,
    deepLinks = deepLinks,
  ) { navBackStackEntry ->
    val t = decodeArguments(serializer, navBackStackEntry)
    content(navBackStackEntry, t)
  }
}
