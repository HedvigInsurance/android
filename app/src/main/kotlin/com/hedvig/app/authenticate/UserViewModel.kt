package com.hedvig.app.authenticate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AccessToken
import com.hedvig.android.auth.AuthAttemptResult
import com.hedvig.android.auth.AuthRepository
import com.hedvig.android.auth.AuthTokenResult
import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.android.auth.AuthorizationCode
import com.hedvig.android.auth.LoginMethod
import com.hedvig.android.auth.LoginStatusResult
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.market.Market
import com.hedvig.app.feature.chat.data.UserRepository
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class UserViewModel(
  private val logoutUserCase: LogoutUseCase,
  private val hAnalytics: HAnalytics,
  private val featureManager: FeatureManager,
  private val pushTokenManager: PushTokenManager,
  private val uploadMarketAndLanguagePreferencesUseCase: UploadMarketAndLanguagePreferencesUseCase,
  private val authenticationTokenService: AuthenticationTokenService,
  private val authRepository: AuthRepository,
) : ViewModel() {

  data class ViewState(
    val autoStartToken: String? = null,
    val authStatus: LoginStatusResult? = null,
    val navigateToLoggedIn: Boolean = false,
  )

  private val mutableViewState = MutableStateFlow(ViewState())
  val viewState: StateFlow<ViewState>
    get() = mutableViewState

  private val _events = Channel<Event>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

  sealed class Event {
    object Logout : Event()
    data class Error(val message: String?) : Event()
  }

  fun fetchBankIdStartToken() {
    viewModelScope.launch {
      val result = authRepository.startLoginAttempt(
        loginMethod = LoginMethod.SE_BANKID,
        market = Market.SE.name,
      )

      when (result) {
        is AuthAttemptResult.BankIdProperties -> observeBankIdStatus(result)
        is AuthAttemptResult.Error -> Timber.e(result.message)
        is AuthAttemptResult.ZignSecProperties -> Timber.e("Got ZignSec properties when signing in with BankId")
      }
    }
  }

  private suspend fun observeBankIdStatus(result: AuthAttemptResult.BankIdProperties) {
    mutableViewState.update {
      it.copy(autoStartToken = result.autoStartToken)
    }

    authRepository.observeLoginStatus(result.statusUrl)
      .onEach { loginStatusResult ->
        mutableViewState.update {
          it.copy(authStatus = loginStatusResult)
        }
      }
      .mapLatest {
        if (it is LoginStatusResult.Completed) {
          submitCode(it.authorizationCode)
        }
      }
      .launchIn(viewModelScope)
  }

  private suspend fun submitCode(authorizationCode: AuthorizationCode) {
    when (val result = authRepository.submitAuthorizationCode(authorizationCode)) {
      is AuthTokenResult.Error -> Timber.e(result.message)
      is AuthTokenResult.Success -> {
        runCatching {
          pushTokenManager.refreshToken()
        }
        onAuthSuccess(result.accessToken)
      }
    }
  }

  private suspend fun onAuthSuccess(accessToken: AccessToken) {
    hAnalytics.loggedIn()
    featureManager.invalidateExperiments()
    authenticationTokenService.authenticationToken = accessToken.token
    uploadMarketAndLanguagePreferencesUseCase.invoke()
    mutableViewState.update {
      it.copy(navigateToLoggedIn = true)
    }
  }

  fun logout() {
    viewModelScope.launch {
      when (val result = logoutUserCase.invoke()) {
        is LogoutUseCase.LogoutResult.Error -> {
          _events.trySend(Event.Error(result.message))
        }
        LogoutUseCase.LogoutResult.Success -> {
          hAnalytics.loggedOut()
          _events.trySend(Event.Logout)
        }
      }
    }
  }
}
