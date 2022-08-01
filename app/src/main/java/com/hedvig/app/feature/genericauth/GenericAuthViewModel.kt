package com.hedvig.app.feature.genericauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.util.isValidEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GenericAuthViewModel(
  private val createOtpAttemptUseCase: CreateOtpAttemptUseCase,
) : ViewModel() {

  private val _viewState = MutableStateFlow(ViewState())
  val viewState = _viewState.asStateFlow()

  data class ViewState(
    val input: String = "",
    val error: TextFieldError? = null,
    val otpId: String? = null,
    val loading: Boolean = false,
  ) {
    enum class TextFieldError {
      EMPTY,
      INVALID_EMAIL,
      NETWORK_ERROR,
    }
  }

  fun setInput(value: String) {
    _viewState.update {
      it.copy(
        input = value,
        error = null,
      )
    }
  }

  fun clear() {
    _viewState.update { ViewState() }
  }

  fun submitEmail() {
    if (isValidEmail(_viewState.value.input)) {
      viewModelScope.launch {
        createStateFromOtpAttempt(_viewState.value.input)
      }
    } else {
      _viewState.update { it.copy(error = validate(_viewState.value.input)) }
    }
  }

  fun onStartOtpInput() {
    _viewState.update { it.copy(otpId = null) }
  }

  private suspend fun createStateFromOtpAttempt(email: String) {
    _viewState.update { it.copy(loading = true) }
    val newState = when (val result = createOtpAttemptUseCase.invoke(email)) {
      is CreateOtpResult.Success -> _viewState.value.copy(
        otpId = result.id,
        error = null,
        loading = false,
      )
      CreateOtpResult.Error -> _viewState.value.copy(
        error = ViewState.TextFieldError.NETWORK_ERROR,
        loading = false,
      )
    }
    _viewState.value = newState
  }

  private fun validate(email: String): ViewState.TextFieldError? {
    if (email.isBlank()) {
      return ViewState.TextFieldError.EMPTY
    }

    if (!isValidEmail(email)) {
      return ViewState.TextFieldError.INVALID_EMAIL
    }
    return null
  }
}
