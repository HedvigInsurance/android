package com.hedvig.app.authenticate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.AuthTokenServiceProvider
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.app.feature.marketing.data.UploadMarketAndLanguagePreferencesUseCase
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.hanalytics.HAnalytics
import kotlin.time.Duration.Companion.seconds
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

class BankIdLoginViewModel(
  private val hAnalytics: HAnalytics,
  private val featureManager: FeatureManager,
  private val uploadMarketAndLanguagePreferencesUseCase: UploadMarketAndLanguagePreferencesUseCase,
  private val authTokenServiceProvider: AuthTokenServiceProvider,
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
              logcat(LogPriority.ERROR) { "Login failed, with error: ${authTokenResult.message}" }
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
          logcat(LogPriority.ERROR) { "Got Error when signing in with BankId: ${result.message}" }
          startLoginAttemptFailed.update { "Got Error when signing in with BankId: ${result.message}" }
        }

        is AuthAttemptResult.ZignSecProperties -> {
          logcat(LogPriority.ERROR) { "Got ZignSec properties when signing in with BankId" }
          startLoginAttemptFailed.update { "Got ZignSec properties when signing in with BankId" }
        }

        is AuthAttemptResult.OtpProperties -> {
          logcat(LogPriority.ERROR) { "Got Otp properties when signing in with BankId" }
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
      logcat(LogPriority.ERROR) { "Got exception for login status: ${loginStatusResult.message}" }
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
    authTokenServiceProvider.provide().loginWithTokens(
      authTokenResult.accessToken,
      authTokenResult.refreshToken,
    )
    featureManager.invalidateExperiments()
    uploadMarketAndLanguagePreferencesUseCase.invoke()
    hAnalytics.loggedIn()
    logcat(LogPriority.INFO) { "Logged in!" }
  }
}

sealed interface BankIdLoginViewState {
  data object Loading : BankIdLoginViewState

  data class Error(val message: String) : BankIdLoginViewState

  data class HandlingBankId(
    val autoStartToken: String,
    val processedAutoStartToken: Boolean,
    val authStatus: LoginStatusResult,
    val processedNavigationToLoggedIn: Boolean,
  ) : BankIdLoginViewState
}
