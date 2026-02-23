package com.hedvig.android.feature.payments.navigation

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsDestination
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.discounts.DiscountsDestination
import com.hedvig.android.feature.payments.ui.discounts.DiscountsViewModel
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryDestination
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryViewModel
import com.hedvig.android.feature.payments.ui.memberpaymentdetails.MemberPaymentDetailsDestination
import com.hedvig.android.feature.payments.ui.memberpaymentdetails.MemberPaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.payments.PaymentsDestination
import com.hedvig.android.feature.payments.ui.payments.PaymentsViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import androidx.navigation.NavController
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.paymentsGraph(
  navController: NavController,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
) {
  navgraph<PaymentsDestination.Graph>(
    startDestination = PaymentsDestination.Payments::class,
  ) {
    navdestination<PaymentsDestination.Payments>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.payments),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) {
      val viewModel: PaymentsViewModel = koinViewModel()
      PaymentsDestination(
        viewModel = viewModel,
        onPaymentHistoryClicked = dropUnlessResumed {
          navController.navigate(PaymentsDestinations.History)
        },
        onChangeBankAccount = dropUnlessResumed { navigateToConnectPayment() },
        onDiscountClicked = dropUnlessResumed {
          navController.navigate(PaymentsDestinations.Discounts)
        },
        onPaymentClicked = dropUnlessResumed { id: String? ->
          navController.navigate(PaymentsDestinations.Details(id))
        },
        onMemberPaymentDetailsClicked = dropUnlessResumed {
          navController.navigate(PaymentsDestinations.MemberPaymentDetails)
        },
      )
    }

    navdestination<PaymentsDestinations.Details> {
      val viewModel: PaymentDetailsViewModel = koinViewModel(parameters = { parametersOf(this.memberChargeId) })
      PaymentDetailsDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
      )
    }

    navdestination<PaymentsDestinations.History> {
      val viewModel: PaymentHistoryViewModel = koinViewModel()
      PaymentHistoryDestination(
        viewModel = viewModel,
        onChargeClicked = dropUnlessResumed { memberChargeId: String ->
          navController.navigate(
            PaymentsDestinations.Details(
              memberChargeId,
            ),
          )
        },
        navigateUp = navController::navigateUp,
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

    navdestination<PaymentsDestinations.Discounts> {
      val viewModel: DiscountsViewModel = koinViewModel()
      DiscountsDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        navigateToForever = dropUnlessResumed {
          navController.navigate(
            PaymentsDestinations.Forever,
          )
        },
      )
    }

    navdestination<PaymentsDestinations.MemberPaymentDetails> {
      val viewModel: MemberPaymentDetailsViewModel = koinViewModel()
      MemberPaymentDetailsDestination(
        viewModel,
        onChangeBankAccount = navigateToConnectPayment,
        navigateUp = navController::navigateUp,
      )
    }
  }
}
