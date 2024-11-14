package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepository
import hedvig.resources.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditCodeBottomSheet(
  isVisible: Boolean,
  code: String,
  referralCodeUpdateError: ForeverRepository.ReferralError?,
  showedReferralCodeSubmissionError: () -> Unit,
  onCodeChanged: (String) -> Unit,
  onDismiss: () -> Unit,
  onSubmitCode: () -> Unit,
  isLoading: Boolean,
) {


  HedvigBottomSheet(
    isVisible = isVisible,
    onVisibleChange = {
      onDismiss()
    },
  ) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    HedvigText(
      text = stringResource(R.string.referrals_change_change_code),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextField(
      text = code,
      labelText = stringResource(R.string.referrals_empty_code_headline),
      onValueChange = onCodeChanged,
      textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
      errorState = if (referralCodeUpdateError == null) {
        HedvigTextFieldDefaults.ErrorState.NoError
      } else {
        HedvigTextFieldDefaults.ErrorState.Error.WithMessage(
          referralCodeUpdateError.toErrorMessage(),
        )
      },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .focusRequester(focusRequester)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      text = stringResource(R.string.general_save_button),
      onClick = {
        showedReferralCodeSubmissionError()
        focusManager.clearFocus()
        onSubmitCode()
      },
      enabled = true,
      isLoading = isLoading,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.general_cancel_button),
      onClick = {
        onDismiss()
      },
      buttonSize = ButtonDefaults.ButtonSize.Large,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}


@Composable
private fun ForeverRepository.ReferralError.toErrorMessage(): String {
  return message ?: stringResource(R.string.referrals_change_code_sheet_general_error)
}
