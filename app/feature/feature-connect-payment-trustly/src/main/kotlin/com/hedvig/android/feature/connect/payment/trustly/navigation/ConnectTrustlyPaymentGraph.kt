package com.hedvig.android.feature.connect.payment

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.connectTrustlyPaymentGraph(
  navigator: Navigator,
) {
  composable<AppDestination.ConnectPaymentTrustly>() {
    val viewModel: TrustlyViewModel = koinViewModel()
    TrustlyDestination(
      viewModel = viewModel,
      navigateUp = navigator::navigateUp,
      finishTrustlyFlow = navigator::popBackStack,
    )
  }
}
