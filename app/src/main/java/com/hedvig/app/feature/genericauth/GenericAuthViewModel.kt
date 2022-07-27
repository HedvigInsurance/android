package com.hedvig.app.feature.genericauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.util.EMAIL_REGEX
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GenericAuthViewModel(
  private val createOtpAttemptUseCase: CreateOtpAttemptUseCase,
) : ViewModel() {
  private val eventChannel = Channel<Event>(Channel.UNLIMITED)
  val eventsFlow = eventChannel.receiveAsFlow()

  sealed class Event {
    data class SubmitEmailSuccess(val id: String, val credential: String) : Event()
  }

  private val _viewState = MutableStateFlow(ViewState())
  val viewState = _viewState.asStateFlow()

  data class ViewState(
    val input: String = "",
    val error: TextFieldError? = null,
    val otpId: String? = null,
    val submitEnabled: Boolean = false,
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
        submitEnabled = isValid(value),
      )
    }
  }

  fun clear() {
    _viewState.update { ViewState() }
  }

  fun submitEmail() {
    with(_viewState) {
      if (!value.submitEnabled) {
        update { it.copy(error = ViewState.TextFieldError.INVALID_EMAIL) }
      } else {
        viewModelScope.launch {
          update { it.copy(loading = true) }
          handleOtpAttempt(value.input)
          update { it.copy(loading = false) }
        }
      }
    }
  }

  private suspend fun handleOtpAttempt(email: String) {
    when (val result = createOtpAttemptUseCase.invoke(email)) {
      is CreateOtpResult.Success -> _viewState.update { it.copy(otpId = result.id, error = null) }
      CreateOtpResult.Error -> _viewState.update { it.copy(error = ViewState.TextFieldError.NETWORK_ERROR) }
    }
  }

  private fun validate(email: String): ViewState.TextFieldError? {
    if (email.isBlank()) {
      return ViewState.TextFieldError.EMPTY
    }

    if (!EMAIL_REGEX.matcher(email).find()) {
      return ViewState.TextFieldError.INVALID_EMAIL
    }
    return null
  }

  private fun isValid(email: String) = validate(email) == null
}
