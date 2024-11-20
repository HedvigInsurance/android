package com.hedvig.android.feature.payments.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.design.system.hedvig.MotionDefaults
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsDestination
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.discounts.DiscountsDestination
import com.hedvig.android.feature.payments.ui.discounts.DiscountsViewModel
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryDestination
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryViewModel
import com.hedvig.android.feature.payments.ui.payments.PaymentsDestination
import com.hedvig.android.feature.payments.ui.payments.PaymentsViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.paymentsGraph(
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
) {
  navgraph<PaymentsDestination.Graph>(
    startDestination = PaymentsDestination.Payments::class,
  ) {
    navdestination<PaymentsDestination.Payments>(
      deepLinks = listOf(
        navDeepLink { uriPattern = hedvigDeepLinkContainer.payments },
      ),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) { backStackEntry ->
      val viewModel: PaymentsViewModel = koinViewModel()
      PaymentsDestination(
        viewModel = viewModel,
        onPaymentHistoryClicked = {
          with(navigator) { backStackEntry.navigate(PaymentsDestinations.History) }
        },
        onChangeBankAccount = navigateToConnectPayment,
        onDiscountClicked = {
          with(navigator) { backStackEntry.navigate(PaymentsDestinations.Discounts) }
        },
        onPaymentClicked = { memberChargeId: String? ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations.Details(
                memberChargeId,
              ),
            )
          }
        },
      )
    }

    navdestination<PaymentsDestinations.Details> { backStackEntry ->
      val viewModel: PaymentDetailsViewModel = koinViewModel(parameters = { parametersOf(this.memberChargeId) })
      PaymentDetailsDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<PaymentsDestinations.History> { backStackEntry ->
      val viewModel: PaymentHistoryViewModel = koinViewModel()
      PaymentHistoryDestination(
        viewModel = viewModel,
        onChargeClicked = { memberChargeId: String ->
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations.Details(
                memberChargeId,
              ),
            )
          }
        },
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<PaymentsDestinations.Forever> {
      val viewModel: ForeverViewModel = koinViewModel()
      ForeverDestination(
        viewModel = viewModel,
        languageService = languageService,
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }

    navdestination<PaymentsDestinations.Discounts> { backStackEntry ->
      val viewModel: DiscountsViewModel = koinViewModel()
      DiscountsDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        navigateToForever = {
          with(navigator) {
            backStackEntry.navigate(
              PaymentsDestinations.Forever,
            )
          }
        },
      )
    }
  }
}
