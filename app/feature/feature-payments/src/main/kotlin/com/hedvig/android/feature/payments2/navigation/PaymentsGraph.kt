package com.hedvig.android.feature.payments2.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.payments2.PaymentOverviewDestination
import com.hedvig.android.feature.payments2.PaymentOverviewViewModel
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
    startDestination = createRoutePattern<PaymentsDestinations2.Overview>(),
    deepLinks = listOf(
      navDeepLink { uriPattern = hedvigDeepLinkContainer.payments },
    ),
  ) {
    composable<PaymentsDestinations2.Overview> { backStackEntry ->
      val viewModel: PaymentOverviewViewModel = koinViewModel()
      PaymentOverviewDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        onPaymentHistoryClicked = { }, // TODO
        onChangeBankAccount = navigateToConnectPayment,
        onDiscountClicked = { }, // TODO
        onUpcomingPaymentClicked = {}, // TODO
      )
    }
  }
}
