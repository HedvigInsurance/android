package com.hedvig.android.feature.payments.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.feature.payments.details.PaymentDetailsDestination
import com.hedvig.android.feature.payments.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.discounts.DiscountsDestination
import com.hedvig.android.feature.payments.discounts.DiscountsViewModel
import com.hedvig.android.feature.payments.history.PaymentHistoryDestination
import com.hedvig.android.feature.payments.history.PaymentHistoryViewModel
import com.hedvig.android.feature.payments.payments.PaymentsDestination
import com.hedvig.android.feature.payments.payments.PaymentsViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.paymentsGraph(
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
) {
  navigation<PaymentsDestination.Graph>(
    startDestination = createRoutePattern<PaymentsDestination.Payments>(),
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
        onPaymentHistoryClicked = {
          with(navigator) { backStackEntry.navigate(PaymentsDestinations.History) }
        },
        onChangeBankAccount = navigateToConnectPayment,
        onDiscountClicked = {
          with(navigator) { backStackEntry.navigate(PaymentsDestinations.Discounts) }
        },
        onUpcomingPaymentClicked = { memberChargeId: String ->
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

    composable<PaymentsDestinations.Details> { backStackEntry ->
      val viewModel: PaymentDetailsViewModel = koinViewModel(parameters = { parametersOf(this.memberChargeId) })
      PaymentDetailsDestination(
        viewModel = viewModel,
        onFailedChargeClick = { memberChargeId: String ->
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

    composable<PaymentsDestinations.History> { backStackEntry ->
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

    composable<PaymentsDestinations.Forever>(
    ) {
      val viewModel: ForeverViewModel = koinViewModel()
      ForeverDestination(
        viewModel = viewModel,
        languageService = languageService,
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }

    composable<PaymentsDestinations.Discounts> { backStackEntry ->
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
