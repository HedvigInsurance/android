package com.hedvig.app.feature.swedishbankid.sign

import app.cash.turbine.test
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.usecase.FakeObserveQuoteCartCheckoutUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SwedishBankIdSignViewModelTest {

  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  @Test
  fun `a successful bankId sign and a successful access token call result in success`() = runTest {
    val fakeObserveQuoteCartCheckoutUseCase = FakeObserveQuoteCartCheckoutUseCase()
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      fakeObserveQuoteCartCheckoutUseCase,
      FakeFeatureManager2(),
    )

    viewModel.viewState.test {
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.StartBankId)

      viewModel.bankIdStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.SignInProgress)

      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
      assertThat(viewModel.viewState.value).isEqualTo(BankIdSignViewState.SignInProgress)
      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
      assertThat(viewModel.viewState.value).isEqualTo(BankIdSignViewState.SignInProgress)
      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.BankIdSuccess)

      assertThat(awaitItem()).isInstanceOf(BankIdSignViewState.StartDirectDebit::class)

      viewModel.directDebitStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.Success)
    }
  }

  @Test
  fun `a delayed successful bankId sign and a successful access token call result in success`() = runTest {
    val fakeObserveQuoteCartCheckoutUseCase = FakeObserveQuoteCartCheckoutUseCase()
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      fakeObserveQuoteCartCheckoutUseCase,
      FakeFeatureManager2(),
    )

    viewModel.viewState.test {
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.StartBankId)

      viewModel.bankIdStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.SignInProgress)

      repeat(10) {
        fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
        assertThat(viewModel.viewState.value).isEqualTo(BankIdSignViewState.SignInProgress)
      }

      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.BankIdSuccess)

      assertThat(awaitItem()).isInstanceOf(BankIdSignViewState.StartDirectDebit::class)

      viewModel.directDebitStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.Success)
    }
  }

  @Test
  fun `not opening the bankID app on this device still allows the singing flow to continue`() = runTest {
    val fakeObserveQuoteCartCheckoutUseCase = FakeObserveQuoteCartCheckoutUseCase()
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      fakeObserveQuoteCartCheckoutUseCase,
      FakeFeatureManager2(),
    )

    viewModel.viewState.test {
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.StartBankId)

      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
      assertThat(viewModel.viewState.value).isEqualTo(BankIdSignViewState.StartBankId)

      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.BankIdSuccess)

      assertThat(awaitItem()).isInstanceOf(BankIdSignViewState.StartDirectDebit::class)

      viewModel.directDebitStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.Success)
    }
  }

  @Test
  fun `a failed bankId sign fails immediately`() = runTest {
    val fakeObserveQuoteCartCheckoutUseCase = FakeObserveQuoteCartCheckoutUseCase()
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      fakeObserveQuoteCartCheckoutUseCase,
      FakeFeatureManager(),
    )

    viewModel.viewState.test {
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.StartBankId)

      viewModel.bankIdStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.SignInProgress)

      fakeObserveQuoteCartCheckoutUseCase.results.add(
        Checkout(Checkout.CheckoutStatus.FAILED, "Some Status Text", null).right(),
      )
      assertThat(awaitItem())
        .isInstanceOf(BankIdSignViewState.Error::class)
        .prop(BankIdSignViewState.Error::message)
        .isEqualTo("Some Status Text")
    }
  }
}
