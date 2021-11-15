package com.hedvig.app.feature.genericauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.embark.EMAIL_REGEX
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

    private val _viewState = MutableStateFlow(
        ViewState(
            input = "",
            error = ViewState.TextFieldError.EMPTY,
            dirty = false,
            touched = false,
        )
    )
    val viewState = _viewState.asStateFlow()

    data class ViewState(
        val input: String,
        val error: TextFieldError?,
        val dirty: Boolean,
        val touched: Boolean,
    ) {
        enum class TextFieldError {
            EMPTY,
            INVALID_EMAIL,
        }
    }

    fun setInput(value: String) {
        _viewState.update { previousState ->
            previousState.copy(
                input = value,
                dirty = true,
                error = computeError(previousState, value),
            )
        }
    }

    fun clear() {
        _viewState.update { it.copy(input = "", error = null, dirty = false, touched = false) }
    }

    fun blur() {
        _viewState.update { previousState ->
            val newState = previousState.copy(touched = previousState.dirty)
            val error = computeError(newState, newState.input)
            newState.copy(error = error)
        }
    }

    fun submitEmail() {
        viewModelScope.launch {
            // TODO: Set Loading-state, once one exists
            val email = viewState.value.input
            when (val result = createOtpAttemptUseCase.invoke(email)) {
                is CreateOtpAttemptUseCase.Result.Success -> {
                    eventChannel.trySend(Event.SubmitEmailSuccess(result.id, email))
                }
                CreateOtpAttemptUseCase.Result.Error -> {
                    _viewState.update { it.copy(error = ViewState.TextFieldError.INVALID_EMAIL) }
                }
            }
            // TODO: Remove Loading-state, once one exists
        }
    }

    private fun computeError(state: ViewState, value: String) =
        if (state.touched && state.dirty) {
            validate(value)
        } else {
            null
        }

    private fun validate(value: String): ViewState.TextFieldError? {
        if (value.isBlank()) {
            return ViewState.TextFieldError.EMPTY
        }

        if (!EMAIL_REGEX.matcher(value).find()) { // TODO: Move the regex out to a common package
            return ViewState.TextFieldError.INVALID_EMAIL
        }
        return null
    }
}
