package com.hedvig.app.feature.genericauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.android.EmailAddressWithTrimmedWhitespaces
import com.hedvig.android.market.MarketManager
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.LoginMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GenericAuthViewModel(
  val marketManager: MarketManager,
  private val authRepository: AuthRepository,
) : ViewModel() {
  private val _viewState = MutableStateFlow(GenericAuthViewState())
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


  fun clear() {
    _viewState.update { GenericAuthViewState() }
  }

  fun submitEmail() {
    val emailInput = _viewState.value.emailInputWithoutWhitespaces
    if (emailInput.isValid) {
      viewModelScope.launch {
        createStateFromOtpAttempt(
          createLoginAttempt = {
            authRepository.startLoginAttempt(
              loginMethod = LoginMethod.OTP,
              market = marketManager.market.value.name,
              personalNumber = null,
              email = emailInput.value,
            )
          }
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
            market = marketManager.market.value.name,
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

  private suspend fun createStateFromOtpAttempt(
    createLoginAttempt: suspend () -> AuthAttemptResult,
  ) {
    _viewState.update { it.copy(loading = true) }
    val newState = when (val startLoginResult = createLoginAttempt()) {
      is AuthAttemptResult.BankIdProperties,
      is AuthAttemptResult.ZignSecProperties,
      is AuthAttemptResult.Error,
      -> _viewState.value.copy(
        error = GenericAuthViewState.TextFieldError.NETWORK_ERROR,
        loading = false,
      )

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
      return GenericAuthViewState.TextFieldError.EMPTY
    }

    if (email.isValid.not()) {
      return GenericAuthViewState.TextFieldError.INVALID_EMAIL
    }
    return null
  }
}

data class GenericAuthViewState(
  val emailInput: String = "",
  val ssnInput: String = "",
  val error: TextFieldError? = null,
  val verifyUrl: String? = null,
  val resendUrl: String? = null,
  val loading: Boolean = false,
) {
  val emailInputWithoutWhitespaces: EmailAddressWithTrimmedWhitespaces
    get() = EmailAddressWithTrimmedWhitespaces(emailInput)

  enum class TextFieldError {
    EMPTY,
    INVALID_EMAIL,
    NETWORK_ERROR,
  }
}
