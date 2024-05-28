package com.hedvig.android.feature.odyssey.step.singleitemcheckout

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.claimflow.CheckoutMethod
import com.hedvig.android.data.claimflow.ClaimFlowDestination
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
    )

    viewModel.uiState.test {
      assertThat(awaitItem()).isEqualTo(SingleItemCheckoutUiState.Unavailable)
    }
  }

  @Test
  fun `providing a list of available checkout methods automatically selects the first one`() = runTest {
    val firstCheckoutMethod = CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiMoney(0.0, CurrencyCode.SEK))
    val viewModel = SingleItemCheckoutViewModel(
      testSingleItemCheckout(
        availableCheckoutMethods = listOf(
          firstCheckoutMethod,
          CheckoutMethod.Known.AutomaticAutogiro("#2", "", UiMoney(0.0, CurrencyCode.SEK)),
          CheckoutMethod.Known.AutomaticAutogiro("#3", "", UiMoney(0.0, CurrencyCode.SEK)),
        ),
      ),
    )

    viewModel.uiState.test {
      val uiState = awaitItem()
      assertThat(uiState).isInstanceOf(SingleItemCheckoutUiState.Content::class)
      assertThat(uiState.asContent()!!.selectedCheckoutMethod).isEqualTo(firstCheckoutMethod)
    }
  }

  @Test
  fun `selecting the second item, updates the amount of money of the selected item`() = runTest {
    val secondCheckoutMethod =
      CheckoutMethod.Known.AutomaticAutogiro("#2", "", UiMoney(2.0, CurrencyCode.SEK))
    val viewModel = SingleItemCheckoutViewModel(
      testSingleItemCheckout(
        availableCheckoutMethods = listOf(
          CheckoutMethod.Known.AutomaticAutogiro("#1", "", UiMoney(1.0, CurrencyCode.SEK)),
          secondCheckoutMethod,
          CheckoutMethod.Known.AutomaticAutogiro("#3", "", UiMoney(3.0, CurrencyCode.SEK)),
        ),
      ),
    )

    assertThat(viewModel.uiState.value.asContent()!!.selectedCheckoutMethod.uiMoney.amount).isEqualTo(1.0)
    viewModel.selectCheckoutMethod(secondCheckoutMethod)
    assertThat(viewModel.uiState.value.asContent()!!.selectedCheckoutMethod.uiMoney.amount).isEqualTo(2.0)
  }

  companion object {
    private fun testSingleItemCheckout(
      availableCheckoutMethods: List<CheckoutMethod.Known>,
      price: UiMoney = UiMoney(100.0, CurrencyCode.SEK),
      depreciation: UiMoney = UiMoney(100.0, CurrencyCode.SEK),
      deductible: UiMoney = UiMoney(100.0, CurrencyCode.SEK),
      payoutAmount: UiMoney = UiMoney(100.0, CurrencyCode.SEK),
      modelName: String? = "IPhone 12",
      brandName: String? = null,
      customName: String? = null,
    ) = ClaimFlowDestination.SingleItemCheckout(
      compensation = ClaimFlowDestination.Compensation.Known.ValueCompensation(
        price,
        depreciation,
        deductible,
        payoutAmount,
      ),
      availableCheckoutMethods,
      modelName,
      brandName,
      customName,
    )
  }
}
