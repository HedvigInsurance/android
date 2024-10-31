package com.hedvig.android.feature.login.genericauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class GenericAuthViewModel(
  private val marketManager: MarketManager,
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
          createLoginAttempt = {},
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
        createLoginAttempt = {},
      )
    }
  }

  fun onStartOtpInput() {
    _viewState.update { it.copy(verifyUrl = null) }
  }

  private suspend fun createStateFromOtpAttempt(createLoginAttempt: suspend () -> Unit) {}

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
