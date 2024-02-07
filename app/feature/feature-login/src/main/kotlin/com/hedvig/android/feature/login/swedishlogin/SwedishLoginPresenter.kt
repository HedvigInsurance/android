package com.hedvig.android.feature.login.swedishlogin

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class SwedishLoginPresenter(
  private val authTokenService: AuthTokenService,
  private val authRepository: AuthRepository,
  private val demoManager: DemoManager,
) : MoleculePresenter<SwedishLoginEvent, SwedishLoginUiState> {
  @Composable
  override fun MoleculePresenterScope<SwedishLoginEvent>.present(lastState: SwedishLoginUiState): SwedishLoginUiState {
    var startLoginAttemptFailed: Boolean by remember { mutableStateOf(false) }
    var bankIdProperties: AuthAttemptResult.BankIdProperties? by remember { mutableStateOf(null) }
    var navigateToLoginScreen: Boolean by remember { mutableStateOf(false) }
    var retryIndex: Int by remember { mutableIntStateOf(0) }

    // Allow BankID the first time, and never again. This is so that users can fail the first attempt of logging in
    // with BankID, and then they should be able to login with the QR code directly, without being forced into BankID
    // again.
    var allowOpeningBankId: Boolean by remember { mutableStateOf(true) }

    val loginStatusResult = remember(retryIndex) {
      snapshotFlow { bankIdProperties }.flatMapConcat { bankIdProperties ->
        if (bankIdProperties == null) return@flatMapConcat flowOf(null)
        authRepository.observeLoginStatus(bankIdProperties.statusUrl)
          .map { loginStatusResult ->
            if (loginStatusResult is LoginStatusResult.Completed) {
              when (val authTokenResult = authRepository.exchange(loginStatusResult.authorizationCode)) {
                is AuthTokenResult.Error -> {
                  logcat(LogPriority.ERROR) { "Login failed, with error: $authTokenResult" }
                  return@map when (authTokenResult) {
                    is AuthTokenResult.Error.BackendErrorResponse -> {
                      LoginStatusResult.Failed(
                        "Error code:${authTokenResult.httpStatusValue}. ${authTokenResult.message}",
                      )
                    }

                    is AuthTokenResult.Error.IOError -> LoginStatusResult.Failed("IO Error ${authTokenResult.message}")
                    is AuthTokenResult.Error.UnknownError -> LoginStatusResult.Failed(authTokenResult.message)
                  }
                }

                is AuthTokenResult.Success -> {
                  authTokenService.loginWithTokens(
                    authTokenResult.accessToken,
                    authTokenResult.refreshToken,
                  )
                  navigateToLoginScreen = true
                  logcat(LogPriority.INFO) { "Logged in!" }
                }
              }
            }
            loginStatusResult
          }
      }
    }.collectAsState(initial = null).value

    LaunchedEffect(retryIndex) {
      val result = authRepository.startLoginAttempt(LoginMethod.SE_BANKID, Market.SE.name)
      when (result) {
        is AuthAttemptResult.BankIdProperties -> bankIdProperties = result
        is AuthAttemptResult.Error -> {
          logcat(LogPriority.ERROR) { "Got Error when signing in with BankId: ${result.toString()}" }
          startLoginAttemptFailed = true
        }

        is AuthAttemptResult.ZignSecProperties -> {
          logcat(LogPriority.ERROR) { "Got ZignSec properties when signing in with BankId" }
          startLoginAttemptFailed = true
        }

        is AuthAttemptResult.OtpProperties -> {
          logcat(LogPriority.ERROR) { "Got Otp properties when signing in with BankId" }
          startLoginAttemptFailed = true
        }
      }
    }

    CollectEvents { event ->
      when (event) {
        SwedishLoginEvent.Retry -> {
          Snapshot.withMutableSnapshot {
            startLoginAttemptFailed = false
            bankIdProperties = null
            navigateToLoginScreen = false
            retryIndex++
          }
        }

        SwedishLoginEvent.DidOpenBankIDApp -> allowOpeningBankId = false
        SwedishLoginEvent.DidNavigateToLoginScreen -> navigateToLoginScreen = false
        SwedishLoginEvent.StartDemoMode -> launch { demoManager.setDemoMode(true) }
      }
    }

    if (startLoginAttemptFailed) {
      return SwedishLoginUiState.StartLoginAttemptFailed
    }
    val bankIdPropertiesValue = bankIdProperties
    if (bankIdPropertiesValue == null || loginStatusResult == null) {
      return SwedishLoginUiState.Loading
    }
    if (loginStatusResult is LoginStatusResult.Failed) {
      return SwedishLoginUiState.BankIdError(loginStatusResult.message)
    }
    if (loginStatusResult is LoginStatusResult.Exception) {
      logcat(LogPriority.ERROR) { "Got exception for login status: ${loginStatusResult.message}" }
      return SwedishLoginUiState.BankIdError(loginStatusResult.message)
    }
    return SwedishLoginUiState.HandlingBankId(
      autoStartToken = SwedishLoginUiState.HandlingBankId.AutoStartToken(bankIdPropertiesValue.autoStartToken),
      loginStatusResult = loginStatusResult,
      allowOpeningBankId = allowOpeningBankId,
      navigateToLoginScreen = navigateToLoginScreen,
    )
  }
}

internal sealed interface SwedishLoginEvent {
  data object Retry : SwedishLoginEvent

  data object DidOpenBankIDApp : SwedishLoginEvent

  data object DidNavigateToLoginScreen : SwedishLoginEvent

  data object StartDemoMode : SwedishLoginEvent
}

internal sealed interface SwedishLoginUiState {
  data class HandlingBankId(
    val autoStartToken: AutoStartToken,
    val loginStatusResult: LoginStatusResult,
    val allowOpeningBankId: Boolean,
    val navigateToLoginScreen: Boolean,
  ) : SwedishLoginUiState {
    @JvmInline
    value class AutoStartToken(val token: String) {
      // Used to generate the QR code that BankID can read
      val autoStartUrl: String
        get() = "bankid:///?autostarttoken=$token"

      // The Uri which opens the BankId app while also passing in the right autoStartUrl
      val bankIdUri: Uri
        get() = Uri.parse("$autoStartUrl&redirect=null")
    }
  }

  data object Loading : SwedishLoginUiState

  data class BankIdError(val message: String) : SwedishLoginUiState

  data object StartLoginAttemptFailed : SwedishLoginUiState
}
