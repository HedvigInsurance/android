package com.hedvig.android.feature.insurance.certificate.ui.email

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTooltip
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TooltipDefaults.BeakDirection.TopEnd
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoOutline
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import hedvig.resources.R

@Composable
internal fun InsuranceEvidenceEmailInputDestination(
  viewModel: InsuranceEvidenceEmailInputViewModel,
  navigateToShowCertificate: (url: String) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  InsuranceEvidenceEmailInputScreen(
    uiState,
    navigateToShowCertificate = navigateToShowCertificate,
    navigateUp = navigateUp,
    onRetry = {
      viewModel.emit(InsuranceEvidenceEmailInputEvent.RetryLoadData)
    },
    onChangeEmail = { email ->
      viewModel.emit(InsuranceEvidenceEmailInputEvent.ChangeEmailInput(email))
    },
    onSubmit = {
      viewModel.emit(InsuranceEvidenceEmailInputEvent.Submit)
    },
    onClearNavigation = {
      viewModel.emit(InsuranceEvidenceEmailInputEvent.ClearNavigation)
    },
  )
}

@Composable
private fun InsuranceEvidenceEmailInputScreen(
  uiState: InsuranceEvidenceEmailInputState,
  navigateToShowCertificate: (url: String) -> Unit,
  navigateUp: () -> Unit,
  onRetry: () -> Unit,
  onChangeEmail: (email: String) -> Unit,
  onClearNavigation: () -> Unit,
  onSubmit: () -> Unit,
) {
  when (uiState) {
    InsuranceEvidenceEmailInputState.Failure -> {
      Box(Modifier.fillMaxSize()) {
        HedvigErrorSection(
          onButtonClick = onRetry,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }

    InsuranceEvidenceEmailInputState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is InsuranceEvidenceEmailInputState.Success -> {
      LaunchedEffect(uiState.fetchedCertificateUrl) {
        val url = uiState.fetchedCertificateUrl
        if (url != null) {
          onClearNavigation()
          navigateToShowCertificate(url)
        }
      }
      InsuranceEvidenceEmailSuccessScreen(
        uiState,
        onSubmit = onSubmit,
        navigateUp = navigateUp,
        onChangeEmail = onChangeEmail,
      )
    }
  }
}

@Composable
private fun InsuranceEvidenceEmailSuccessScreen(
  uiState: InsuranceEvidenceEmailInputState.Success,
  onSubmit: () -> Unit,
  navigateUp: () -> Unit,
  onChangeEmail: (email: String) -> Unit,
) {
  val explanationBottomSheetState = rememberHedvigBottomSheetState<Unit>()
  ExplanationBottomSheet(explanationBottomSheetState)
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = {
          explanationBottomSheetState.show(Unit)
        },
        content = {
          Icon(
            imageVector = HedvigIcons.InfoOutline,
            contentDescription = stringResource(R.string.TOAST_READ_MORE),
          )
        },
      )
    },
  ) {
    Box(Modifier.weight(1f)) {
      SuccessContent(
        uiState = uiState,
        onSubmit = onSubmit,
        onChangeEmail = onChangeEmail,
      )
      Column(
        Modifier.fillMaxWidth(),
      ) {
        HedvigTooltip(
          message = stringResource(R.string.TOAST_READ_MORE),
          showTooltip = true,
          beakDirection = TopEnd,
          tooltipShown = {},
          modifier = Modifier
            .padding(horizontal = 8.dp)
            .align(Alignment.End),
        )
      }
    }
  }
}

@Composable
private fun SuccessContent(
  uiState: InsuranceEvidenceEmailInputState.Success,
  onChangeEmail: (email: String) -> Unit,
  onSubmit: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var emailInput by remember {
    mutableStateOf(uiState.email ?: "")
  }
  Column(modifier) {
    Spacer(modifier = Modifier.height(8.dp))
    FlowHeading(
      stringResource(R.string.INSURANCE_EVIDENCE_DOCUMENT_TITLE),
      stringResource(R.string.CERTIFICATES_VERIFY_EMAIL),
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(24.dp))
    Spacer(Modifier.weight(1f))
    EmailTextField(
      email = emailInput,
      onEmailChanged = {
        emailInput = it
        onChangeEmail(it)
      },
      modifier = Modifier.padding(horizontal = 16.dp),
      errorText = uiState.emailValidationErrorMessage
        ?.let { stringResource(id = it) },
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      stringResource(id = R.string.CERTIFICATES_CREATE_CERTIFICATE),
      onClick = onSubmit,
      enabled = true,
      isLoading = uiState.buttonLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun EmailTextField(
  email: String,
  errorText: String?,
  onEmailChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    text = email,
    onValueChange = {
      onEmailChanged(it)
    },
    errorState = if (errorText != null) {
      ErrorState.Error.WithMessage(errorText)
    } else {
      ErrorState.NoError
    },
    textFieldSize = Medium,
    labelText = stringResource(R.string.PROFILE_MY_INFO_EMAIL_LABEL),
    modifier = modifier.fillMaxWidth(),
  )
}

@Composable
internal fun ExplanationBottomSheet(sheetState: HedvigBottomSheetState<Unit>) {
  HedvigBottomSheet(sheetState) { discount ->
    HedvigText(
      text = stringResource(id = R.string.INSURANCE_EVIDENCE_READ_MORE_TITLE),
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = stringResource(id = R.string.INSURANCE_EVIDENCE_READ_MORE_DESCRIPTION),
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      buttonSize = Large,
      onClick = { sheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceEvidenceEmailInputScreen(
  @PreviewParameter(InsuranceEvidenceEmailInputStateProvider::class) state: InsuranceEvidenceEmailInputState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InsuranceEvidenceEmailInputScreen(
        state,
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class InsuranceEvidenceEmailInputStateProvider :
  CollectionPreviewParameterProvider<InsuranceEvidenceEmailInputState>(
    listOf(
      InsuranceEvidenceEmailInputState.Success(
        email = "myemail@email.com",
      ),
      InsuranceEvidenceEmailInputState.Success(
        email = "myemail@email",
        emailValidationErrorMessage = R.string.PROFILE_MY_INFO_INVALID_EMAIL,
      ),
      InsuranceEvidenceEmailInputState.Success(
        email = "",
        emailValidationErrorMessage = R.string.travel_certificate_email_empty_error,
      ),
      InsuranceEvidenceEmailInputState.Failure,
      InsuranceEvidenceEmailInputState.Loading,
    ),
  )
