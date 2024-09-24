package com.hedvig.android.feature.login.genericauth

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.market.Market
import com.hedvig.android.market.Market.DK
import com.hedvig.android.market.Market.NO
import com.hedvig.android.market.Market.SE

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
    setSSNInput = viewModel::setSSNInput,
    submitSSN = viewModel::submitSSN,
  )
}

@Composable
private fun GenericAuthScreen(
  uiState: GenericAuthViewState,
  navigateUp: () -> Unit,
  onStartOtpInput: (verifyUrl: String, resendUrl: String) -> Unit,
  setEmailInput: (String) -> Unit,
  submitEmail: () -> Unit,
  setSSNInput: (String) -> Unit,
  submitSSN: () -> Unit,
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
      val market = uiState.market
      when (market) {
        SE -> EmailInputScreen(
          onUpClick = navigateUp,
          onInputChanged = setEmailInput,
          onSubmitEmail = submitEmail,
          emailInput = uiState.emailInput,
          error = uiState.error?.let { errorMessage(it) },
          loading = uiState.loading,
        )

        NO,
        DK,
        -> SSNInputScreen(
          market = market,
          onUpClick = navigateUp,
          onInputChanged = setSSNInput,
          onSubmitSSN = submitSSN,
          emailInput = uiState.ssnInput,
          error = uiState.error?.let { errorMessage(it) },
          loading = uiState.loading,
          canSubmitSsn = uiState.canSubmitSsn,
        )
      }
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
private fun Preview(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isSe: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      GenericAuthScreen(
        GenericAuthViewState(market = if (isSe) Market.SE else Market.DK),
        navigateUp = {},
        onStartOtpInput = { _, _ -> },
        setEmailInput = {},
        submitEmail = {},
        setSSNInput = {},
        submitSSN = {},
      )
    }
  }
}
