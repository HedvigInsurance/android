package com.hedvig.android.feature.home.home.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.home.claimdetail.claimDetailGraph
import com.hedvig.android.feature.home.claims.commonclaim.commonClaimGraph
import com.hedvig.android.feature.home.home.ui.HomeDestination
import com.hedvig.android.feature.home.home.ui.HomeViewModel
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.hanalytics.HAnalytics
import com.kiwi.navigationcompose.typed.createRoutePattern
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.homeGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  onStartChat: () -> Unit,
  onStartClaim: (NavBackStackEntry) -> Unit,
  startMovingFlow: () -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  navigateToPayinScreen: () -> Unit,
  openAppSettings: () -> Unit,
  openUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  hAnalytics: HAnalytics,
) {
  animatedNavigation<TopLevelGraph.HOME>(
    startDestination = createRoutePattern<AppDestination.TopLevelDestination.Home>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.home },
    ),
  ) {
    animatedComposable<AppDestination.TopLevelDestination.Home>(
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: HomeViewModel = koinViewModel()
      HomeDestination(
        viewModel = viewModel,
        onStartChat = onStartChat,
        onClaimDetailCardClicked = { claimId: String ->
          with(navigator) { backStackEntry.navigate(HomeDestinations.ClaimDetailDestination(claimId)) }
        },
        navigateToConnectPayment = navigateToPayinScreen,
        onStartClaim = { onStartClaim(backStackEntry) },
        onStartMovingFlow = startMovingFlow,
        onGenerateTravelCertificateClicked = onGenerateTravelCertificateClicked,
        onOpenCommonClaim = { commonClaimsData ->
          with(navigator) { backStackEntry.navigate(HomeDestinations.CommonClaimDestination(commonClaimsData)) }
        },
        openUrl = openUrl,
        openAppSettings = openAppSettings,
      )
    }
    claimDetailGraph(
      navigateUp = navigator::navigateUp,
      navigateToChat = onStartChat,
    )
    commonClaimGraph(
      imageLoader = imageLoader,
      hAnalytics = hAnalytics,
      navigateUp = navigator::navigateUp,
      startClaimsFlow = onStartClaim,
    )
    nestedGraphs()
  }
}
