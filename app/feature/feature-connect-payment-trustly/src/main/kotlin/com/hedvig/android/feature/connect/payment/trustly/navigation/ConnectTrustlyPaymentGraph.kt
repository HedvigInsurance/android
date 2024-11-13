package com.hedvig.android.feature.connect.payment

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.connectPaymentGraph(
  navigator: Navigator,
  market: Market,
  onNavigateToNewConversation: () -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
) {
  navdestination<TrustlyDestination>(
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.connectPayment },
      navDeepLink { uriPattern = hedvigDeepLinkContainer.directDebit },
    ),
  ) {
    val viewModel: TrustlyViewModel = koinViewModel()
    TrustlyDestination(
      viewModel = viewModel,
      market = market,
      navigateUp = navigator::navigateUp,
      finishTrustlyFlow = navigator::popBackStack,
      onNavigateToNewConversation = onNavigateToNewConversation,
    )
  }
}
