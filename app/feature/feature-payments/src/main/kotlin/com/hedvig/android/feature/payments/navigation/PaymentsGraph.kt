package com.hedvig.android.feature.payments.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
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
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.paymentsGraph(
  backstack: Backstack,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
  navigateToPayoutAccount: () -> Unit,
  openConversation: () -> Unit,
) {
  entry<PaymentsKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: PaymentsViewModel = metroViewModel()
    PaymentsDestination(
      viewModel = viewModel,
      onPaymentHistoryClicked = dropUnlessResumed {
        backstack.add(PaymentHistoryKey)
      },
      onPayoutAccountClicked = dropUnlessResumed { navigateToPayoutAccount() },
      onChangeBankAccount = dropUnlessResumed { navigateToConnectPayment() },
      onDiscountClicked = dropUnlessResumed {
        backstack.add(DiscountsKey)
      },
      onPaymentClicked = dropUnlessResumed { id: String? ->
        backstack.add(PaymentDetailsKey(id))
      },
      onMemberPaymentDetailsClicked = dropUnlessResumed {
        backstack.add(MemberPaymentDetailsKey)
      },
      onOpenManualCharge = {
        backstack.add(ManualChargeKey)
      },
    )
  }

  entry<ManualChargeKey> {
    val viewModel: ManualChargeViewModel = metroViewModel()
    ManualChargeDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onNavigateToPaymentDetails = dropUnlessResumed { chargeId: String ->
        backstack.add(PaymentDetailsKey(chargeId))
      },
      onNavigateToSuccess = { showCancellationWarning ->
        backstack.navigateAndPopUpTo<ManualChargeKey>(
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
      backstack::navigateUp,
    )
  }

  entry<PaymentDetailsKey> { key ->
    val memberChargeId = key.memberChargeId
    val viewModel: PaymentDetailsViewModel =
      assistedMetroViewModel<PaymentDetailsViewModel, PaymentDetailsViewModel.Factory> { create(memberChargeId) }
    PaymentDetailsDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }

  entry<PaymentHistoryKey> {
    val viewModel: PaymentHistoryViewModel = metroViewModel()
    PaymentHistoryDestination(
      viewModel = viewModel,
      onChargeClicked = dropUnlessResumed { memberChargeId: String ->
        backstack.add(PaymentDetailsKey(memberChargeId))
      },
      navigateUp = backstack::navigateUp,
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
      navigateUp = backstack::navigateUp,
      navigateToForever = dropUnlessResumed {
        backstack.add(ForeverKey)
      },
    )
  }

  entry<MemberPaymentDetailsKey> {
    val viewModel: MemberPaymentDetailsViewModel = metroViewModel()
    MemberPaymentDetailsDestination(
      viewModel,
      onChangeBankAccount = navigateToConnectPayment,
      navigateUp = backstack::navigateUp,
    )
  }
}
