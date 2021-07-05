package com.hedvig.app.feature.offer.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.util.ValidationResult
import com.hedvig.app.util.validateEmail
import com.hedvig.app.util.validateNationalIdentityNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn

private const val INPUT_DELAY_MS = 1500L

class CheckoutViewModel : ViewModel() {

    protected val _viewState = MutableStateFlow<ViewState>(
        ViewState.Input(
            emailInputState = ViewState.InputState.NoInput,
            identityInputState = ViewState.InputState.NoInput
        )
    )
    val viewState: StateFlow<ViewState> = _viewState

    private val emailInput = MutableStateFlow("")
    private val identityNumberInput = MutableStateFlow("")

    init {
        emailInput.debounce(INPUT_DELAY_MS)
            .combine(identityNumberInput.debounce(INPUT_DELAY_MS)) { emailInput, identityNumberInput ->
                _viewState.value = ViewState.Input(
                    emailInputState = createInputState(emailInput, ::validateEmail),
                    identityInputState = createInputState(identityNumberInput, ::validateNationalIdentityNumber)
                )
            }.launchIn(viewModelScope)
    }

    fun onEmailChanged(input: String) {
        emailInput.value = input
    }

    fun onIdentityNumberChanged(input: String) {
        identityNumberInput.value = input
    }

    private fun createInputState(input: String, validate: (String) -> ValidationResult): ViewState.InputState {
        return if (input.isEmpty()) {
            ViewState.InputState.NoInput
        } else {
            val result = validate(input)
            if (result.isSuccessful) {
                ViewState.InputState.Valid(input)
            } else {
                ViewState.InputState.Invalid(input, result.errorTextKey)
            }
        }
    }

    sealed class ViewState {
        data class Input(
            val emailInputState: InputState,
            val identityInputState: InputState
        ) : ViewState() {
            val allValid = emailInputState is InputState.Valid && identityInputState is InputState.Valid
        }

        object Loading : ViewState()
        sealed class InputState {
            data class Valid(val input: String) : InputState()
            data class Invalid(val input: String, val stringRes: Int?) : InputState()
            object NoInput : InputState()
        }
    }
}
