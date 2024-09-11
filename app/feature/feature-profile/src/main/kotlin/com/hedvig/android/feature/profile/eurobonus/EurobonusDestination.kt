package com.hedvig.android.feature.profile.eurobonus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnTap

@Composable
internal fun EurobonusDestination(viewModel: EurobonusViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.isEligibleForEurobonus) {
    if (uiState.isEligibleForEurobonus == false) {
      navigateUp()
    }
  }
  val focusManager = LocalFocusManager.current
  EurobonusScreen(
    setEurobonusNumber = { viewModel.emit(EurobonusEvent.UpdateEurobonusValue(it)) },
    uiState = uiState,
    onSave = {
      focusManager.clearFocus()
      viewModel.emit(EurobonusEvent.SubmitEditedEurobonus)
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun EurobonusScreen(
  uiState: EurobonusUiState,
  setEurobonusNumber: (String) -> Unit,
  onSave: () -> Unit,
  navigateUp: () -> Unit,
) {
  Box(
    propagateMinConstraints = true,
    modifier = Modifier.fillMaxSize(),
  ) {
    if (uiState.isLoading) {
      HedvigFullScreenCenterAlignedProgressDebounced()
    } else {
      HedvigScaffold(
        topAppBarText = stringResource(hedvig.resources.R.string.sas_integration_title),
        navigateUp = navigateUp,
        modifier = Modifier.clearFocusOnTap(),
      ) {
        Box(
          contentAlignment = Alignment.BottomStart,
          modifier = Modifier
              .heightIn(80.dp)
              .fillMaxWidth(),
        ) {
          HedvigText(
            text = stringResource(hedvig.resources.R.string.sas_integration_connect_your_eurobonus),
            style = HedvigTheme.typography.headlineMedium, //todo: font size here??? no 22.sp in the nbew DS
            modifier = Modifier.padding(16.dp),
          )
        }
        EurobonusNumberField(
          canSubmit = uiState.canSubmit,
          canEditText = uiState.canEditText,
          hasError = uiState.hasError ?: false,
          number = uiState.eurobonusNumber,
          onSubmitEurobonus = onSave,
          setEurobonusText = setEurobonusNumber,
        )
        Spacer(Modifier.height(16.dp))
        HedvigText(
          text = stringResource(hedvig.resources.R.string.sas_integration_info),
          style = HedvigTheme.typography.bodySmall, //todo: font size here??? 14.sp is for label?
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(hedvig.resources.R.string.general_save_button),
          enabled = uiState.canSubmit,
          onClick = { onSave() },
          buttonStyle = ButtonDefaults.ButtonStyle.Primary,
          buttonSize = ButtonDefaults.ButtonSize.Large,
          modifier = Modifier.padding(horizontal = 16.dp),
          isLoading = uiState.isSubmitting,
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun EurobonusNumberField(
  number: String,
  hasError: Boolean,
  canEditText: Boolean,
  canSubmit: Boolean,
  setEurobonusText: (String) -> Unit,
  onSubmitEurobonus: () -> Unit,
) {
  var numberValue by remember {
    mutableStateOf(number)
  }
  HedvigTextField(
    text = numberValue,
    onValueChange = { newInput ->
      if (newInput.indices.all { newInput[it].isWhitespace().not() }) {
        numberValue = newInput
        setEurobonusText(newInput)
      }
    },
    enabled = canEditText,
    labelText = buildString {
      append(stringResource(hedvig.resources.R.string.sas_integration_title))
      append(" ")
      append(stringResource(hedvig.resources.R.string.sas_integration_number))
    },
    errorState =
    if (hasError) {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(stringResource(hedvig.resources.R.string.something_went_wrong))
    } else {
      HedvigTextFieldDefaults.ErrorState.NoError
      // todo: there was a supporting text
           // Text(stringResource(hedvig.resources.R.string.sas_integration_number_placeholder))
      // here, not sure what to do with that, can't find it in figma?
    },
    keyboardOptions = KeyboardOptions(
      capitalization = KeyboardCapitalization.Characters,
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        if (canSubmit) {
          onSubmitEurobonus()
        }
      },
    ),
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,//todo: what textField size here??
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewEurobonusScreen(
  @PreviewParameter(
    com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider::class,
  ) hasError: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      EurobonusScreen(
        EurobonusUiState(
          eurobonusNumber = "ABC-123",
          canSubmit = true,
          isLoading = true,
          hasError = hasError,
        ),
        {},
        {},
        {},
      )
    }
  }
}
