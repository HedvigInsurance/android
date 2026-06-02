package com.hedvig.android.feature.payments.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsDestination
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.discounts.DiscountsDestination
import com.hedvig.android.feature.payments.ui.discounts.DiscountsViewModel
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryDestination
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryViewModel
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeDestination
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeSuccessDestination
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeViewModel
import com.hedvig.android.feature.payments.ui.memberpaymentdetails.MemberPaymentDetailsDestination
import com.hedvig.android.feature.payments.ui.memberpaymentdetails.MemberPaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.payments.PaymentsDestination
import com.hedvig.android.feature.payments.ui.payments.PaymentsViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.entryTransitionMetadata
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.paymentsGraph(
  navigator: Navigator,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
  navigateToPayoutAccount: () -> Unit,
  openConversation: () -> Unit,
) {
  navgraph(
    startDestination = PaymentsDestination.Payments::class,
  ) {
    navdestination<PaymentsDestination.Payments>(
      metadata = entryTransitionMetadata(MotionDefaults.fadeThroughEnter, MotionDefaults.fadeThroughExit),
    ) {
      val viewModel: PaymentsViewModel = metroViewModel()
      PaymentsDestination(
        viewModel = viewModel,
        onPaymentHistoryClicked = dropUnlessResumed {
          navigator.navigate(PaymentsDestinations.History)
        },
        onPayoutAccountClicked = dropUnlessResumed { navigateToPayoutAccount() },
        onChangeBankAccount = dropUnlessResumed { navigateToConnectPayment() },
        onDiscountClicked = dropUnlessResumed {
          navigator.navigate(PaymentsDestinations.Discounts)
        },
        onPaymentClicked = dropUnlessResumed { id: String? ->
          navigator.navigate(PaymentsDestinations.Details(id))
        },
        onMemberPaymentDetailsClicked = dropUnlessResumed {
          navigator.navigate(PaymentsDestinations.MemberPaymentDetails)
        },
        onOpenManualCharge = {
          navigator.navigate(PaymentsDestinations.ManualCharge)
        },
      )
    }

    navdestination<PaymentsDestinations.ManualCharge> {
      val viewModel: ManualChargeViewModel = metroViewModel()
      ManualChargeDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onNavigateToPaymentDetails = dropUnlessResumed { chargeId: String ->
          navigator.navigate(
            PaymentsDestinations.Details(
              chargeId,
            ),
          )
        },
        onNavigateToSuccess = { showCancellationWarning ->
          navigator.navigate<PaymentsDestinations.ManualCharge>(
            PaymentsDestinations.ManualChargeSuccess(
              showCancellationWarning = showCancellationWarning,
            ),
            inclusive = true,
          )
        },
        openConversation = openConversation,
      )
    }

    navdestination<PaymentsDestinations.ManualChargeSuccess> {
      ManualChargeSuccessDestination(
        this.showCancellationWarning,
        navigator::navigateUp,
      )
    }

    navdestination<PaymentsDestinations.Details> {
      val memberChargeId = this.memberChargeId
      val viewModel: PaymentDetailsViewModel =
        assistedMetroViewModel<PaymentDetailsViewModel, PaymentDetailsViewModel.Factory> { create(memberChargeId) }
      PaymentDetailsDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<PaymentsDestinations.History> {
      val viewModel: PaymentHistoryViewModel = metroViewModel()
      PaymentHistoryDestination(
        viewModel = viewModel,
        onChargeClicked = dropUnlessResumed { memberChargeId: String ->
          navigator.navigate(
            PaymentsDestinations.Details(
              memberChargeId,
            ),
          )
        },
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<PaymentsDestinations.Forever> {
      val viewModel: ForeverViewModel = metroViewModel()
      ForeverDestination(
        viewModel = viewModel,
        languageService = languageService,
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }

    navdestination<PaymentsDestinations.Discounts> {
      val viewModel: DiscountsViewModel = metroViewModel()
      DiscountsDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        navigateToForever = dropUnlessResumed {
          navigator.navigate(
            PaymentsDestinations.Forever,
          )
        },
      )
    }

    navdestination<PaymentsDestinations.MemberPaymentDetails> {
      val viewModel: MemberPaymentDetailsViewModel = metroViewModel()
      MemberPaymentDetailsDestination(
        viewModel,
        onChangeBankAccount = navigateToConnectPayment,
        navigateUp = navigator::navigateUp,
      )
    }
  }
}
