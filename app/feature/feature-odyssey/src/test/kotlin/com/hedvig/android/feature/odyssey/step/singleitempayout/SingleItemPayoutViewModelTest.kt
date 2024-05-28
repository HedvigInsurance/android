package com.hedvig.android.feature.odyssey.step.singleitempayout

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.claimflow.CheckoutMethod
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.feature.odyssey.data.TestClaimFlowRepository
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import octopus.type.CurrencyCode
import org.junit.Rule
import org.junit.Test

class SingleItemPayoutViewModelTest {
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `succeeding a payout updates the status to be considered PaidOut`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val checkoutMethod = CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiMoney(1.0, CurrencyCode.SEK))
    val viewModel = SingleItemPayoutViewModel(
      ClaimFlowDestination.SingleItemPayout(checkoutMethod),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      assertThat(viewModel.uiState.value.status).isEqualTo(PayoutUiState.Status.Loading)
      claimFlowRepository.submitSingleItemCheckoutResponse.add(Unit.right())
      runCurrent()
      assertThat(viewModel.uiState.value.status).isEqualTo(PayoutUiState.Status.PaidOut)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `failing the payout network request, you can retry and succeed the second time`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val checkoutMethod = CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiMoney(1.0, CurrencyCode.SEK))
    val viewModel = SingleItemPayoutViewModel(
      ClaimFlowDestination.SingleItemPayout(checkoutMethod),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      assertThat(viewModel.uiState.value.status).isEqualTo(PayoutUiState.Status.Loading)
      claimFlowRepository.submitSingleItemCheckoutResponse.add(ErrorMessage("Error", null).left())
      runCurrent()
      assertThat(viewModel.uiState.value.status).isEqualTo(PayoutUiState.Status.Error)

      viewModel.requestPayout()
      runCurrent()
      assertThat(viewModel.uiState.value.status).isEqualTo(PayoutUiState.Status.Loading)

      claimFlowRepository.submitSingleItemCheckoutResponse.add(Unit.right())
      runCurrent()
      assertThat(viewModel.uiState.value.status).isEqualTo(PayoutUiState.Status.PaidOut)
      cancelAndIgnoreRemainingEvents()
    }
  }
}
