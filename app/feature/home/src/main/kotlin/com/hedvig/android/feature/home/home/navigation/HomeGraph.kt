package com.hedvig.android.feature.home.home.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
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
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.PaymentType
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import giraffe.HomeQuery
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.homeGraph(
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  nestedGraphs: NavGraphBuilder.() -> Unit,
  onStartChat: () -> Unit,
  onStartClaim: (NavBackStackEntry) -> Unit,
  startMovingFlow: () -> Unit,
  onHowClaimsWorkClick: (List<HomeQuery.HowClaimsWork>) -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  navigateToPayinScreen: (PaymentType) -> Unit,
  tryOpenUri: (Uri) -> Unit,
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
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      HomeDestination(
        uiState = uiState,
        reload = viewModel::reload,
        onStartChat = onStartChat,
        onClaimDetailCardClicked = { claimId: String ->
          viewModel.onClaimDetailCardClicked(claimId)
          navController.navigate(HomeDestinations.ClaimDetailDestination(claimId))
        },
        onClaimDetailCardShown = viewModel::onClaimDetailCardShown,
        onPaymentCardClicked = { paymentType ->
          viewModel.onPaymentCardClicked()
          navigateToPayinScreen(paymentType)
        },
        onPaymentCardShown = viewModel::onPaymentCardShown,
        onHowClaimsWorkClick = onHowClaimsWorkClick,
        onStartClaim = { onStartClaim(backStackEntry) },
        onStartMovingFlow = startMovingFlow,
        onGenerateTravelCertificateClicked = onGenerateTravelCertificateClicked,
        onOpenCommonClaim = { commonClaimsData ->
          navController.navigate(HomeDestinations.CommonClaimDestination(commonClaimsData))
        },
        tryOpenUri = tryOpenUri,
        imageLoader = imageLoader,
      )
    }
    claimDetailGraph(
      navController = navController,
      navigateToChat = onStartChat,
    )
    commonClaimGraph(
      navController = navController,
      imageLoader = imageLoader,
      hAnalytics = hAnalytics,
      startClaimsFlow = onStartClaim,
    )
    nestedGraphs()
  }
}
