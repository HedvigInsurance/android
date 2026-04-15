package com.hedvig.android.feature.connect.payment

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.connect.payment.trustly.TrustlyPayoutViewModel
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyPayoutDestination
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.connectPaymentGraph(
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
) {
  navdestination<TrustlyDestination>(
    deepLinks = navDeepLinks(
      hedvigDeepLinkContainer.connectPayment,
      hedvigDeepLinkContainer.directDebit,
    ),
  ) {
    val viewModel: TrustlyViewModel = koinViewModel()
    TrustlyDestination(
      viewModel = viewModel,
      navigateUp = navController::navigateUp,
      finishTrustlyFlow = navController::popBackStack,
    )
  }

  navdestination<TrustlyPayoutDestination> {
    val viewModel: TrustlyPayoutViewModel = koinViewModel()
    TrustlyDestination(
      viewModel = viewModel,
      navigateUp = navController::navigateUp,
      finishTrustlyFlow = navController::popBackStack,
    )
  }
}
