package com.hedvig.app.feature.swedishbankid.sign

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.hedvig.app.authenticate.FakeLoginStatusService
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCase
import com.hedvig.app.feature.offer.usecase.FakeCreateAccessTokenUseCase
import com.hedvig.app.feature.offer.usecase.FakeObserveQuoteCartCheckoutUseCase
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.coroutines.MainCoroutineRule
import com.hedvig.app.util.featureflags.FakeFeatureManager
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SwedishBankIdSignViewModelTest {

  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  @Test
  fun `a successful bankId sign and a successful access token call result in success`() = runTest {
    val loginStatusService = FakeLoginStatusService(isViewingOffer = true)
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      FakeObserveQuoteCartCheckoutUseCase {
        flow {
          delay(10)
          emit(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
          delay(10)
          emit(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
        }
      },
      FakeCreateAccessTokenUseCase {
        delay(10)
        CreateAccessTokenUseCase.Success.right()
      },
      FakeFeatureManager(),
    )

    var viewState: BankIdSignViewState = viewModel.viewState.value
    val collectingJob = launch {
      viewModel.viewState.collect {
        viewState = it
      }
    }
    assertThat(viewState).isEqualTo(BankIdSignViewState.StartBankId)

    viewModel.bankIdStarted()
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.SignInProgress)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.SignInProgress)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.BankIdSuccess)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isInstanceOf(BankIdSignViewState.StartDirectDebit::class)

    viewModel.directDebitStarted()
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.Success)

    assertThat(loginStatusService.isLoggedIn).isEqualTo(true)
    assertThat(loginStatusService.isViewingOffer).isEqualTo(false)

    collectingJob.cancelAndJoin()
  }

  @Test
  fun `a delayed successful bankId sign and a successful access token call result in success`() = runTest {
    val loginStatusService = FakeLoginStatusService(isViewingOffer = true)
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      FakeObserveQuoteCartCheckoutUseCase {
        flow {
          repeat(10) {
            delay(10)
            emit(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
          }
          delay(10)
          emit(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
        }
      },
      FakeCreateAccessTokenUseCase {
        delay(10)
        CreateAccessTokenUseCase.Success.right()
      },
      FakeFeatureManager(),
    )

    var viewState: BankIdSignViewState = viewModel.viewState.value
    val collectingJob = launch {
      viewModel.viewState.collect {
        viewState = it
      }
    }
    assertThat(viewState).isEqualTo(BankIdSignViewState.StartBankId)

    viewModel.bankIdStarted()
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.SignInProgress)

    repeat(10) {
      advanceTimeBy(10)
      runCurrent()
      assertThat(viewState).isEqualTo(BankIdSignViewState.SignInProgress)
    }

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.BankIdSuccess)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isInstanceOf(BankIdSignViewState.StartDirectDebit::class)

    viewModel.directDebitStarted()
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.Success)

    assertThat(loginStatusService.isLoggedIn).isEqualTo(true)
    assertThat(loginStatusService.isViewingOffer).isEqualTo(false)

    collectingJob.cancelAndJoin()
  }

  @Test
  fun `not opening the bankID app on this device still allows the singing flow to continue`() = runTest {
    val loginStatusService = FakeLoginStatusService(isViewingOffer = true)
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      FakeObserveQuoteCartCheckoutUseCase {
        flow {
          delay(10)
          emit(Checkout(Checkout.CheckoutStatus.PENDING, null, null).right())
          delay(10)
          emit(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
        }
      },
      FakeCreateAccessTokenUseCase {
        delay(10)
        CreateAccessTokenUseCase.Success.right()
      },
      FakeFeatureManager(),
    )

    var viewState: BankIdSignViewState = viewModel.viewState.value
    val collectingJob = launch {
      viewModel.viewState.collect {
        viewState = it
      }
    }
    assertThat(viewState).isEqualTo(BankIdSignViewState.StartBankId)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.StartBankId)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.BankIdSuccess)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isInstanceOf(BankIdSignViewState.StartDirectDebit::class)

    viewModel.directDebitStarted()
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.Success)

    assertThat(loginStatusService.isLoggedIn).isEqualTo(true)
    assertThat(loginStatusService.isViewingOffer).isEqualTo(false)

    collectingJob.cancelAndJoin()
  }

  @Test
  fun `a successful bankId sign and a failed access token call result in failure`() = runTest {
    val loginStatusService = FakeLoginStatusService(isViewingOffer = true)
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      FakeObserveQuoteCartCheckoutUseCase {
        flow {
          delay(10)
          emit(Checkout(Checkout.CheckoutStatus.SIGNED, null, null).right())
        }
      },
      FakeCreateAccessTokenUseCase {
        delay(10)
        ErrorMessage().left()
      },
      FakeFeatureManager(),
    )

    var viewState: BankIdSignViewState = viewModel.viewState.value
    val collectingJob = launch {
      viewModel.viewState.collect {
        viewState = it
      }
    }
    assertThat(viewState).isEqualTo(BankIdSignViewState.StartBankId)

    viewModel.bankIdStarted()
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.SignInProgress)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.BankIdSuccess)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState).isInstanceOf(BankIdSignViewState.Error::class)

    assertThat(loginStatusService.isLoggedIn).isEqualTo(false)
    assertThat(loginStatusService.isViewingOffer).isEqualTo(true)

    collectingJob.cancelAndJoin()
  }

  @Test
  fun `a failed bankId sign fails immediately`() = runTest {
    val loginStatusService = FakeLoginStatusService(isViewingOffer = true)
    val viewModel = SwedishBankIdSignViewModel(
      QuoteCartId(""),
      loginStatusService,
      FakeObserveQuoteCartCheckoutUseCase {
        flow {
          delay(10)
          emit(Checkout(Checkout.CheckoutStatus.FAILED, "Some Status Text", null).right())
        }
      },
      FakeCreateAccessTokenUseCase { error("I should never be called") },
      FakeFeatureManager(),
    )

    var viewState: BankIdSignViewState = viewModel.viewState.value
    val collectingJob = launch {
      viewModel.viewState.collect {
        viewState = it
      }
    }
    assertThat(viewState).isEqualTo(BankIdSignViewState.StartBankId)

    viewModel.bankIdStarted()
    runCurrent()
    assertThat(viewState).isEqualTo(BankIdSignViewState.SignInProgress)

    advanceTimeBy(10)
    runCurrent()
    assertThat(viewState)
      .isInstanceOf(BankIdSignViewState.Error::class)
      .prop(BankIdSignViewState.Error::message)
      .isEqualTo("Some Status Text")

    assertThat(loginStatusService.isLoggedIn).isEqualTo(false)
    assertThat(loginStatusService.isViewingOffer).isEqualTo(true)

    collectingJob.cancelAndJoin()
  }
}
