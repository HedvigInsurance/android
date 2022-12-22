package com.hedvig.app.authenticate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.market.Market
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.app.service.push.PushTokenManager
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.AuthorizationCodeGrant
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import slimber.log.e

class BankIdLoginViewModel(
  private val hAnalytics: HAnalytics,
  private val featureManager: FeatureManager,
  private val pushTokenManager: PushTokenManager,
  private val uploadMarketAndLanguagePreferencesUseCase: UploadMarketAndLanguagePreferencesUseCase,
  private val authTokenService: AuthTokenService,
  private val authRepository: AuthRepository,
) : ViewModel() {

  data class ViewState(
    val autoStartToken: String? = null,
    val authStatus: LoginStatusResult? = null,
    val navigateToLoggedIn: Boolean = false,
  )

  private val mutableViewState = MutableStateFlow(ViewState())
  val viewState: StateFlow<ViewState>
    get() = mutableViewState.asStateFlow()

  fun fetchBankIdStartToken() {
    viewModelScope.launch {
      val result = authRepository.startLoginAttempt(
        loginMethod = LoginMethod.SE_BANKID,
        market = Market.SE.name,
      )

      when (result) {
        is AuthAttemptResult.BankIdProperties -> observeBankIdStatus(result)
        is AuthAttemptResult.Error -> e { result.message }
        is AuthAttemptResult.ZignSecProperties -> e { "Got ZignSec properties when signing in with BankId" }
        is AuthAttemptResult.OtpProperties -> e { "Got Otp properties when signing in with BankId" }
      }
    }
  }

  private suspend fun observeBankIdStatus(bankIdProperties: AuthAttemptResult.BankIdProperties) {
    mutableViewState.update {
      it.copy(autoStartToken = bankIdProperties.autoStartToken)
    }

    authRepository.observeLoginStatus(bankIdProperties.statusUrl)
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

  private suspend fun submitCode(grant: AuthorizationCodeGrant) {
    when (val authTokenResult = authRepository.exchange(grant)) {
      is AuthTokenResult.Error -> e { authTokenResult.message }
      is AuthTokenResult.Success -> {
        onAuthSuccess(authTokenResult)
      }
    }
  }

  private suspend fun onAuthSuccess(authTokenResult: AuthTokenResult.Success) {
    hAnalytics.loggedIn()
    featureManager.invalidateExperiments()
    authTokenService.updateTokens(
      authTokenResult.accessToken,
      authTokenResult.refreshToken,
    )
    uploadMarketAndLanguagePreferencesUseCase.invoke()
    mutableViewState.update {
      it.copy(navigateToLoggedIn = true)
    }
    runCatching {
      pushTokenManager.refreshToken()
    }
  }
}
