package com.hedvig.android.feature.connect.payment.adyen

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.connectAdyenPaymentGraph(navigator: Navigator) {
  navdestination<AppDestination.ConnectPaymentAdyen> {
    val viewModel: AdyenViewModel = koinViewModel()
    AdyenDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      finishAdyenFlow = navigator::popBackStack,
    )
  }
}
