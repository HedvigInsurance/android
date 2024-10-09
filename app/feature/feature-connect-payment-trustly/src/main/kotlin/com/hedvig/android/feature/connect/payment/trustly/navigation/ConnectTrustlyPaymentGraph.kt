package com.hedvig.android.feature.connect.payment

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import hedvig.resources.R
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.connectPaymentGraph(
  navigator: Navigator,
  market: Market,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
) {
  navdestination<AppDestination.ConnectPayment>(
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.connectPayment },
      navDeepLink { uriPattern = hedvigDeepLinkContainer.directDebit },
    ),
  ) {
    if (market == Market.SE) {
      val viewModel: TrustlyViewModel = koinViewModel()
      TrustlyDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        finishTrustlyFlow = navigator::popBackStack,
      )
    } else {
      Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
      ) {
        HedvigErrorSection(
          onButtonClick = { navigator.popBackStack() },
          modifier = Modifier.wrapContentHeight(),
          title = stringResource(R.string.something_went_wrong),
          subTitle = null,
          buttonText = stringResource(R.string.general_back_button),
        )
      }
    }
  }
}
