package com.hedvig.android.feature.login.swedishlogin

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.authlib.OtpMarket
import com.hedvig.authlib.StatusUrl
import kotlinx.coroutines.launch

@OptIn(SavedStateHandleSaveableApi::class)
internal class SwedishLoginPresenter(
  private val authTokenService: AuthTokenService,
  private val authRepository: AuthRepository,
  private val demoManager: DemoManager,
  private val savedStateHandle: SavedStateHandle,
) : MoleculePresenter<SwedishLoginEvent, SwedishLoginUiState> {
  @Composable
  override fun MoleculePresenterScope<SwedishLoginEvent>.present(lastState: SwedishLoginUiState): SwedishLoginUiState {
    var startLoginAttemptFailed: Boolean by remember { mutableStateOf(false) }
    var bankIdProperties: AuthAttemptResult.BankIdProperties? by remember(savedStateHandle) {
      savedStateHandle.saveable(
        key = BankIdPropertiesSaver.toString(),
        stateSaver = BankIdPropertiesSaver,
      ) {
        mutableStateOf(null)
      }
    }
    var navigateToLoginScreen: Boolean by remember { mutableStateOf(false) }
    var retryIndex: Int by remember { mutableIntStateOf(0) }

    // Allow BankID the first time, and never again. This is so that users can fail the first attempt of logging in
    // with BankID, and then they should be able to login with the QR code directly, without being forced into BankID
    // again.
    var allowOpeningBankId: Boolean by remember(savedStateHandle) {
      savedStateHandle.saveable("allowOpeningBankId") {
        mutableStateOf(true)
      }
    }
    var loginStatusResult: LoginStatusResult? by remember { mutableStateOf(null) }

    LaunchedEffect(retryIndex) {
      snapshotFlow { bankIdProperties }.collect { bankIdProperties ->
        if (bankIdProperties == null) {
          loginStatusResult = null
          return@collect
        }
        authRepository.observeLoginStatus(bankIdProperties.statusUrl).collect { latestLoginStatusResult ->
          logcat { "Stelios:observed latestLoginStatusResult:$latestLoginStatusResult" }
          if (latestLoginStatusResult is LoginStatusResult.Completed) {
            when (val authTokenResult = authRepository.exchange(latestLoginStatusResult.authorizationCode)) {
              is AuthTokenResult.Error -> {
                logcat(LogPriority.ERROR) { "Login failed, with error: $authTokenResult" }
                loginStatusResult = when (authTokenResult) {
                  is AuthTokenResult.Error.BackendErrorResponse -> {
                    LoginStatusResult.Exception("Error: ${authTokenResult.message}")
                  }

                  is AuthTokenResult.Error.IOError -> LoginStatusResult.Exception("IO Error ${authTokenResult.message}")
                  is AuthTokenResult.Error.UnknownError -> LoginStatusResult.Exception(authTokenResult.message)
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
          } else {
            if (latestLoginStatusResult is LoginStatusResult.Exception) {
              logcat(LogPriority.ERROR) { "Got exception for login status: ${latestLoginStatusResult.message}" }
            }
            loginStatusResult = latestLoginStatusResult
          }
        }
      }
    }

    LaunchedEffect(retryIndex) {
      if (bankIdProperties != null) {
        return@LaunchedEffect
      }
      val result = authRepository.startLoginAttempt(LoginMethod.SE_BANKID, OtpMarket.SE)
      when (result) {
        is AuthAttemptResult.BankIdProperties -> bankIdProperties = result
        is AuthAttemptResult.Error -> {
          logcat(LogPriority.ERROR) { "Got Error when signing in with BankId: $result" }
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
        SwedishLoginEvent.StartDemoMode -> launch {
          demoManager.setDemoMode(true)
          navigateToLoginScreen = true
        }
      }
    }

    if (startLoginAttemptFailed) {
      return SwedishLoginUiState.StartLoginAttemptFailed
    }
    val bankIdPropertiesValue = bankIdProperties
    if (bankIdPropertiesValue == null) {
      return SwedishLoginUiState.Loading(navigateToLoginScreen)
    }
    val loginStatusResultValue = loginStatusResult
    return when (loginStatusResultValue) {
      null -> SwedishLoginUiState.Loading(navigateToLoginScreen)
      is LoginStatusResult.Failed -> SwedishLoginUiState.BankIdError(loginStatusResultValue.localisedMessage, navigateToLoginScreen)
      is LoginStatusResult.Exception -> SwedishLoginUiState.BankIdError(loginStatusResultValue.message, navigateToLoginScreen)
      is LoginStatusResult.Completed -> SwedishLoginUiState.LoggedIn(navigateToLoginScreen)
      is LoginStatusResult.Pending -> {
        SwedishLoginUiState.HandlingBankId(
          statusMessage = loginStatusResultValue.statusMessage,
          autoStartToken = SwedishLoginUiState.HandlingBankId.AutoStartToken(bankIdPropertiesValue.autoStartToken),
          bankIdLiveQrCodeData = loginStatusResultValue.bankIdProperties?.liveQrCodeData?.let {
            SwedishLoginUiState.HandlingBankId.BankIdLiveQrCodeData(it)
          },
          bankIdAppOpened = loginStatusResultValue.bankIdProperties?.bankIdAppOpened == true,
          allowOpeningBankId = allowOpeningBankId,
          navigateToLoginScreen = navigateToLoginScreen,
        )
      }
    }
  }
}

internal sealed interface SwedishLoginEvent {
  data object Retry : SwedishLoginEvent

  data object DidOpenBankIDApp : SwedishLoginEvent

  data object DidNavigateToLoginScreen : SwedishLoginEvent

  data object StartDemoMode : SwedishLoginEvent
}

internal sealed interface SwedishLoginUiState {
  val navigateToLoginScreen: Boolean

  data class HandlingBankId(
    val statusMessage: String,
    val autoStartToken: AutoStartToken,
    val bankIdLiveQrCodeData: BankIdLiveQrCodeData?,
    val bankIdAppOpened: Boolean,
    val allowOpeningBankId: Boolean,
    override val navigateToLoginScreen: Boolean,
  ) : SwedishLoginUiState {
    @JvmInline
    value class AutoStartToken(val token: String) {
      // The Uri which opens the BankId app while also passing in the right autoStartUrl
      val bankIdUri: Uri
        get() = Uri.parse("bankid:///?autostarttoken=$token&redirect=null")
    }

    @JvmInline
    value class BankIdLiveQrCodeData(val data: String)
  }

  data class Loading(override val navigateToLoginScreen: Boolean) : SwedishLoginUiState

  data class LoggedIn(override val navigateToLoginScreen: Boolean) : SwedishLoginUiState

  data class BankIdError(val message: String, override val navigateToLoginScreen: Boolean) : SwedishLoginUiState

  data object StartLoginAttemptFailed : SwedishLoginUiState {
    override val navigateToLoginScreen: Boolean = false
  }
}

private val BankIdPropertiesSaver: Saver<AuthAttemptResult.BankIdProperties?, Any> =
  listSaver<AuthAttemptResult.BankIdProperties?, String>(
    save = { properties ->
      properties ?: return@listSaver emptyList()
      listOf(properties.id, properties.statusUrl.url, properties.autoStartToken)
    },
    restore = { list ->
      if (list.isEmpty()) return@listSaver null
      AuthAttemptResult.BankIdProperties(
        id = list[0],
        statusUrl = StatusUrl(list[1]),
        autoStartToken = list[2],
      )
    },
  )
