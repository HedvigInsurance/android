package com.hedvig.app.feature.genericauth

import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.embark.EMAIL_REGEX
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GenericAuthViewModel : ViewModel() {
    private val _viewState = MutableStateFlow(
        ViewState(
            input = "",
            error = ViewState.InputError.EMPTY,
            dirty = false,
            touched = false,
        )
    )
    val viewState = _viewState.asStateFlow()

    data class ViewState(
        val input: String,
        val error: InputError?,
        val dirty: Boolean,
        val touched: Boolean,
    ) {
        enum class InputError {
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

    private fun computeError(state: ViewState, value: String) =
        if (state.touched && state.dirty) {
            validate(value)
        } else {
            null
        }

    private fun validate(value: String): ViewState.InputError? {
        if (value.isBlank()) {
            return ViewState.InputError.EMPTY
        }

        if (!EMAIL_REGEX.matcher(value).find()) { // TODO: Move the regex out to a common package
            return ViewState.InputError.INVALID_EMAIL
        }
        return null
    }
}
