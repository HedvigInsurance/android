package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepository
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepository.ReferralError
import hedvig.resources.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

@Composable
internal fun EditCodeBottomSheet(
  sheetState: HedvigBottomSheetState<String>,
  referralCodeUpdateError: ReferralError?,
  showedReferralCodeSubmissionError: () -> Unit,
  referralCodeSuccessfullyChanged: Boolean,
  onSubmitCode: (code: String) -> Unit,
  isLoading: Boolean,
) {
  DismissSheetOnSuccessfulCodeChangeEffect(sheetState, referralCodeSuccessfullyChanged)
  ClearErrorOnSheetDismissedEffect(sheetState, showedReferralCodeSubmissionError)
  HedvigBottomSheet(hedvigBottomSheetState = sheetState) { initialCode ->
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember {
      mutableStateOf(
        TextFieldValue(
          text = initialCode,
          selection = TextRange(initialCode.length),
        ),
      )
    }

    ClearErrorOnNewInputEffect({ textFieldValue }, showedReferralCodeSubmissionError)
    HedvigText(
      text = stringResource(R.string.referrals_change_change_code),
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().semantics { heading() },
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextField(
      textValue = textFieldValue,
      labelText = stringResource(R.string.referrals_empty_code_headline),
      onValueChange = { textFieldValue = it },
      textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
      errorState = if (referralCodeUpdateError == null) {
        HedvigTextFieldDefaults.ErrorState.NoError
      } else {
        HedvigTextFieldDefaults.ErrorState.Error.WithMessage(
          referralCodeUpdateError.toErrorMessage(),
        )
      },
      modifier = Modifier
        .focusRequester(focusRequester)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(R.string.general_save_button),
      onClick = {
        showedReferralCodeSubmissionError()
        focusManager.clearFocus()
        onSubmitCode(textFieldValue.text)
      },
      enabled = true,
      isLoading = isLoading,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.general_cancel_button),
      onClick = { sheetState.dismiss() },
      buttonSize = ButtonDefaults.ButtonSize.Large,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun DismissSheetOnSuccessfulCodeChangeEffect(
  sheetState: HedvigBottomSheetState<String>,
  referralCodeSuccessfullyChanged: Boolean,
) {
  val updatedReferralCodeSuccessfullyChanged by rememberUpdatedState(referralCodeSuccessfullyChanged)
  LaunchedEffect(sheetState) {
    snapshotFlow { updatedReferralCodeSuccessfullyChanged }
      .drop(1)
      .collect {
        if (it) {
          sheetState.dismiss()
        }
      }
  }
}

@Composable
private fun ClearErrorOnSheetDismissedEffect(
  sheetState: HedvigBottomSheetState<String>,
  showedReferralCodeSubmissionError: () -> Unit,
) {
  val updatedShowedReferralCodeSubmissionError by rememberUpdatedState(showedReferralCodeSubmissionError)
  LaunchedEffect(sheetState) {
    snapshotFlow { sheetState.isVisible }
      .drop(1)
      .collect {
        if (!it) {
          updatedShowedReferralCodeSubmissionError()
        }
      }
  }
}

@Composable
private fun ClearErrorOnNewInputEffect(
  textFieldValue: () -> TextFieldValue,
  showedReferralCodeSubmissionError: () -> Unit,
) {
  val updatedShowedReferralCodeSubmissionError by rememberUpdatedState(showedReferralCodeSubmissionError)
  LaunchedEffect(Unit) {
    snapshotFlow { textFieldValue() }
      .drop(1)
      .collectLatest {
        // clear error after the member edits the code manually
        updatedShowedReferralCodeSubmissionError()
      }
  }
}

@Composable
private fun ForeverRepository.ReferralError.toErrorMessage(): String {
  return message ?: stringResource(R.string.referrals_change_code_sheet_general_error)
}
