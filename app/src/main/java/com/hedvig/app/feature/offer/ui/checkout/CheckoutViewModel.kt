package com.hedvig.app.feature.offer.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.util.ValidationResult
import com.hedvig.app.util.validateEmail
import com.hedvig.app.util.validateNationalIdentityNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val quoteIds: List<String>,
    private val signQuotesUseCase: SignQuotesUseCase
) : ViewModel() {

    protected val _viewState = MutableStateFlow(
        ViewState(
            enableSign = false,
            emailInputState = ViewState.InputState.NoInput,
            identityInputState = ViewState.InputState.NoInput
        )
    )
    val viewState: StateFlow<ViewState> = _viewState

    private var emailInput: String = ""
    private var identityNumberInput: String = ""

    fun validateInput() {
        _viewState.value = _viewState.value.copy(
            emailInputState = createInputState(emailInput, ::validateEmail),
            identityInputState = createInputState(identityNumberInput, ::validateNationalIdentityNumber)
        )
    }

    fun onEmailChanged(input: String, hasError: Boolean) {
        emailInput = input
        setState(hasError)
    }

    fun onIdentityNumberChanged(input: String, hasError: Boolean) {
        identityNumberInput = input
        setState(hasError)
    }

    private fun setState(hasError: Boolean) {
        if (hasError) {
            validateInput()
        }
        setEnabledState()
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

    private fun setEnabledState() {
        _viewState.value = _viewState.value.copy(
            enableSign = emailInput.isNotBlank() && identityNumberInput.isNotBlank()
        )
    }

    fun onTrySign(emailInput: String, identityNumberInput: String) {
        if (viewState.value.canSign()) {
            signQuotes(
                identityNumberInput = identityNumberInput,
                emailInput = emailInput,
                quoteIds = quoteIds
            )
        }
    }

    private fun signQuotes(identityNumberInput: String, emailInput: String, quoteIds: List<String>) {
        viewModelScope.launch {
            val result = signQuotesUseCase.editAndSignQuotes(
                quoteIds = quoteIds,
                ssn = identityNumberInput,
                email = emailInput
            )
            when (result) {
                is SignQuotesUseCase.SignQuoteResult.Error -> TODO("Show error")
                SignQuotesUseCase.SignQuoteResult.Success -> TODO("Go to next screen")
            }
        }
    }

    data class ViewState(
        val enableSign: Boolean,
        val emailInputState: InputState,
        val identityInputState: InputState
    ) {
        sealed class InputState {
            data class Valid(val input: String) : InputState()
            data class Invalid(val input: String, val stringRes: Int?) : InputState()
            object NoInput : InputState()
        }
    }

    private fun ViewState.canSign(): Boolean {
        return emailInputState is ViewState.InputState.Valid && identityInputState is ViewState.InputState.Valid
    }
}
