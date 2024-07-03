package com.hedvig.android.feature.login.otpinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthTokenService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpInputViewModel(
  private val verifyUrl: String,
  private val resendUrl: String,
  credential: String,
  private val authTokenService: AuthTokenService,
//  private val authRepository: AuthRepository,
) : ViewModel() {
  private val _viewState = MutableStateFlow(ViewState(credential = credential))
  val viewState = _viewState.asStateFlow()

  private val _events = Channel<Event>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

  data class ViewState(
    val input: String = "",
    val credential: String,
    val networkErrorMessage: String? = null,
    val loadingResend: Boolean = false,
    val loadingCode: Boolean = false,
  )

  sealed class Event {
    data class Success(
      val authToken: String,
    ) : Event()

    object CodeResent : Event()
  }

  fun setInput(value: String) {
    _viewState.update {
      it.copy(input = value, networkErrorMessage = null)
    }
  }

  fun submitCode(code: String) {
    _viewState.update {
      it.copy(loadingCode = true, networkErrorMessage = null)
    }

    viewModelScope.launch {
      TODO("todo does not work without authlib")
//      when (val otpResult = authRepository.submitOtp(verifyUrl, code)) {
//        is SubmitOtpResult.Error -> setErrorState(otpResult.message)
//        is SubmitOtpResult.Success -> submitAuthCode(otpResult)
//      }
    }
  }

//  private suspend fun submitAuthCode(otpResult: SubmitOtpResult.Success) {
//    when (val authCodeResult = authRepository.exchange(otpResult.loginAuthorizationCode)) {
//      is AuthTokenResult.Error -> setErrorState(
//        when (authCodeResult) {
//          is AuthTokenResult.Error.BackendErrorResponse -> "Error:${authCodeResult.message}"
//          is AuthTokenResult.Error.IOError -> "IO Error:${authCodeResult.message}"
//          is AuthTokenResult.Error.UnknownError -> authCodeResult.message
//        },
//      )
//
//      is AuthTokenResult.Success -> {
//        authTokenService.loginWithTokens(
//          authCodeResult.accessToken,
//          authCodeResult.refreshToken,
//        )
//        _events.trySend(Event.Success(authCodeResult.accessToken.token))
//        _viewState.update {
//          it.copy(loadingCode = false)
//        }
//      }
//    }
//  }

  private fun setErrorState(message: String) {
    _viewState.update {
      it.copy(networkErrorMessage = message, loadingCode = false)
    }
  }

  fun resendCode() {
    _viewState.update {
      it.copy(networkErrorMessage = null, loadingResend = true)
    }
    viewModelScope.launch {
      TODO("todo does not work without authlib")
//      when (val result = authRepository.resendOtp(resendUrl)) {
//        is Error -> {
//          _viewState.update {
//            it.copy(networkErrorMessage = result.message, loadingResend = false)
//          }
//        }
//
//        Success -> {
//          _events.trySend(Event.CodeResent)
//          _viewState.update {
//            it.copy(networkErrorMessage = null, input = "", loadingResend = false)
//          }
//        }
//      }
    }
  }

  fun dismissError() {
    _viewState.update {
      it.copy(networkErrorMessage = null)
    }
  }
}
