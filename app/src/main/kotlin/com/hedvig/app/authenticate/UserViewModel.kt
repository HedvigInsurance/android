package com.hedvig.app.authenticate

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
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import slimber.log.e
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
      is AuthTokenResult.Error -> e { result.message }
      is AuthTokenResult.Success -> {
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
    runCatching {
      pushTokenManager.refreshToken()
    }
  }

  fun logout() {
    logoutUserCase.invoke()
  }
}
