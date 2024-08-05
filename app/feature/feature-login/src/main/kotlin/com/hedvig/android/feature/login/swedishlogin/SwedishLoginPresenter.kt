package com.hedvig.android.feature.login.swedishlogin

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.hedvig.android.auth.AuthStatus
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
        key = BankIdPropertiesSaver.ID,
        stateSaver = BankIdPropertiesSaver(),
      ) {
        mutableStateOf(null)
      }
    }
    val navigateToLoginScreen: Boolean by isLoggedInAsState(demoManager, authTokenService)
    var retryIndex: Int by remember { mutableIntStateOf(0) }

    // Allow BankID the first time, and never again. This is so that users can fail the first attempt of logging in
    // with BankID, and then they should be able to login with the QR code directly, without being forced into BankID
    // again.
    var allowOpeningBankId: Boolean by remember(savedStateHandle) {
      savedStateHandle.saveable(
        key = "allowOpeningBankId",
        stateSaver = autoSaver(),
      ) {
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
            retryIndex++
          }
        }

        SwedishLoginEvent.DidOpenBankIDApp -> allowOpeningBankId = false
        SwedishLoginEvent.StartDemoMode -> {
          launch {
            demoManager.setDemoMode(true)
          }
        }
      }
    }

    if (startLoginAttemptFailed) {
      return SwedishLoginUiState(
        BankIdUiState.StartLoginAttemptFailed,
        navigateToLoginScreen,
      )
    }
    val bankIdPropertiesValue = bankIdProperties
    if (bankIdPropertiesValue == null) {
      return SwedishLoginUiState(
        BankIdUiState.Loading,
        navigateToLoginScreen,
      )
    }
    val loginStatusResultValue = loginStatusResult
    val bankIdUiState: BankIdUiState = when (loginStatusResultValue) {
      null -> BankIdUiState.Loading
      is LoginStatusResult.Failed -> BankIdUiState.BankIdError(loginStatusResultValue.localisedMessage)
      is LoginStatusResult.Exception -> BankIdUiState.BankIdError(loginStatusResultValue.message)
      is LoginStatusResult.Completed -> BankIdUiState.LoggedIn
      is LoginStatusResult.Pending -> {
        BankIdUiState.HandlingBankId(
          statusMessage = loginStatusResultValue.statusMessage,
          autoStartToken = BankIdUiState.HandlingBankId.AutoStartToken(bankIdPropertiesValue.autoStartToken),
          bankIdLiveQrCodeData = loginStatusResultValue.bankIdProperties?.liveQrCodeData?.let {
            BankIdUiState.HandlingBankId.BankIdLiveQrCodeData(it)
          },
          bankIdAppOpened = loginStatusResultValue.bankIdProperties?.bankIdAppOpened == true,
          allowOpeningBankId = allowOpeningBankId,
        )
      }
    }
    return SwedishLoginUiState(
      bankIdUiState,
      navigateToLoginScreen,
    )
  }
}

@Composable
private fun isLoggedInAsState(demoManager: DemoManager, authTokenService: AuthTokenService): State<Boolean> {
  val isDemoMode by demoManager.isDemoMode().collectAsState(false)
  val isLoggedIn by authTokenService.authStatus.collectAsState()
  return remember {
    derivedStateOf {
      isDemoMode || isLoggedIn is AuthStatus.LoggedIn
    }
  }
}

internal sealed interface SwedishLoginEvent {
  data object Retry : SwedishLoginEvent

  data object DidOpenBankIDApp : SwedishLoginEvent

  data object StartDemoMode : SwedishLoginEvent
}

internal data class SwedishLoginUiState(
  val bankIdUiState: BankIdUiState,
  val navigateToLoginScreen: Boolean,
)

internal sealed interface BankIdUiState {
  data class HandlingBankId(
    val statusMessage: String,
    val autoStartToken: AutoStartToken,
    val bankIdLiveQrCodeData: BankIdLiveQrCodeData?,
    val bankIdAppOpened: Boolean,
    val allowOpeningBankId: Boolean,
  ) : BankIdUiState {
    @JvmInline
    value class AutoStartToken(val token: String) {
      // The Uri which opens the BankId app while also passing in the right autoStartUrl
      val bankIdUri: Uri
        get() = Uri.parse("bankid:///?autostarttoken=$token&redirect=null")
    }

    @JvmInline
    value class BankIdLiveQrCodeData(val data: String)
  }

  data object Loading : BankIdUiState

  data object LoggedIn : BankIdUiState

  data class BankIdError(val message: String) : BankIdUiState

  data object StartLoginAttemptFailed : BankIdUiState
}

internal class BankIdPropertiesSaver : Saver<AuthAttemptResult.BankIdProperties?, Any> by bankIdPropertiesListSaver {
  companion object {
    const val ID = "BankIdPropertiesSaver"
  }
}

private val bankIdPropertiesListSaver = listSaver<AuthAttemptResult.BankIdProperties?, String>(
  save = { bankIdProperties: AuthAttemptResult.BankIdProperties? ->
    bankIdProperties ?: return@listSaver emptyList()
    listOf(bankIdProperties.id, bankIdProperties.statusUrl.url, bankIdProperties.autoStartToken)
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
