package com.hedvig.app.feature.zignsec

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.app.util.LiveEvent
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.authlib.StatusUrl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class SimpleSignAuthenticationViewModel(
  private val data: SimpleSignAuthenticationData,
  private val authRepository: AuthRepository,
  private val authTokenService: AuthTokenService,
) : ViewModel() {
  private val _input = MutableLiveData("")
  val input: LiveData<String> = _input
  val isValid = input.map {
    when (data.market) {
      Market.NO -> NORWEGIAN_NATIONAL_IDENTITY_NUMBER.matches(it)
      Market.DK -> DANISH_PERSONAL_IDENTIFICATION_NUMBER.matches(it)
      else -> false
    }
  }

  private val _isSubmitting = MutableLiveData(false)
  val isSubmitting: LiveData<Boolean> = _isSubmitting

  private val _zignSecUrl = MutableLiveData<String>()
  val zignSecUrl: LiveData<String> = _zignSecUrl

  private val _statusUrl = MutableLiveData<StatusUrl>()
  private val statusUrl: LiveData<StatusUrl> = _statusUrl

  private val _events = LiveEvent<Event>()
  val events: LiveData<Event> = _events

  sealed class Event {
    object Error : Event() {
      override fun toString() = "Error"
    }
  }

  /**
   * While this flow is active, we listen to changes in authentication status and report Error/Success in [events]
   */
  suspend fun subscribeToAuthSuccessEvent() {
    statusUrl.asFlow().collectLatest { statusUrl ->
      authRepository.observeLoginStatus(statusUrl)
        .distinctUntilChanged()
        .onCompletion {
          logcat { "subscribeToAuthSuccessEvent finished" }
        }
        .collect { loginStatusResult ->
          logcat { "Login status:$loginStatusResult" }
          when (loginStatusResult) {
            is LoginStatusResult.Completed -> {
              onSimpleSignSuccess(loginStatusResult)
            }

            is LoginStatusResult.Failed -> _events.postValue(Event.Error)
            is LoginStatusResult.Pending -> {}
            is LoginStatusResult.Exception -> _events.postValue(Event.Error)
          }
        }
    }
  }

  fun setInput(text: String) {
    _input.value = text
  }

  fun authFailed() {
    _events.value = Event.Error
  }

  fun startZignSec() {
    if (isSubmitting.value == true) {
      return
    }
    _isSubmitting.value = true
    val nationalIdentityNumber = input.value ?: return

    viewModelScope.launch {
      val result = authRepository.startLoginAttempt(
        loginMethod = LoginMethod.ZIGNSEC,
        market = data.market.name,
        personalNumber = nationalIdentityNumber,
      )
      handleStartAuth(result)
    }
  }

  private fun handleStartAuth(result: AuthAttemptResult) {
    logcat { "Auth start result:$result" }
    when (result) {
      is AuthAttemptResult.BankIdProperties -> _events.postValue(Event.Error)
      is AuthAttemptResult.Error -> _events.postValue(Event.Error)
      is AuthAttemptResult.OtpProperties -> _events.postValue(Event.Error)
      is AuthAttemptResult.ZignSecProperties -> {
        _zignSecUrl.postValue(result.redirectUrl)
        _statusUrl.postValue(result.statusUrl)
      }
    }
    _isSubmitting.postValue(false)
  }

  private suspend fun onSimpleSignSuccess(loginStatusResult: LoginStatusResult.Completed) {
    logcat { "Simple sign success:$loginStatusResult" }
    when (val result = authRepository.exchange(loginStatusResult.authorizationCode)) {
      is AuthTokenResult.Error -> {
        _events.postValue(Event.Error)
        logcat(LogPriority.ERROR) { "Login exchange error:$result" }
      }

      is AuthTokenResult.Success -> {
        logcat { "Login exchange success:$result" }
        authTokenService.loginWithTokens(result.accessToken, result.refreshToken)
      }
    }
  }

  companion object {
    private val DANISH_PERSONAL_IDENTIFICATION_NUMBER = Regex("[0-9]{10}")
    private val NORWEGIAN_NATIONAL_IDENTITY_NUMBER = Regex("[0-9]{11}")
  }
}
