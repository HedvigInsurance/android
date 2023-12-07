package com.hedvig.android.feature.payments2.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.feature.payments2.PaymentOverviewDestination
import com.hedvig.android.feature.payments2.PaymentOverviewViewModel
import com.hedvig.android.feature.payments2.data.MemberCharge
import com.hedvig.android.feature.payments2.data.PaymentOverview
import com.hedvig.android.feature.payments2.details.PaymentDetailsDestination
import com.hedvig.android.feature.payments2.history.PaymentHistoryDestination
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
          navigator.navigateUnsafe(PaymentsDestinations2.History(paymentOverview))
        },
        onChangeBankAccount = navigateToConnectPayment,
        onDiscountClicked = { }, // TODO
        onUpcomingPaymentClicked = { memberCharge: MemberCharge, paymentOverview: PaymentOverview ->
          navigator.navigateUnsafe(
            PaymentsDestinations2.Details(
              selectedMemberCharge = memberCharge,
              paymentOverview = paymentOverview,
            ),
          )
        },
      )
    }
    composable<PaymentsDestinations2.Details> {
      PaymentDetailsDestination(
        memberCharge = selectedMemberCharge,
        paymentOverview = paymentOverview,
        onFailedChargeClick = { memberCharge: MemberCharge, paymentOverview: PaymentOverview ->
          navigator.navigateUnsafe(
            PaymentsDestinations2.Details(
              selectedMemberCharge = memberCharge,
              paymentOverview = paymentOverview,
            ),
          )
        },
        navigateUp = navigator::navigateUp,
      )
    }
    composable<PaymentsDestinations2.History> {
      PaymentHistoryDestination(
        paymentOverview = paymentOverview.copy(paymentConnection = null), // Payment connection is not valid for historic payments
        onChargeClicked = { memberCharge: MemberCharge, paymentOverview: PaymentOverview ->
          navigator.navigateUnsafe(
            PaymentsDestinations2.Details(
              selectedMemberCharge = memberCharge,
              paymentOverview = paymentOverview,
            ),
          )
        },
        navigateUp = navigator::navigateUp,
      )
    }
  }
}
