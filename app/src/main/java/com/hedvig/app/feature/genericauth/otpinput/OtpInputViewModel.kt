package com.hedvig.app.feature.genericauth.otpinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.util.ErrorEvent
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

    private val eventChannel = Channel<Event>(Channel.UNLIMITED)
    val eventsFlow = eventChannel.receiveAsFlow()

    data class ViewState(
        val input: String = "",
        val credential: String,
        val errorMessage: String? = null,
        val errorEvent: ErrorEvent? = null,
        val loadingResend: Boolean = false,
        val loadingCode: Boolean = false
    )

    sealed class Event {
        data class Success(val authToken: String) : Event()
        object ShowDialog : Event()
        object CodeResent : Event()
        object None : Event()
    }

    fun setInput(value: String) {
        _viewState.update {
            it.copy(input = value)
        }
    }

    fun submitCode(code: String) {
        eventChannel.trySend(Event.None)
        _viewState.update {
            it.copy(loadingCode = true, errorMessage = null)
        }
        viewModelScope.launch {
            when (val result = sendOtpCodeUseCase.invoke(otpId, code)) {
                is SendOtpCodeUseCase.OtpResult.NetworkError -> result.handleNetworkError()
                is SendOtpCodeUseCase.OtpResult.OtpError -> result.handleOtpError()
                is SendOtpCodeUseCase.OtpResult.Success -> result.handleSuccess()
            }
        }
    }

    fun resendCode() {
        eventChannel.trySend(Event.None)
        _viewState.update {
            it.copy(errorMessage = null, loadingResend = true)
        }
        viewModelScope.launch {
            when (val result = reSendOtpCodeUseCase.invoke(credential)) {
                is ReSendOtpCodeUseCase.ResendOtpResult.Error -> result.handleError()
                is ReSendOtpCodeUseCase.ResendOtpResult.Success -> result.handleSuccess()
            }
        }
    }

    private fun SendOtpCodeUseCase.OtpResult.Success.handleSuccess() {
        authenticationTokenService.authenticationToken = authToken
        eventChannel.trySend(Event.Success(authToken))
        _viewState.update {
            it.copy(errorMessage = null, loadingCode = false)
        }
    }

    private fun ReSendOtpCodeUseCase.ResendOtpResult.Success.handleSuccess() {
        eventChannel.trySend(Event.CodeResent)
        _viewState.update {
            it.copy(errorEvent = null, errorMessage = null, input = "", loadingResend = false)
        }
    }

    private fun SendOtpCodeUseCase.OtpResult.NetworkError.handleNetworkError() {
        eventChannel.trySend(Event.ShowDialog)
        _viewState.update {
            it.copy(errorMessage = message, loadingCode = false)
        }
    }

    private fun SendOtpCodeUseCase.OtpResult.OtpError.handleOtpError() {
        _viewState.update {
            it.copy(errorEvent = error, loadingCode = false)
        }
    }

    private fun ReSendOtpCodeUseCase.ResendOtpResult.Error.handleError() {
        eventChannel.trySend(Event.ShowDialog)
        _viewState.update {
            it.copy(errorMessage = message, loadingResend = false)
        }
    }
}
