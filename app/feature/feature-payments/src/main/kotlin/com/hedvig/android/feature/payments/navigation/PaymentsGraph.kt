package com.hedvig.android.feature.payments.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.details.PaymentDetailsDestination
import com.hedvig.android.feature.payments.discounts.DiscountsDestination
import com.hedvig.android.feature.payments.history.PaymentHistoryDestination
import com.hedvig.android.feature.payments.overview.PaymentOverviewDestination
import com.hedvig.android.feature.payments.overview.PaymentOverviewViewModel
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
        onPaymentHistoryClicked = { paymentOverview ->
          with(navigator) { backStackEntry.navigate(PaymentsDestinations2.History(paymentOverview)) }
        },
        onChangeBankAccount = navigateToConnectPayment,
        onDiscountClicked = { discounts ->
          with(navigator) { backStackEntry.navigate(PaymentsDestinations2.Discounts(discounts)) }
        },
        onUpcomingPaymentClicked = { memberCharge: MemberCharge, paymentOverview: PaymentOverview ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations2.Details(
                selectedMemberCharge = memberCharge,
                paymentOverview = paymentOverview,
              ),
            )
          }
        },
      )
    }
    composable<PaymentsDestinations2.Details> { backStackEntry ->
      PaymentDetailsDestination(
        memberCharge = selectedMemberCharge,
        paymentOverview = paymentOverview,
        onFailedChargeClick = { memberCharge: MemberCharge, paymentOverview: PaymentOverview ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations2.Details(
                selectedMemberCharge = memberCharge,
                paymentOverview = paymentOverview,
              ),
            )
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    composable<PaymentsDestinations2.History> { backStackEntry ->
      PaymentHistoryDestination(
        paymentOverview = paymentOverview.copy(
          paymentConnection = null, // Payment connection is not valid for historic payments
        ),
        onChargeClicked = { memberCharge: MemberCharge, paymentOverview: PaymentOverview ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations2.Details(
                selectedMemberCharge = memberCharge,
                paymentOverview = paymentOverview,
              ),
            )
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    composable<PaymentsDestinations2.Discounts> {
      val viewModel: PaymentOverviewViewModel = koinViewModel()

      DiscountsDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
  }
}
