package com.hedvig.android.feature.login.otpinput

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.Grant
import com.hedvig.authlib.ResendOtpResult
import com.hedvig.authlib.SubmitOtpResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OtpInputViewModel(
  verifyUrl: String,
  resendUrl: String,
  credential: String,
  authTokenService: AuthTokenService,
  authRepository: AuthRepository,
) : MoleculeViewModel<OtpInputEvent, OtpInputUiState>(
    initialState = OtpInputUiState(credential = credential),
    presenter = OtpInputPresenter(
      verifyUrl = verifyUrl,
      resendUrl = resendUrl,
      credential = credential,
      submitOtp = authRepository::submitOtp,
      resendOtp = authRepository::resendOtp,
      exchange = authRepository::exchange,
      loginWithTokens = authTokenService::loginWithTokens,
      authStatus = authTokenService.authStatus,
    ),
  )

internal class OtpInputPresenter(
  private val verifyUrl: String,
  private val resendUrl: String,
  private val credential: String,
  private val submitOtp: suspend (verifyUrl: String, otp: String) -> SubmitOtpResult,
  private val resendOtp: suspend (resendUrl: String) -> ResendOtpResult,
  private val exchange: suspend (grant: Grant) -> AuthTokenResult,
  private val loginWithTokens: suspend (
    accessToken: com.hedvig.authlib.AccessToken,
    refreshToken: com.hedvig.authlib.RefreshToken,
  ) -> Unit,
  private val authStatus: Flow<AuthStatus?>,
) : MoleculePresenter<OtpInputEvent, OtpInputUiState> {
  @Composable
  override fun MoleculePresenterScope<OtpInputEvent>.present(lastState: OtpInputUiState): OtpInputUiState {
    var uiState by remember { mutableStateOf(lastState.copy(credential = credential)) }

    // Observe auth status for navigation
    val currentAuthStatus by authStatus.collectAsState(initial = null)
    LaunchedEffect(currentAuthStatus) {
      if (currentAuthStatus is AuthStatus.LoggedIn) {
        uiState = uiState.copy(navigateToLoginScreen = true)
      }
    }

    CollectEvents { event ->
      when (event) {
        is OtpInputEvent.SetInput -> {
          uiState = uiState.copy(input = event.value, networkErrorMessage = null)
        }

        is OtpInputEvent.SubmitCode -> {
          if (uiState.loadingCode) return@CollectEvents
          uiState = uiState.copy(loadingCode = true, networkErrorMessage = null)
          launch {
            when (val otpResult = submitOtp(verifyUrl, event.code)) {
              is SubmitOtpResult.Error -> {
                uiState = uiState.copy(
                  networkErrorMessage = otpResult.message,
                  loadingCode = false,
                )
              }
              is SubmitOtpResult.Success -> {
                when (val authCodeResult = exchange(otpResult.loginAuthorizationCode)) {
                  is AuthTokenResult.Error -> {
                    val errorMessage = when (authCodeResult) {
                      is AuthTokenResult.Error.BackendErrorResponse -> "Error:${authCodeResult.message}"
                      is AuthTokenResult.Error.IOError -> "IO Error:${authCodeResult.message}"
                      is AuthTokenResult.Error.UnknownError -> authCodeResult.message
                    }
                    uiState = uiState.copy(
                      networkErrorMessage = errorMessage,
                      loadingCode = false,
                    )
                  }
                  is AuthTokenResult.Success -> {
                    loginWithTokens(authCodeResult.accessToken, authCodeResult.refreshToken)
                    uiState = uiState.copy(loadingCode = false)
                  }
                }
              }
            }
          }
        }

        is OtpInputEvent.ResendCode -> {
          if (uiState.loadingResend) return@CollectEvents
          uiState = uiState.copy(networkErrorMessage = null, loadingResend = true)
          launch {
            when (val result = resendOtp(resendUrl)) {
              is ResendOtpResult.Error -> {
                uiState = uiState.copy(
                  networkErrorMessage = result.message,
                  loadingResend = false,
                )
              }
              ResendOtpResult.Success -> {
                uiState = uiState.copy(
                  networkErrorMessage = null,
                  input = "",
                  loadingResend = false,
                  codeResentEvent = true,
                )
              }
            }
          }
        }

        is OtpInputEvent.DismissError -> {
          uiState = uiState.copy(networkErrorMessage = null)
        }

        is OtpInputEvent.HandledCodeResentEvent -> {
          uiState = uiState.copy(codeResentEvent = false)
        }
      }
    }

    return uiState
  }
}

sealed interface OtpInputEvent {
  data class SetInput(val value: String) : OtpInputEvent

  data class SubmitCode(val code: String) : OtpInputEvent

  data object ResendCode : OtpInputEvent

  data object DismissError : OtpInputEvent

  data object HandledCodeResentEvent : OtpInputEvent
}

data class OtpInputUiState(
  val input: String = "",
  val credential: String,
  val networkErrorMessage: String? = null,
  val loadingResend: Boolean = false,
  val loadingCode: Boolean = false,
  val navigateToLoginScreen: Boolean = false,
  val codeResentEvent: Boolean = false,
)
