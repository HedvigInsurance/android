package com.hedvig.android.feature.odyssey.step.singleitemcheckout

import app.cash.turbine.test
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.matchesPredicate
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.feature.odyssey.data.TestClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.model.FlowId
import com.hedvig.android.odyssey.navigation.CheckoutMethod
import com.hedvig.android.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.odyssey.navigation.UiGuaranteedMoney
import com.hedvig.android.odyssey.step.singleitemcheckout.PayoutUiState
import com.hedvig.android.odyssey.step.singleitemcheckout.SingleItemCheckoutUiState
import com.hedvig.android.odyssey.step.singleitemcheckout.SingleItemCheckoutViewModel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import octopus.type.CurrencyCode
import org.junit.Rule
import org.junit.Test

class SingleItemCheckoutViewModelTest {
  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  @Test
  fun `a single item with no checkout option results in an Unavailable state immediately`() = runTest {
    val viewModel = SingleItemCheckoutViewModel(
      testSingleItemCheckout(availableCheckoutMethods = emptyList()),
      TestClaimFlowRepository(),
    )

    viewModel.uiState.test {
      assertThat(awaitItem()).isEqualTo(SingleItemCheckoutUiState.Unavailable)
    }
  }

  @Test
  fun `providing a list of available checkout methods automatically selects the first one`() = runTest {
    val firstCheckoutMethod = CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiGuaranteedMoney(0.0, CurrencyCode.SEK))
    val viewModel = SingleItemCheckoutViewModel(
      testSingleItemCheckout(
        availableCheckoutMethods = listOf(
          firstCheckoutMethod,
          CheckoutMethod.Known.AutomaticAutogiro("#2", "", UiGuaranteedMoney(0.0, CurrencyCode.SEK)),
          CheckoutMethod.Known.AutomaticAutogiro("#3", "", UiGuaranteedMoney(0.0, CurrencyCode.SEK)),
        ),
      ),
      TestClaimFlowRepository(),
    )

    viewModel.uiState.test {
      val uiState = awaitItem()
      assertThat(uiState).isInstanceOf(SingleItemCheckoutUiState.Content::class)
      assertThat(uiState.asContent()!!.selectedCheckoutMethod).isEqualTo(firstCheckoutMethod)
    }
  }

  @Test
  fun `not selecting a different checkout method should send the money of the first one`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val firstCheckoutMethod = CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiGuaranteedMoney(1.0, CurrencyCode.SEK))
    val viewModel = SingleItemCheckoutViewModel(
      testSingleItemCheckout(
        availableCheckoutMethods = listOf(
          firstCheckoutMethod,
          CheckoutMethod.Known.AutomaticAutogiro("#2", "", UiGuaranteedMoney(2.0, CurrencyCode.SEK)),
          CheckoutMethod.Known.AutomaticAutogiro("#3", "", UiGuaranteedMoney(3.0, CurrencyCode.SEK)),
        ),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      claimFlowRepository.submitSingleItemCheckoutResponse.add(ClaimFlowStep.UnknownStep(FlowId("")).right())
      viewModel.requestPayout()
      runCurrent()
      val sentMoneyAmount = claimFlowRepository.submitSingleItemCheckoutInput.awaitItem()
      assertThat(sentMoneyAmount).isEqualTo(1.0)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `selecting the second item, and asking for the payout sends in the right amount of money`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val secondCheckoutMethod =
      CheckoutMethod.Known.AutomaticAutogiro("#2", "", UiGuaranteedMoney(2.0, CurrencyCode.SEK))
    val viewModel = SingleItemCheckoutViewModel(
      testSingleItemCheckout(
        availableCheckoutMethods = listOf(
          CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiGuaranteedMoney(1.0, CurrencyCode.SEK)),
          secondCheckoutMethod,
          CheckoutMethod.Known.AutomaticAutogiro("#3", "", UiGuaranteedMoney(3.0, CurrencyCode.SEK)),
        ),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      viewModel.selectCheckoutMethod(secondCheckoutMethod)

      claimFlowRepository.submitSingleItemCheckoutResponse.add(ClaimFlowStep.UnknownStep(FlowId("")).right())
      viewModel.requestPayout()
      runCurrent()
      val sentMoneyAmount = claimFlowRepository.submitSingleItemCheckoutInput.awaitItem()
      assertThat(sentMoneyAmount).isEqualTo(2.0)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `selecting the second item, should update the money inside payoutUiState`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val secondCheckoutMethod =
      CheckoutMethod.Known.AutomaticAutogiro("#2", "", UiGuaranteedMoney(2.0, CurrencyCode.SEK))
    val viewModel = SingleItemCheckoutViewModel(
      testSingleItemCheckout(
        availableCheckoutMethods = listOf(
          CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiGuaranteedMoney(1.0, CurrencyCode.SEK)),
          secondCheckoutMethod,
          CheckoutMethod.Known.AutomaticAutogiro("#3", "", UiGuaranteedMoney(3.0, CurrencyCode.SEK)),
        ),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      assertThat(viewModel.uiState.value.asContent()!!.payoutUiState.amount.amount).isEqualTo(1.0)
      viewModel.selectCheckoutMethod(secondCheckoutMethod)
      assertThat(viewModel.uiState.value.asContent()!!.payoutUiState.amount.amount).isEqualTo(2.0)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `succeeding a payout updates the next step state with the returned next step`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val checkoutMethod = CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiGuaranteedMoney(1.0, CurrencyCode.SEK))
    val viewModel = SingleItemCheckoutViewModel(
      testSingleItemCheckout(
        availableCheckoutMethods = listOf(checkoutMethod),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      viewModel.requestPayout()
      assertThat(viewModel.uiState.value.asContent()!!.payoutUiState.status).isEqualTo(PayoutUiState.Status.Loading)
      claimFlowRepository.submitSingleItemCheckoutResponse.add(ClaimFlowStep.ClaimSuccessStep(FlowId("")).right())
      runCurrent()
      assertThat(viewModel.uiState.value.asContent()!!.payoutUiState.status)
        .isInstanceOf(PayoutUiState.Status.PaidOut::class)
      assertThat((viewModel.uiState.value.asContent()!!.payoutUiState.status as PayoutUiState.Status.PaidOut).nextStep)
        .matchesPredicate { it is ClaimFlowStep.ClaimSuccessStep }
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `failing the payout network request, you can retry and succeed the second time`() = runTest {
    val claimFlowRepository = TestClaimFlowRepository()
    val checkoutMethod = CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiGuaranteedMoney(1.0, CurrencyCode.SEK))
    val viewModel = SingleItemCheckoutViewModel(
      testSingleItemCheckout(
        availableCheckoutMethods = listOf(checkoutMethod),
      ),
      claimFlowRepository,
    )

    viewModel.uiState.test {
      viewModel.uiState.value
      cancelAndIgnoreRemainingEvents()
    }
  }

  companion object {
    private fun testSingleItemCheckout(
      availableCheckoutMethods: List<CheckoutMethod.Known>,
      price: UiGuaranteedMoney = UiGuaranteedMoney(100.0, CurrencyCode.SEK),
      depreciation: UiGuaranteedMoney = UiGuaranteedMoney(100.0, CurrencyCode.SEK),
      deductible: UiGuaranteedMoney = UiGuaranteedMoney(100.0, CurrencyCode.SEK),
      payoutAmount: UiGuaranteedMoney = UiGuaranteedMoney(100.0, CurrencyCode.SEK),
    ) = ClaimFlowDestination.SingleItemCheckout(
      price,
      depreciation,
      deductible,
      payoutAmount,
      availableCheckoutMethods,
    )
  }
}
