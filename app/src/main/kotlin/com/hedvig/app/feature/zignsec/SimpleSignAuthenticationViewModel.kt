package com.hedvig.app.feature.zignsec

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.market.Market
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.zignsec.usecase.AuthResult
import com.hedvig.app.feature.zignsec.usecase.SimpleSignStartAuthResult
import com.hedvig.app.feature.zignsec.usecase.StartDanishAuthUseCase
import com.hedvig.app.feature.zignsec.usecase.StartNorwegianAuthUseCase
import com.hedvig.app.feature.zignsec.usecase.SubscribeToAuthResultUseCase
import com.hedvig.app.util.LiveEvent
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SimpleSignAuthenticationViewModel(
  private val data: SimpleSignAuthenticationData,
  private val startDanishAuthUseCase: StartDanishAuthUseCase,
  private val startNorwegianAuthUseCase: StartNorwegianAuthUseCase,
  private val hAnalytics: HAnalytics,
  private val featureManager: FeatureManager,
  private val loginStatusService: LoginStatusService,
  private val subscribeToAuthResultUseCase: SubscribeToAuthResultUseCase,
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
  fun subscribeToAuthSuccessEvent(): Flow<*> {
    return subscribeToAuthResultUseCase.invoke().onEach { authResult ->
      when (authResult) {
        AuthResult.Failed -> _events.postValue(Event.Error)
        AuthResult.Success -> {
          onAuthSuccess()
          _events.postValue(Event.Success)
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
    when (data.market) {
      Market.NO -> {
        val nationalIdentityNumber = input.value ?: return
        viewModelScope.launch {
          handleStartAuth(startNorwegianAuthUseCase.invoke(nationalIdentityNumber))
        }
      }
      Market.DK -> {
        val personalIdentificationNumber = input.value ?: return
        viewModelScope.launch {
          handleStartAuth(startDanishAuthUseCase.invoke(personalIdentificationNumber))
        }
      }
      else -> {
      }
    }
  }

  private fun handleStartAuth(result: SimpleSignStartAuthResult) {
    when (result) {
      is SimpleSignStartAuthResult.Success -> {
        _zignSecUrl.postValue(result.url)
        _events.postValue(Event.LoadWebView)
      }
      SimpleSignStartAuthResult.Error -> {
        _events.postValue(Event.Error)
      }
    }
    _isSubmitting.postValue(false)
  }

  fun cancelSignIn() {
    _events.value = Event.CancelSignIn
  }

  private suspend fun onAuthSuccess() {
    hAnalytics.loggedIn()
    featureManager.invalidateExperiments()
    loginStatusService.isLoggedIn = true
  }

  companion object {
    private val DANISH_PERSONAL_IDENTIFICATION_NUMBER = Regex("[0-9]{10}")
    private val NORWEGIAN_NATIONAL_IDENTITY_NUMBER = Regex("[0-9]{11}")
  }
}
