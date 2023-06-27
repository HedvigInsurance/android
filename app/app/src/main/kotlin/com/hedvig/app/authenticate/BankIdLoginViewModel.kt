package com.hedvig.app.authenticate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.market.Market
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import slimber.log.e
import slimber.log.i
import kotlin.time.Duration.Companion.seconds

class BankIdLoginViewModel(
  private val hAnalytics: HAnalytics,
  private val featureManager: FeatureManager,
  private val uploadMarketAndLanguagePreferencesUseCase: UploadMarketAndLanguagePreferencesUseCase,
  private val authTokenService: AuthTokenService,
  private val authRepository: AuthRepository,
) : ViewModel() {

  private val startLoginAttemptFailed: MutableStateFlow<String?> = MutableStateFlow(null)
  private val bankIdProperties: MutableStateFlow<AuthAttemptResult.BankIdProperties?> = MutableStateFlow(null)
  private val processedAutoStartToken: MutableStateFlow<Boolean> = MutableStateFlow(false)
  private val processedNavigationToLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
  private val loginStatusResult: Flow<LoginStatusResult?> = bankIdProperties.flatMapConcat { bankIdProperties ->
    if (bankIdProperties == null) return@flatMapConcat flowOf(null)
    authRepository.observeLoginStatus(bankIdProperties.statusUrl)
      .map { loginStatusResult ->
        if (loginStatusResult is LoginStatusResult.Completed) {
          when (val authTokenResult = authRepository.exchange(loginStatusResult.authorizationCode)) {
            is AuthTokenResult.Error -> {
              e { "Login failed, with error: ${authTokenResult.message}" }
              return@map LoginStatusResult.Failed(authTokenResult.message)
            }

            is AuthTokenResult.Success -> {
              login(authTokenResult)
            }
          }
        }
        loginStatusResult
      }
  }

  init {
    viewModelScope.launch {
      val result: AuthAttemptResult = authRepository.startLoginAttempt(
        loginMethod = LoginMethod.SE_BANKID,
        market = Market.SE.name,
      )

      when (result) {
        is AuthAttemptResult.BankIdProperties -> bankIdProperties.update { result }
        is AuthAttemptResult.Error -> {
          e { "Got Error when signing in with BankId: ${result.message}" }
          startLoginAttemptFailed.update { "Got Error when signing in with BankId: ${result.message}" }
        }

        is AuthAttemptResult.ZignSecProperties -> {
          e { "Got ZignSec properties when signing in with BankId" }
          startLoginAttemptFailed.update { "Got ZignSec properties when signing in with BankId" }
        }

        is AuthAttemptResult.OtpProperties -> {
          e { "Got Otp properties when signing in with BankId" }
          startLoginAttemptFailed.update { "Got Otp properties when signing in with BankId" }
        }
      }
    }
  }

  val viewState: StateFlow<BankIdLoginViewState> = combine(
    startLoginAttemptFailed,
    bankIdProperties,
    processedAutoStartToken,
    loginStatusResult,
    processedNavigationToLoggedIn,
  ) {
      startLoginAttemptFailedMessage, bankIdProperties, processedAutoStartToken, loginStatusResult,
      processedNavigationToLoggedIn,
    ->
    if (startLoginAttemptFailedMessage != null) {
      return@combine BankIdLoginViewState.Error(startLoginAttemptFailedMessage)
    }
    if (bankIdProperties == null || loginStatusResult == null) {
      return@combine BankIdLoginViewState.Loading
    }
    if (loginStatusResult is LoginStatusResult.Failed) {
      return@combine BankIdLoginViewState.Error(loginStatusResult.message)
    }
    if (loginStatusResult is LoginStatusResult.Exception) {
      e { "Got exception for login status: ${loginStatusResult.message}" }
      return@combine BankIdLoginViewState.Error(loginStatusResult.message)
    }
    BankIdLoginViewState.HandlingBankId(
      bankIdProperties.autoStartToken,
      processedAutoStartToken,
      loginStatusResult,
      processedNavigationToLoggedIn,
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    BankIdLoginViewState.Loading,
  )

  fun didProcessAutoStartToken() {
    processedAutoStartToken.update { true }
  }

  fun didNavigateToLoginScreen() {
    processedNavigationToLoggedIn.update { true }
  }

  private suspend fun login(authTokenResult: AuthTokenResult.Success) {
    authTokenService.loginWithTokens(
      authTokenResult.accessToken,
      authTokenResult.refreshToken,
    )
    featureManager.invalidateExperiments()
    uploadMarketAndLanguagePreferencesUseCase.invoke()
    hAnalytics.loggedIn()
    i { "Logged in!" }
  }
}

sealed interface BankIdLoginViewState {
  object Loading : BankIdLoginViewState {
    override fun toString(): String = "Loading"
  }

  data class Error(val message: String) : BankIdLoginViewState {
    override fun toString(): String = "Error: $message"
  }

  data class HandlingBankId(
    val autoStartToken: String,
    val processedAutoStartToken: Boolean,
    val authStatus: LoginStatusResult,
    val processedNavigationToLoggedIn: Boolean,
  ) : BankIdLoginViewState
}
