package com.hedvig.app.feature.genericauth.otpinput

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthRepository
import com.hedvig.android.auth.AuthTokenResult
import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.android.auth.LoginMethod
import com.hedvig.android.auth.ResendOtpResult
import com.hedvig.android.auth.StatusUrl
import com.hedvig.android.auth.SubmitOtpResult
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
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
  private val authenticationTokenService: AuthenticationTokenService,
  private val authRepository: AuthRepository,
  private val uploadMarketAndLanguagePreferencesUseCase: UploadMarketAndLanguagePreferencesUseCase,
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
    data class Success(val authToken: String) : Event()
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
      when (val otpResult = authRepository.submitOtp(verifyUrl, code)) {
        is SubmitOtpResult.Error -> setErrorState(otpResult.message)
        is SubmitOtpResult.Success -> submitAuthCode(otpResult)
      }
    }
  }

  private suspend fun submitAuthCode(otpResult: SubmitOtpResult.Success) {
    when (val submitAuthCodeResult = authRepository.submitAuthorizationCode(otpResult.loginAuthorizationCode)) {
      is AuthTokenResult.Error -> setErrorState(submitAuthCodeResult.message)
      is AuthTokenResult.Success -> {
        authenticationTokenService.authenticationToken = submitAuthCodeResult.accessToken.token
        _events.trySend(Event.Success(submitAuthCodeResult.accessToken.token))
        _viewState.update {
          it.copy(loadingCode = false)
        }
        uploadMarketAndLanguagePreferencesUseCase.invoke()
      }
    }
  }

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
      when (val result = authRepository.resendOtp(resendUrl)) {
        is ResendOtpResult.Error -> {
          _viewState.update {
            it.copy(networkErrorMessage = result.message, loadingResend = false)
          }
        }
        ResendOtpResult.Success -> {
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
}
