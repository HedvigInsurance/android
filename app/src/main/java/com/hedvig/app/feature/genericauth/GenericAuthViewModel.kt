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
    val error: TextFieldError? = TextFieldError.EMPTY,
  ) {
    enum class TextFieldError {
      EMPTY,
      INVALID_EMAIL,
      NETWORK_ERROR,
    }
  }

  fun setInput(value: String) {
    _viewState.update { it.copy(input = value) }
  }

  fun clear() {
    _viewState.update { ViewState() }
  }

  fun submitEmail() {
    viewModelScope.launch {
      // TODO: Set Loading-state, once one exists
      val email = viewState.value.input
      val error = validate(email)
      _viewState.update {
        it.copy(error = error)
      }
      when (val result = createOtpAttemptUseCase.invoke(email)) {
        is CreateOtpResult.Success -> {
          eventChannel.trySend(Event.SubmitEmailSuccess(result.id, email))
        }
        CreateOtpResult.Error -> {
          _viewState.update { it.copy(error = ViewState.TextFieldError.NETWORK_ERROR) }
        }
      }
      // TODO: Remove Loading-state, once one exists
    }
  }

  private fun validate(value: String): ViewState.TextFieldError? {
    if (value.isBlank()) {
      return ViewState.TextFieldError.EMPTY
    }

    if (!EMAIL_REGEX.matcher(value).find()) {
      return ViewState.TextFieldError.INVALID_EMAIL
    }
    return null
  }
}
