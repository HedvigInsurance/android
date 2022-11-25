package com.hedvig.app.feature.swedishbankid.sign

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.android.auth.test.FakeLoginStatusService
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCase
import com.hedvig.app.feature.offer.usecase.FakeCreateAccessTokenUseCase
import com.hedvig.app.feature.offer.usecase.FakeObserveQuoteCartCheckoutUseCase
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.coroutines.MainCoroutineRule
import com.hedvig.hanalytics.PaymentType
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SwedishBankIdSignViewModelTest {

  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  @Test
  fun `a successful bankId sign and a successful access token call result in success`() = runTest {
    val loginStatusService = FakeLoginStatusService()
    val fakeObserveQuoteCartCheckoutUseCase = FakeObserveQuoteCartCheckoutUseCase()
    val fakeCreateAccessTokenUseCase = FakeCreateAccessTokenUseCase()
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      fakeObserveQuoteCartCheckoutUseCase,
      fakeCreateAccessTokenUseCase,
      FakeFeatureManager(paymentType = { enumValues<PaymentType>().random() }),
    )

    viewModel.viewState.test {
      assertThat(loginStatusService.isLoggedIn).isEqualTo(false)
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.StartBankId)

      viewModel.bankIdStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.SignInProgress)

      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
      assertThat(viewModel.viewState.value).isEqualTo(BankIdSignViewState.SignInProgress)
      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
      assertThat(viewModel.viewState.value).isEqualTo(BankIdSignViewState.SignInProgress)
      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.BankIdSuccess)

      fakeCreateAccessTokenUseCase.results.add(CreateAccessTokenUseCase.Success.right())
      assertThat(awaitItem()).isInstanceOf(BankIdSignViewState.StartDirectDebit::class)

      viewModel.directDebitStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.Success)

      assertThat(loginStatusService.isLoggedIn).isEqualTo(true)
    }
  }

  @Test
  fun `a delayed successful bankId sign and a successful access token call result in success`() = runTest {
    val loginStatusService = FakeLoginStatusService()
    val fakeObserveQuoteCartCheckoutUseCase = FakeObserveQuoteCartCheckoutUseCase()
    val fakeCreateAccessTokenUseCase = FakeCreateAccessTokenUseCase()
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      fakeObserveQuoteCartCheckoutUseCase,
      fakeCreateAccessTokenUseCase,
      FakeFeatureManager(paymentType = { enumValues<PaymentType>().random() }),
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

      fakeCreateAccessTokenUseCase.results.add(CreateAccessTokenUseCase.Success.right())
      assertThat(awaitItem()).isInstanceOf(BankIdSignViewState.StartDirectDebit::class)

      viewModel.directDebitStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.Success)

      assertThat(loginStatusService.isLoggedIn).isEqualTo(true)
    }
  }

  @Test
  fun `not opening the bankID app on this device still allows the singing flow to continue`() = runTest {
    val loginStatusService = FakeLoginStatusService()
    val fakeObserveQuoteCartCheckoutUseCase = FakeObserveQuoteCartCheckoutUseCase()
    val fakeCreateAccessTokenUseCase = FakeCreateAccessTokenUseCase()
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      fakeObserveQuoteCartCheckoutUseCase,
      fakeCreateAccessTokenUseCase,
      FakeFeatureManager(paymentType = { enumValues<PaymentType>().random() }),
    )

    viewModel.viewState.test {
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.StartBankId)

      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
      assertThat(viewModel.viewState.value).isEqualTo(BankIdSignViewState.StartBankId)

      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.BankIdSuccess)

      fakeCreateAccessTokenUseCase.results.add(CreateAccessTokenUseCase.Success.right())
      assertThat(awaitItem()).isInstanceOf(BankIdSignViewState.StartDirectDebit::class)

      viewModel.directDebitStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.Success)

      assertThat(loginStatusService.isLoggedIn).isEqualTo(true)
    }
  }

  @Test
  fun `a successful bankId sign and a failed access token call result in failure`() = runTest {
    val loginStatusService = FakeLoginStatusService()
    val fakeObserveQuoteCartCheckoutUseCase = FakeObserveQuoteCartCheckoutUseCase()
    val fakeCreateAccessTokenUseCase = FakeCreateAccessTokenUseCase()
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      fakeObserveQuoteCartCheckoutUseCase,
      fakeCreateAccessTokenUseCase,
      FakeFeatureManager(),
    )

    viewModel.viewState.test {
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.StartBankId)

      viewModel.bankIdStarted()
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.SignInProgress)

      fakeObserveQuoteCartCheckoutUseCase.results.add(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
      assertThat(awaitItem()).isEqualTo(BankIdSignViewState.BankIdSuccess)

      fakeCreateAccessTokenUseCase.results.add(ErrorMessage().left())
      assertThat(awaitItem()).isInstanceOf(BankIdSignViewState.Error::class)

      assertThat(loginStatusService.isLoggedIn).isEqualTo(false)
    }
  }

  @Test
  fun `a failed bankId sign fails immediately`() = runTest {
    val loginStatusService = FakeLoginStatusService()
    val fakeObserveQuoteCartCheckoutUseCase = FakeObserveQuoteCartCheckoutUseCase()
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      fakeObserveQuoteCartCheckoutUseCase,
      FakeCreateAccessTokenUseCase().apply { results.close() },
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

      assertThat(loginStatusService.isLoggedIn).isEqualTo(false)
    }
  }
}
