package com.hedvig.android.feature.connect.payment.adyen

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.connectAdyenPaymentGraph(navigator: Navigator) {
  composable<AppDestination.ConnectPaymentAdyen> {
    val viewModel: AdyenViewModel = koinViewModel()
    AdyenDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      finishAdyenFlow = navigator::popBackStack,
    )
  }
}
