package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.shared.foreverui.ui.data.ForeverRepository
import hedvig.resources.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditCodeBottomSheet(
  sheetState: SheetState,
  code: TextFieldValue,
  referralCodeUpdateError: ForeverRepository.ReferralError?,
  showedReferralCodeSubmissionError: () -> Unit,
  onCodeChanged: (TextFieldValue) -> Unit,
  onDismiss: () -> Unit,
  onSubmitCode: () -> Unit,
  isLoading: Boolean,
) {
  // No idea why material3.ModalBottomSheet does not read the latest values here, but this fixes it
  // https://issuetracker.google.com/issues/300280211
  val updatedReferralCodeUpdateError by rememberUpdatedState(referralCodeUpdateError)
  val updatedIsLoading by rememberUpdatedState(isLoading)
  ModalBottomSheet(
    containerColor = MaterialTheme.colorScheme.background,
    onDismissRequest = onDismiss,
    shape = MaterialTheme.shapes.squircleLargeTop,
    sheetState = sheetState,
    tonalElevation = 0.dp,
  ) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    Text(
      text = stringResource(R.string.referrals_change_change_code),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextField(
      value = code,
      label = { Text(stringResource(R.string.referrals_empty_code_headline)) },
      onValueChange = onCodeChanged,
      errorText = updatedReferralCodeUpdateError?.toErrorMessage(),
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .focusRequester(focusRequester)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigContainedButton(
      text = stringResource(R.string.general_save_button),
      onClick = {
        showedReferralCodeSubmissionError()
        onSubmitCode()
      },
      isLoading = updatedIsLoading,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.general_cancel_button),
      onClick = onDismiss,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))

    val imeVisibleState = rememberUpdatedState(WindowInsets.isImeVisible)
    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
      snapshotFlow { imeVisibleState.value }
        .drop(1)
        .filter { !it }
        .collectLatest {
          focusManager.clearFocus()
        }
    }
  }
}

@Composable
private fun ForeverRepository.ReferralError.toErrorMessage(): String {
  return message ?: stringResource(R.string.referrals_change_code_sheet_general_error)
}
