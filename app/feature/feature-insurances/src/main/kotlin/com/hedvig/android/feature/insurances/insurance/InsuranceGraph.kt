package com.hedvig.android.feature.insurances.insurance

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailDestination
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.insurances.navigation.InsurancesDestinations
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsDestination
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createNavArguments
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.decodeArguments
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.registerDestinationType
import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.insuranceGraph(
  sharedTransitionScope: SharedTransitionScope,
  nestedGraphs: NavGraphBuilder.() -> Unit,
  navigator: Navigator,
  openChat: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
  startMovingFlow: (NavBackStackEntry) -> Unit,
  startTerminationFlow: (backStackEntry: NavBackStackEntry, cancelInsuranceData: CancelInsuranceData) -> Unit,
  startEditCoInsured: (backStackEntry: NavBackStackEntry, contractId: String) -> Unit,
  startEditCoInsuredAddMissingInfo: (backStackEntry: NavBackStackEntry, contractId: String) -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  imageLoader: ImageLoader,
) {
  navigation<InsurancesDestination.Graph>(
    startDestination = createRoutePattern<InsurancesDestination.Insurances>(),
  ) {
    nestedGraphs()
    animatedComposable<InsurancesDestination.Insurances>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.insurances },
        navDeepLink { uriPattern = hedvigDeepLinkContainer.contractWithoutContractId },
      ),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry, _ ->
      val viewModel: InsuranceViewModel = koinViewModel()
      sharedTransitionScope.InsuranceDestination(
        animatedContentScope = this,
        viewModel = viewModel,
        onInsuranceCardClick = { contractId: String ->
          with(navigator) { backStackEntry.navigate(InsurancesDestinations.InsuranceContractDetail(contractId)) }
        },
        onCrossSellClick = openUrl,
        navigateToCancelledInsurances = {
          with(navigator) { backStackEntry.navigate(InsurancesDestinations.TerminatedInsurances) }
        },
        imageLoader = imageLoader,
      )
    }
    animatedComposable<InsurancesDestinations.InsuranceContractDetail>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.contract },
      ),
    ) { backStackEntry, contractDetail ->
      val viewModel: ContractDetailViewModel = koinViewModel { parametersOf(contractDetail.contractId) }
      sharedTransitionScope.ContractDetailDestination(
        animatedContentScope = this,
        viewModel = viewModel,
        onEditCoInsuredClick = { contractId: String -> startEditCoInsured(backStackEntry, contractId) },
        onMissingInfoClick = { contractId -> startEditCoInsuredAddMissingInfo(backStackEntry, contractId) },
        onChangeAddressClick = { startMovingFlow(backStackEntry) },
        onCancelInsuranceClick = { cancelInsuranceData: CancelInsuranceData ->
          startTerminationFlow(
            backStackEntry,
            cancelInsuranceData,
          )
        },
        openChat = { openChat(backStackEntry) },
        openUrl = openUrl,
        navigateUp = navigator::navigateUp,
        navigateBack = navigator::popBackStack,
        imageLoader = imageLoader,
      )
    }
    composable<InsurancesDestinations.TerminatedInsurances> { backStackEntry ->
      val viewModel: TerminatedContractsViewModel = koinViewModel()
      TerminatedContractsDestination(
        viewModel = viewModel,
        navigateToContractDetail = { contractId: String ->
          with(navigator) { backStackEntry.navigate(InsurancesDestinations.InsuranceContractDetail(contractId)) }
        },
        navigateUp = navigator::navigateUp,
        imageLoader = imageLoader,
      )
    }
  }
}

private inline fun <reified T : Destination> NavGraphBuilder.animatedComposable(
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
