package com.hedvig.android.feature.connect.payment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import hedvig.resources.R
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
        Column {
          TopAppBarWithBack(onClick = dropUnlessResumed { navigator.navigateUp() })
          HedvigErrorSection(
            title = stringResource(R.string.MOVEINTENT_GENERIC_ERROR),
            subTitle = null,
            onButtonClick = dropUnlessResumed { onNavigateToNewConversation() },
            buttonText = stringResource(R.string.open_chat),
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
          )
        }
      }
    }
  }
}
