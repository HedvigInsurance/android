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
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import com.hedvig.android.navigation.compose.entryTransitionMetadata
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.navigateUp
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.paymentsGraph(
  backStack: MutableList<HedvigNavKey>,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
  navigateToPayoutAccount: () -> Unit,
  openConversation: () -> Unit,
) {
  entry<PaymentsKey>(
    metadata = entryTransitionMetadata(MotionDefaults.fadeThroughEnter, MotionDefaults.fadeThroughExit) +
      NavSuiteSceneDecoratorStrategy.showNavBar(),
  ) {
    val viewModel: PaymentsViewModel = metroViewModel()
    PaymentsDestination(
      viewModel = viewModel,
      onPaymentHistoryClicked = dropUnlessResumed {
        backStack.add(PaymentHistoryKey)
      },
      onPayoutAccountClicked = dropUnlessResumed { navigateToPayoutAccount() },
      onChangeBankAccount = dropUnlessResumed { navigateToConnectPayment() },
      onDiscountClicked = dropUnlessResumed {
        backStack.add(DiscountsKey)
      },
      onPaymentClicked = dropUnlessResumed { id: String? ->
        backStack.add(PaymentDetailsKey(id))
      },
      onMemberPaymentDetailsClicked = dropUnlessResumed {
        backStack.add(MemberPaymentDetailsKey)
      },
      onOpenManualCharge = {
        backStack.add(ManualChargeKey)
      },
    )
  }

  entry<ManualChargeKey> {
    val viewModel: ManualChargeViewModel = metroViewModel()
    ManualChargeDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      onNavigateToPaymentDetails = dropUnlessResumed { chargeId: String ->
        backStack.add(PaymentDetailsKey(chargeId))
      },
      onNavigateToSuccess = { showCancellationWarning ->
        backStack.navigateAndPopUpTo<ManualChargeKey>(
          ManualChargeSuccessKey(
            showCancellationWarning = showCancellationWarning,
          ),
          inclusive = true,
        )
      },
      openConversation = openConversation,
    )
  }

  entry<ManualChargeSuccessKey> { key ->
    ManualChargeSuccessDestination(
      key.showCancellationWarning,
      backStack::navigateUp,
    )
  }

  entry<PaymentDetailsKey> { key ->
    val memberChargeId = key.memberChargeId
    val viewModel: PaymentDetailsViewModel =
      assistedMetroViewModel<PaymentDetailsViewModel, PaymentDetailsViewModel.Factory> { create(memberChargeId) }
    PaymentDetailsDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
    )
  }

  entry<PaymentHistoryKey> {
    val viewModel: PaymentHistoryViewModel = metroViewModel()
    PaymentHistoryDestination(
      viewModel = viewModel,
      onChargeClicked = dropUnlessResumed { memberChargeId: String ->
        backStack.add(PaymentDetailsKey(memberChargeId))
      },
      navigateUp = backStack::navigateUp,
    )
  }

  entry<ForeverKey> {
    val viewModel: ForeverViewModel = metroViewModel()
    ForeverDestination(
      viewModel = viewModel,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
    )
  }

  entry<DiscountsKey> {
    val viewModel: DiscountsViewModel = metroViewModel()
    DiscountsDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      navigateToForever = dropUnlessResumed {
        backStack.add(ForeverKey)
      },
    )
  }

  entry<MemberPaymentDetailsKey> {
    val viewModel: MemberPaymentDetailsViewModel = metroViewModel()
    MemberPaymentDetailsDestination(
      viewModel,
      onChangeBankAccount = navigateToConnectPayment,
      navigateUp = backStack::navigateUp,
    )
  }
}
