package com.hedvig.app.feature.swedishbankid.sign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCase
import com.hedvig.app.feature.offer.usecase.ObserveQuoteCartCheckoutUseCase
import com.hedvig.hanalytics.PaymentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update

class SwedishBankIdSignViewModel(
  quoteCartId: QuoteCartId,
  private val loginStatusService: LoginStatusService,
  private val observeQuoteCartCheckoutUseCase: ObserveQuoteCartCheckoutUseCase,
  private val createAccessTokenUseCase: CreateAccessTokenUseCase,
  private val featureManager: FeatureManager,
) : ViewModel() {

  private val _viewState: MutableStateFlow<BankIdSignViewState> = MutableStateFlow(BankIdSignViewState.StartBankId)

  val viewState: StateFlow<BankIdSignViewState> = _viewState.transformLatest { viewState ->
    emit(viewState)
    when {
      viewState.shouldQueryForSignStatus -> {
        observeQuoteCartCheckoutUseCase
          .invoke(quoteCartId)
          .collect { result ->
            val state = when (result) {
              is Either.Left -> BankIdSignViewState.Error()
              is Either.Right -> result.value.toViewState(viewState)
            }
            _viewState.value = state
          }
      }
      viewState is BankIdSignViewState.BankIdSuccess -> {
        when (createAccessTokenUseCase.invoke(quoteCartId)) {
          is Either.Left -> _viewState.value = BankIdSignViewState.Error()
          is Either.Right -> {
            loginStatusService.isViewingOffer = false
            loginStatusService.isLoggedIn = true
            featureManager.invalidateExperiments()
            _viewState.value = BankIdSignViewState.StartDirectDebit(featureManager.getPaymentType())
          }
        }
      }
    }
  }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(),
      _viewState.value,
    )

  fun bankIdStarted() {
    _viewState.update { signState ->
      when (signState) {
        BankIdSignViewState.StartBankId -> BankIdSignViewState.SignInProgress
        else -> signState
      }
    }
  }

  fun directDebitStarted() {
    _viewState.update { signState ->
      when (signState) {
        is BankIdSignViewState.StartDirectDebit -> BankIdSignViewState.Success
        else -> signState
      }
    }
  }
}

sealed interface BankIdSignViewState {
  object StartBankId : BankIdSignViewState
  object SignInProgress : BankIdSignViewState
  object Cancelled : BankIdSignViewState
  data class Error(val message: String? = null) : BankIdSignViewState
  object BankIdSuccess : BankIdSignViewState
  data class StartDirectDebit(val payinType: PaymentType) : BankIdSignViewState
  object Success : BankIdSignViewState

  val shouldQueryForSignStatus: Boolean
    get() = this is StartBankId || this is SignInProgress

  val isDialogDismissible: Boolean
    get() = this is Error || this is Cancelled
}

private fun Checkout?.toViewState(
  previousState: BankIdSignViewState,
): BankIdSignViewState {
  return when (this?.status) {
    Checkout.CheckoutStatus.COMPLETED -> BankIdSignViewState.BankIdSuccess
    Checkout.CheckoutStatus.SIGNED -> BankIdSignViewState.BankIdSuccess
    Checkout.CheckoutStatus.FAILED -> BankIdSignViewState.Error(this.statusText)
    Checkout.CheckoutStatus.UNKNOWN -> BankIdSignViewState.Error(this.statusText)
    Checkout.CheckoutStatus.PENDING, null -> {
      when (previousState) {
        // Keep "Open BankID" up in the screen if it wasn't opened yet.
        BankIdSignViewState.StartBankId -> BankIdSignViewState.StartBankId
        else -> BankIdSignViewState.SignInProgress
      }
    }
  }
}
