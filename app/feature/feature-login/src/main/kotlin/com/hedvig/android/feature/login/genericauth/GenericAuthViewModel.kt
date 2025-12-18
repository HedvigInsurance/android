package com.hedvig.android.feature.login.genericauth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.OtpMarket
import kotlinx.coroutines.launch

internal class GenericAuthViewModel(
  authRepository: AuthRepository,
) : MoleculeViewModel<GenericAuthEvent, GenericAuthViewState>(
    initialState = GenericAuthViewState(),
    presenter = GenericAuthPresenter(
      startLoginAttempt = { email ->
        authRepository.startLoginAttempt(
          loginMethod = LoginMethod.OTP,
          market = OtpMarket.SE,
          personalNumber = null,
          email = email,
        )
      },
    ),
  )

internal class GenericAuthPresenter(
  private val startLoginAttempt: suspend (email: String) -> AuthAttemptResult,
) : MoleculePresenter<GenericAuthEvent, GenericAuthViewState> {
  @Composable
  override fun MoleculePresenterScope<GenericAuthEvent>.present(
    lastState: GenericAuthViewState,
  ): GenericAuthViewState {
    var uiState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        is GenericAuthEvent.SetEmailInput -> {
          uiState = uiState.copy(
            emailInput = event.value,
            error = null,
          )
        }

        is GenericAuthEvent.SubmitEmail -> {
          val emailInput = uiState.emailInputWithoutWhitespaces
          if (emailInput.isValid) {
            if (uiState.loading) return@CollectEvents
            uiState = uiState.copy(loading = true)
            launch {
              val newState = when (val result = startLoginAttempt(emailInput.value)) {
                is AuthAttemptResult.BankIdProperties -> uiState.copy(
                  error = GenericAuthViewState.TextFieldError.Other.NetworkError,
                  loading = false,
                )

                is AuthAttemptResult.Error -> {
                  val error = when (result) {
                    is AuthAttemptResult.Error.Localised -> {
                      GenericAuthViewState.TextFieldError.Message(result.reason)
                    }
                    is AuthAttemptResult.Error.BackendErrorResponse,
                    is AuthAttemptResult.Error.IOError,
                    is AuthAttemptResult.Error.UnknownError,
                    -> {
                      GenericAuthViewState.TextFieldError.Other.NetworkError
                    }
                  }
                  uiState.copy(
                    error = error,
                    loading = false,
                  )
                }

                is AuthAttemptResult.OtpProperties -> uiState.copy(
                  verifyUrl = result.verifyUrl,
                  resendUrl = result.resendUrl,
                  error = null,
                  loading = false,
                )
              }
              uiState = newState
            }
          } else {
            uiState = uiState.copy(error = validate(emailInput))
          }
        }

        is GenericAuthEvent.OnStartOtpInput -> {
          uiState = uiState.copy(verifyUrl = null)
        }
      }
    }

    return uiState
  }

  private fun validate(email: EmailAddressWithTrimmedWhitespaces): GenericAuthViewState.TextFieldError? {
    if (email.value.isBlank()) {
      return GenericAuthViewState.TextFieldError.Other.Empty
    }

    if (email.isValid.not()) {
      return GenericAuthViewState.TextFieldError.Other.InvalidEmail
    }
    return null
  }
}

internal sealed interface GenericAuthEvent {
  data class SetEmailInput(val value: String) : GenericAuthEvent

  data object SubmitEmail : GenericAuthEvent

  data object OnStartOtpInput : GenericAuthEvent
}

data class GenericAuthViewState(
  val emailInput: String = "",
  val error: TextFieldError? = null,
  val verifyUrl: String? = null,
  val resendUrl: String? = null,
  val loading: Boolean = false,
) {
  val emailInputWithoutWhitespaces: EmailAddressWithTrimmedWhitespaces
    get() = EmailAddressWithTrimmedWhitespaces(emailInput)

  sealed interface TextFieldError {
    data class Message(val message: String) : TextFieldError

    sealed interface Other : TextFieldError {
      data object Empty : Other

      data object InvalidEmail : Other

      data object NetworkError : Other
    }
  }
}
