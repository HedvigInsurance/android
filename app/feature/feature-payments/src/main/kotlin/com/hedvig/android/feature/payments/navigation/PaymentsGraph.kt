package com.hedvig.android.feature.payments.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.payments.PaymentDestination
import com.hedvig.android.feature.payments.PaymentViewModel
import com.hedvig.android.feature.payments.history.PaymentHistoryDestination
import com.hedvig.android.feature.payments.history.PaymentHistoryViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.paymentsGraph(
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigateToConnectPayment: () -> Unit,
) {
  navigation<AppDestination.PaymentInfo>(
    startDestination = createRoutePattern<PaymentsDestinations.PaymentInfo>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.payments },
    ),
  ) {
    composable<PaymentsDestinations.PaymentInfo> { backStackEntry ->
      val viewModel: PaymentViewModel = koinViewModel()
      PaymentDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        onPaymentHistoryClicked = {
          with(navigator) { backStackEntry.navigate(PaymentsDestinations.PaymentHistory) }
        },
        onChangeBankAccount = navigateToConnectPayment,
      )
    }
    composable<PaymentsDestinations.PaymentHistory> {
      val viewModel: PaymentHistoryViewModel = koinViewModel()
      PaymentHistoryDestination(
        viewModel = viewModel,
        onNavigateUp = navigator::navigateUp,
        onNavigateBack = navigator::popBackStack,
      )
    }
  }
}
