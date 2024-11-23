package com.hedvig.android.feature.odyssey.step.phonenumber

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberUiState.Status.ERROR
import com.hedvig.android.feature.odyssey.step.phonenumber.PhoneNumberUiState.Status.LOADING
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun PhoneNumberDestination(
  viewModel: PhoneNumberViewModel,
  windowSizeClass: WindowSizeClass,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState: PhoneNumberUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  PhoneNumberScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    updatePhoneNumber = viewModel::updatePhoneNumber,
    submitPhoneNumber = viewModel::submitPhoneNumber,
    showedError = viewModel::showedError,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun PhoneNumberScreen(
  uiState: PhoneNumberUiState,
  windowSizeClass: WindowSizeClass,
  updatePhoneNumber: (String) -> Unit,
  submitPhoneNumber: () -> Unit,
  showedError: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    errorSnackbarState = ErrorSnackbarState(
      error = uiState.status == ERROR,
      showedError = showedError,
    ),
    modifier = Modifier.clearFocusOnTap(),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    HedvigText(
      text = stringResource(R.string.CLAIMS_CONFIRM_NUMBER_TITLE),
      style = HedvigTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Spacer(Modifier.weight(1f))
    HedvigTextField(
      text = uiState.phoneNumber,
      onValueChange = updatePhoneNumber,
      textFieldSize = Medium,
      labelText = stringResource(R.string.ODYSSEY_PHONE_NUMBER_LABEL),
      enabled = uiState.status != LOADING,
      keyboardOptions = KeyboardOptions(
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Done,
      ),
      keyboardActions = KeyboardActions(
        onDone = { submitPhoneNumber() },
      ),
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.SAVE_AND_CONTINUE_BUTTON_LABEL),
      onClick = submitPhoneNumber,
      isLoading = uiState.status == LOADING,
      enabled = uiState.canSubmit,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewPhoneNumberScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PhoneNumberScreen(
        uiState = PhoneNumberUiState(
          phoneNumber = "24670",
          status = PhoneNumberUiState.Status.IDLE,
        ),
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        updatePhoneNumber = {},
        submitPhoneNumber = {},
        showedError = {},
        navigateUp = {},
        closeClaimFlow = {},
      )
    }
  }
}
