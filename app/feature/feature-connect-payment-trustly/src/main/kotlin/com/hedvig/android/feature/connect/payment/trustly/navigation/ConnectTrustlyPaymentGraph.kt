package com.hedvig.android.feature.connect.payment

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.connectPaymentGraph(
  navigator: Navigator,
  market: Market,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigateToAdyenConnectPayment: () -> Unit,
) {
  composable<AppDestination.ConnectPayment>(
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.connectPayment },
      navDeepLink { uriPattern = hedvigDeepLinkContainer.directDebit },
    ),
  ) { _ ->
    LaunchedEffect(Unit) {
      if (market != Market.SE) {
        navigateToAdyenConnectPayment()
      }
    }
    val viewModel: TrustlyViewModel = koinViewModel { parametersOf(market) }
    TrustlyDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      finishTrustlyFlow = navigator::popBackStack,
    )
  }
}
