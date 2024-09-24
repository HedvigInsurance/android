package com.hedvig.android.feature.login.genericauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.OtpMarket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class GenericAuthViewModel(
  private val marketManager: MarketManager,
  private val authRepository: AuthRepository,
) : ViewModel() {
  private val _viewState = MutableStateFlow(GenericAuthViewState(marketManager.market.value))
  val viewState = _viewState.asStateFlow()

  fun setEmailInput(value: String) {
    _viewState.update {
      it.copy(
        emailInput = value,
        error = null,
      )
    }
  }

  fun setSSNInput(value: String) {
    _viewState.update {
      it.copy(
        ssnInput = value,
        error = null,
      )
    }
  }

  fun submitEmail() {
    val emailInput = _viewState.value.emailInputWithoutWhitespaces
    if (emailInput.isValid) {
      viewModelScope.launch {
        createStateFromOtpAttempt(
          createLoginAttempt = {
            authRepository.startLoginAttempt(
              loginMethod = LoginMethod.OTP,
              market = when (marketManager.market.value) {
                Market.SE -> OtpMarket.SE
                Market.NO -> OtpMarket.NO
                Market.DK -> OtpMarket.DK
              },
              personalNumber = null,
              email = emailInput.value,
            )
          },
        )
      }
    } else {
      _viewState.update { it.copy(error = validate(emailInput)) }
    }
  }

  fun submitSSN() {
    val ssnInput = _viewState.value.ssnInput
    viewModelScope.launch {
      createStateFromOtpAttempt(
        createLoginAttempt = {
          authRepository.startLoginAttempt(
            loginMethod = LoginMethod.OTP,
            market = when (marketManager.market.value) {
              Market.SE -> OtpMarket.SE
              Market.NO -> OtpMarket.NO
              Market.DK -> OtpMarket.DK
            },
            personalNumber = ssnInput,
            email = null,
          )
        },
      )
    }
  }

  fun onStartOtpInput() {
    _viewState.update { it.copy(verifyUrl = null) }
  }

  private suspend fun createStateFromOtpAttempt(createLoginAttempt: suspend () -> AuthAttemptResult) {
    _viewState.update { it.copy(loading = true) }
    val newState = when (val startLoginResult = createLoginAttempt()) {
      is AuthAttemptResult.BankIdProperties -> _viewState.value.copy(
        error = GenericAuthViewState.TextFieldError.Other.NetworkError,
        loading = false,
      )

      is AuthAttemptResult.Error -> {
        val error = when (startLoginResult) {
          is AuthAttemptResult.Error.Localised -> GenericAuthViewState.TextFieldError.Message(startLoginResult.reason)
          is AuthAttemptResult.Error.BackendErrorResponse,
          is AuthAttemptResult.Error.IOError,
          is AuthAttemptResult.Error.UnknownError,
          -> {
            GenericAuthViewState.TextFieldError.Other.NetworkError
          }
        }
        _viewState.value.copy(
          error = error,
          loading = false,
        )
      }

      is AuthAttemptResult.OtpProperties -> _viewState.value.copy(
        verifyUrl = startLoginResult.verifyUrl,
        resendUrl = startLoginResult.resendUrl,
        error = null,
        loading = false,
      )
    }

    _viewState.update { newState }
  }

  private fun validate(email: EmailAddressWithTrimmedWhitespaces): GenericAuthViewState.TextFieldError? {
    if (email.value.isBlank()) {
      return GenericAuthViewState.TextFieldError.Other.Empty
    }

    if (email.isValid.not()) {
      return GenericAuthViewState.TextFieldError.Other.InvalidEmail
    }
    return null
  }
}

data class GenericAuthViewState(
  val market: Market,
  val emailInput: String = "",
  val ssnInput: String = "",
  val error: TextFieldError? = null,
  val verifyUrl: String? = null,
  val resendUrl: String? = null,
  val loading: Boolean = false,
) {
  val emailInputWithoutWhitespaces: EmailAddressWithTrimmedWhitespaces
    get() = EmailAddressWithTrimmedWhitespaces(emailInput)

  val canSubmitSsn: Boolean
    get() = when (market) {
      Market.SE -> error("Should not be able to enter SSN for generic auth for SE")
      Market.NO -> ssnInput.length == 11
      Market.DK -> ssnInput.length == 10
    }

  sealed interface TextFieldError {
    data class Message(val message: String) : TextFieldError

    sealed interface Other : TextFieldError {
      data object Empty : Other

      data object InvalidEmail : Other

      data object NetworkError : Other
    }
  }
}
