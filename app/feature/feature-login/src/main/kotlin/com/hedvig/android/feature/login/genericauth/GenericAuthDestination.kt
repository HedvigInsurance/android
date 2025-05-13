package com.hedvig.android.feature.login.genericauth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@Composable
internal fun GenericAuthDestination(
  viewModel: GenericAuthViewModel,
  navigateUp: () -> Unit,
  onStartOtpInput: (verifyUrl: String, resendUrl: String, email: String) -> Unit,
) {
  val uiState by viewModel.viewState.collectAsStateWithLifecycle()
  GenericAuthScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onStartOtpInput = { verifyUrl: String, resendUrl: String ->
      viewModel.onStartOtpInput()
      onStartOtpInput(verifyUrl, resendUrl, uiState.emailInputWithoutWhitespaces.value)
    },
    setEmailInput = viewModel::setEmailInput,
    submitEmail = viewModel::submitEmail,
  )
}

@Composable
private fun GenericAuthScreen(
  uiState: GenericAuthViewState,
  navigateUp: () -> Unit,
  onStartOtpInput: (verifyUrl: String, resendUrl: String) -> Unit,
  setEmailInput: (String) -> Unit,
  submitEmail: () -> Unit,
) {
  LaunchedEffect(uiState.verifyUrl) {
    val verifyUrl = uiState.verifyUrl ?: return@LaunchedEffect
    val resendUrl = uiState.resendUrl ?: return@LaunchedEffect
    onStartOtpInput(verifyUrl, resendUrl)
  }

  HedvigTheme {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
      modifier = Modifier.fillMaxSize(),
    ) {
      EmailInputScreen(
        onUpClick = navigateUp,
        onInputChanged = setEmailInput,
        onSubmitEmail = submitEmail,
        emailInput = uiState.emailInput,
        error = uiState.error?.let { errorMessage(it) },
        loading = uiState.loading,
      )
    }
  }
}

@Composable
private fun errorMessage(error: GenericAuthViewState.TextFieldError): String {
  return when (error) {
    is GenericAuthViewState.TextFieldError.Message -> error.message
    is GenericAuthViewState.TextFieldError.Other -> {
      when (error) {
        GenericAuthViewState.TextFieldError.Other.Empty -> {
          stringResource(hedvig.resources.R.string.login_text_input_email_error_enter_email)
        }

        GenericAuthViewState.TextFieldError.Other.InvalidEmail -> {
          stringResource(hedvig.resources.R.string.login_text_input_email_error_not_valid)
        }

        GenericAuthViewState.TextFieldError.Other.NetworkError -> {
          stringResource(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE)
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewGenericAuthScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      GenericAuthScreen(
        GenericAuthViewState(),
        navigateUp = {},
        onStartOtpInput = { _, _ -> },
        setEmailInput = {},
        submitEmail = {},
      )
    }
  }
}
