package com.hedvig.android.feature.login.otpinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.ResendOtpResult.Error
import com.hedvig.authlib.ResendOtpResult.Success
import com.hedvig.authlib.SubmitOtpResult
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpInputViewModel(
  private val verifyUrl: String,
  private val resendUrl: String,
  credential: String,
  private val authTokenService: AuthTokenService,
  private val authRepository: AuthRepository,
  coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
) : ViewModel(coroutineScope) {
  private val _viewState = MutableStateFlow(ViewState.initial(credential = credential))
  val viewState = combine(
    _viewState,
    authTokenService.authStatus,
  ) { viewState, authStatus ->
    viewState.copy(
      navigateToLoginScreen = authStatus is AuthStatus.LoggedIn,
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    _viewState.value,
  )

  private val _events = Channel<Event>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

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
      when (val otpResult = authRepository.submitOtp(verifyUrl, code)) {
        is SubmitOtpResult.Error -> setErrorState(otpResult.message)
        is SubmitOtpResult.Success -> submitAuthCode(otpResult)
      }
    }
  }

  fun resendCode() {
    _viewState.update {
      it.copy(networkErrorMessage = null, loadingResend = true)
    }
    viewModelScope.launch {
      when (val result = authRepository.resendOtp(resendUrl)) {
        is Error -> {
          _viewState.update {
            it.copy(networkErrorMessage = result.message, loadingResend = false)
          }
        }

        Success -> {
          _events.trySend(Event.CodeResent)
          _viewState.update {
            it.copy(networkErrorMessage = null, input = "", loadingResend = false)
          }
        }
      }
    }
  }

  fun dismissError() {
    _viewState.update {
      it.copy(networkErrorMessage = null)
    }
  }

  private suspend fun submitAuthCode(otpResult: SubmitOtpResult.Success) {
    when (val authCodeResult = authRepository.exchange(otpResult.loginAuthorizationCode)) {
      is AuthTokenResult.Error -> setErrorState(
        when (authCodeResult) {
          is AuthTokenResult.Error.BackendErrorResponse -> "Error:${authCodeResult.message}"
          is AuthTokenResult.Error.IOError -> "IO Error:${authCodeResult.message}"
          is AuthTokenResult.Error.UnknownError -> authCodeResult.message
        },
      )

      is AuthTokenResult.Success -> {
        authTokenService.loginWithTokens(
          authCodeResult.accessToken,
          authCodeResult.refreshToken,
        )
        _viewState.update {
          it.copy(loadingCode = false)
        }
      }
    }
  }

  private fun setErrorState(message: String) {
    _viewState.update {
      it.copy(networkErrorMessage = message, loadingCode = false)
    }
  }

  data class ViewState(
    val input: String = "",
    val credential: String,
    val networkErrorMessage: String?,
    val loadingResend: Boolean,
    val loadingCode: Boolean,
    val navigateToLoginScreen: Boolean,
  ) {
    companion object {
      fun initial(credential: String): ViewState = ViewState(
        credential = credential,
        input = "",
        networkErrorMessage = null,
        loadingResend = false,
        loadingCode = false,
        navigateToLoginScreen = false,
      )
    }
  }

  sealed class Event {
    object CodeResent : Event()
  }
}
