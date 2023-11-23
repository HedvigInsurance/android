package com.hedvig.android.feature.home.home.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.home.claims.commonclaim.commonClaimGraph
import com.hedvig.android.feature.home.home.ui.HomeDestination
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.hanalytics.HAnalytics
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.homeGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  onStartChat: (NavBackStackEntry) -> Unit,
  onStartClaim: (NavBackStackEntry) -> Unit,
  startMovingFlow: (NavBackStackEntry) -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  navigateToClaimDetails: (NavBackStackEntry, claimId: String) -> Unit,
  navigateToPayinScreen: () -> Unit,
  navigateToContractDetail: (contractId: String) -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  hAnalytics: HAnalytics,
) {
  navigation<TopLevelGraph.HOME>(
    startDestination = createRoutePattern<AppDestination.TopLevelDestination.Home>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.home },
    ),
  ) {
    composable<AppDestination.TopLevelDestination.Home>(
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: HomeViewModel = koinViewModel()
      HomeDestination(
        viewModel = viewModel,
        onStartChat = { onStartChat(backStackEntry) },
        onClaimDetailCardClicked = { claimId: String ->
          navigateToClaimDetails(backStackEntry, claimId)
        },
        navigateToConnectPayment = navigateToPayinScreen,
        navigateToContractDetail = navigateToContractDetail,
        onStartClaim = { onStartClaim(backStackEntry) },
        onStartMovingFlow = { startMovingFlow(backStackEntry) },
        onGenerateTravelCertificateClicked = onGenerateTravelCertificateClicked,
        onOpenCommonClaim = { commonClaimsData ->
          with(navigator) { backStackEntry.navigate(HomeDestinations.CommonClaimDestination(commonClaimsData)) }
        },
        openUrl = openUrl,
        openAppSettings = openAppSettings,
      )
    }
    commonClaimGraph(
      imageLoader = imageLoader,
      hAnalytics = hAnalytics,
      navigateUp = navigator::navigateUp,
      startClaimsFlow = onStartClaim,
    )
    nestedGraphs()
  }
}
