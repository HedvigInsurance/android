package com.hedvig.app.feature.genericauth.otpinput

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.authenticate.AuthenticationTokenService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpInputViewModel(
    private val otpId: String,
    private val credential: String,
    private val authenticationTokenService: AuthenticationTokenService,
    private val sendOtpCodeUseCase: SendOtpCodeUseCase,
    private val reSendOtpCodeUseCase: ReSendOtpCodeUseCase,
) : ViewModel() {
    private val _viewState = MutableStateFlow(ViewState(credential = credential))
    val viewState = _viewState.asStateFlow()

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    data class ViewState(
        val input: String = "",
        val credential: String,
        val networkErrorMessage: String? = null,
        @StringRes
        val otpError: Int? = null,
        val loadingResend: Boolean = false,
        val loadingCode: Boolean = false
    )

    sealed class Event {
        data class Success(val authToken: String) : Event()
        object CodeResent : Event()
    }

    fun setInput(value: String) {
        _viewState.update {
            it.copy(input = value)
        }
    }

    fun submitCode(code: String) {
        _viewState.update {
            it.copy(loadingCode = true, networkErrorMessage = null)
        }
        viewModelScope.launch {
            when (val result = sendOtpCodeUseCase.invoke(otpId, code)) {
                is OtpResult.Error -> result.handleError()
                is OtpResult.Success -> result.handleSuccess()
            }
        }
    }

    fun resendCode() {
        _viewState.update {
            it.copy(networkErrorMessage = null, loadingResend = true)
        }
        viewModelScope.launch {
            when (val result = reSendOtpCodeUseCase.invoke(credential)) {
                is ResendOtpResult.Error -> result.handleError()
                is ResendOtpResult.Success -> result.handleSuccess()
            }
        }
    }

    fun dismissError() {
        _viewState.update {
            it.copy(networkErrorMessage = null)
        }
    }

    private fun OtpResult.Success.handleSuccess() {
        authenticationTokenService.authenticationToken = authToken
        _events.trySend(Event.Success(authToken))
        _viewState.update {
            it.copy(loadingCode = false)
        }
    }

    private fun ResendOtpResult.Success.handleSuccess() {
        _events.trySend(Event.CodeResent)
        _viewState.update {
            it.copy(otpError = null, input = "", loadingResend = false)
        }
    }

    private fun OtpResult.Error.handleError() {
        when (val error = this) {
            is OtpResult.Error.NetworkError -> {
                _viewState.update {
                    it.copy(networkErrorMessage = error.message, loadingCode = false)
                }
            }
            is OtpResult.Error.OtpError -> {
                _viewState.update {
                    it.copy(otpError = error.toStringRes(), loadingCode = false)
                }
            }
        }
    }

    private fun ResendOtpResult.Error.handleError() {
        _viewState.update {
            it.copy(networkErrorMessage = message, loadingResend = false)
        }
    }
}
