package com.hedvig.app.feature.zignsec

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.market.Market
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.app.util.LiveEvent
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.authlib.StatusUrl
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SimpleSignAuthenticationViewModel(
  private val data: SimpleSignAuthenticationData,
  private val hAnalytics: HAnalytics,
  private val featureManager: FeatureManager,
  private val uploadMarketAndLanguagePreferencesUseCase: UploadMarketAndLanguagePreferencesUseCase,
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
  val statusUrl: LiveData<StatusUrl> = _statusUrl

  private val _events = LiveEvent<Event>()
  val events: LiveData<Event> = _events

  sealed class Event {
    object Success : Event()
    object Error : Event()
    object LoadWebView : Event()
    object CancelSignIn : Event()
  }

  /**
   * While this flow is active, we listen to changes in authentication status and report Error/Success in [events]
   */
  suspend fun subscribeToAuthSuccessEvent() {
    statusUrl.asFlow().collectLatest { statusUrl ->
      authRepository.observeLoginStatus(statusUrl).collect { loginStatusResult ->
        when (loginStatusResult) {
          is LoginStatusResult.Completed -> {
            onSimpleSignSuccess(loginStatusResult)
            _events.postValue(Event.Success)
          }
          is LoginStatusResult.Failed -> _events.postValue(Event.Error)
          is LoginStatusResult.Pending -> {}
        }
      }
    }
  }

  fun setInput(text: CharSequence?) {
    text?.toString()?.let { _input.value = it }
  }

  fun authFailed() {
    _events.value = Event.Error
  }

  fun invalidMarket() {
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
    when (result) {
      is AuthAttemptResult.BankIdProperties -> _events.postValue(Event.Error)
      is AuthAttemptResult.Error -> _events.postValue(Event.Error)
      is AuthAttemptResult.OtpProperties -> _events.postValue(Event.Error)
      is AuthAttemptResult.ZignSecProperties -> {
        _zignSecUrl.postValue(result.redirectUrl)
        _statusUrl.postValue(result.statusUrl)
        _events.postValue(Event.LoadWebView)
      }
    }
    _isSubmitting.postValue(false)
  }

  fun cancelSignIn() {
    _events.value = Event.CancelSignIn
  }

  private suspend fun onSimpleSignSuccess(loginStatusResult: LoginStatusResult.Completed) {
    when (val result = authRepository.exchange(loginStatusResult.authorizationCode)) {
      is AuthTokenResult.Error -> {
        _events.postValue(Event.Error)
      }
      is AuthTokenResult.Success -> {
        hAnalytics.loggedIn()
        featureManager.invalidateExperiments()
        authTokenService.updateTokens(result.accessToken, result.refreshToken)
        uploadMarketAndLanguagePreferencesUseCase.invoke()
      }
    }
  }

  companion object {
    private val DANISH_PERSONAL_IDENTIFICATION_NUMBER = Regex("[0-9]{10}")
    private val NORWEGIAN_NATIONAL_IDENTITY_NUMBER = Regex("[0-9]{11}")
  }
}
