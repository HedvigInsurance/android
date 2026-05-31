package com.hedvig.android.feature.connect.payment

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import dev.zacsweers.metrox.viewmodel.metroViewModel

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
    val viewModel: TrustlyViewModel = metroViewModel()
    TrustlyDestination(
      viewModel = viewModel,
      navigateUp = navController::navigateUp,
      finishTrustlyFlow = navController::popBackStack,
    )
  }
}
