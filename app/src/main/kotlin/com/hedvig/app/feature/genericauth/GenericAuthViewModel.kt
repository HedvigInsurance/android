package com.hedvig.app.feature.genericauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.android.EmailAddressWithTrimmedWhitespaces
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GenericAuthViewModel(
  private val createOtpAttemptUseCase: CreateOtpAttemptUseCase,
) : ViewModel() {

  private val _viewState = MutableStateFlow(GenericAuthViewState())
  val viewState = _viewState.asStateFlow()

  fun setInput(value: String) {
    _viewState.update {
      it.copy(
        emailInput = value,
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
        createStateFromOtpAttempt(emailInput)
      }
    } else {
      _viewState.update { it.copy(error = validate(emailInput)) }
    }
  }

  fun onStartOtpInput() {
    _viewState.update { it.copy(otpId = null) }
  }

  private suspend fun createStateFromOtpAttempt(email: EmailAddressWithTrimmedWhitespaces) {
    _viewState.update { it.copy(loading = true) }
    val newState = when (val result = createOtpAttemptUseCase.invoke(email.value)) {
      is CreateOtpResult.Success -> _viewState.value.copy(
        otpId = result.id,
        error = null,
        loading = false,
      )
      CreateOtpResult.Error -> _viewState.value.copy(
        error = GenericAuthViewState.TextFieldError.NETWORK_ERROR,
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
  val error: TextFieldError? = null,
  val otpId: String? = null,
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
