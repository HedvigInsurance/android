package com.hedvig.android.feature.payments.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.details.PaymentDetailsDestination
import com.hedvig.android.feature.payments.discounts.DiscountsDestination
import com.hedvig.android.feature.payments.discounts.DiscountsViewModel
import com.hedvig.android.feature.payments.history.PaymentHistoryDestination
import com.hedvig.android.feature.payments.payments.PaymentsDestination
import com.hedvig.android.feature.payments.payments.PaymentsViewModel
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.paymentsGraph(
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navigateToConnectPayment: () -> Unit,
) {
  navigation<PaymentsDestination.Graph>(
    startDestination = PaymentsDestination.Payments::class,
  ) {
    composable<PaymentsDestination.Payments>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.payments },
      ),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: PaymentsViewModel = koinViewModel()
      PaymentsDestination(
        viewModel = viewModel,
        onPaymentHistoryClicked = { paymentOverview ->
          with(navigator) { backStackEntry.navigate(PaymentsDestinations.History(paymentOverview)) }
        },
        onChangeBankAccount = navigateToConnectPayment,
        onDiscountClicked = { discounts ->
          with(navigator) { backStackEntry.navigate(PaymentsDestinations.Discounts(discounts)) }
        },
        onUpcomingPaymentClicked = { memberCharge: MemberCharge, paymentOverview: PaymentOverview ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations.Details(
                selectedMemberCharge = memberCharge,
                paymentOverview = paymentOverview,
              ),
            )
          }
        },
      )
    }
    composable<PaymentsDestinations.Details> { backStackEntry, destination ->
      PaymentDetailsDestination(
        memberCharge = destination.selectedMemberCharge,
        paymentOverview = destination.paymentOverview,
        onFailedChargeClick = { memberCharge: MemberCharge ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations.Details(
                selectedMemberCharge = memberCharge,
                paymentOverview = destination.paymentOverview,
              ),
            )
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    composable<PaymentsDestinations.History> { backStackEntry, destination ->
      PaymentHistoryDestination(
        pastCharges = destination.paymentOverview.pastCharges,
        onChargeClicked = { memberCharge: MemberCharge ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations.Details(
                selectedMemberCharge = memberCharge,
                paymentOverview = destination.paymentOverview,
              ),
            )
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }
    composable<PaymentsDestinations.Discounts> { _ ->
      val viewModel: DiscountsViewModel = koinViewModel()
      DiscountsDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
  }
}
