package com.hedvig.android.feature.connect.payment

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.connectPaymentGraph(navController: NavController, hedvigDeepLinkContainer: HedvigDeepLinkContainer) {
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
}
