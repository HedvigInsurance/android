package com.hedvig.android.feature.forever.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import hedvig.resources.R

@Composable
internal fun EditCodeBottomSheet(
  code: TextFieldValue,
  errorText: String?,
  onCodeChanged: (TextFieldValue) -> Unit,
  onDismiss: () -> Unit,
  onSubmitCode: () -> Unit,
  isLoading: Boolean,
  sheetState: SheetState,
  focusRequester: FocusRequester,
) {
  ModalBottomSheet(
    containerColor = MaterialTheme.colorScheme.background,
    onDismissRequest = { onDismiss() },
    shape = MaterialTheme.shapes.squircleLargeTop,
    sheetState = sheetState,
    tonalElevation = 0.dp,
  ) {
    Text(
      text = stringResource(id = R.string.referrals_change_change_code),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextField(
      value = code,
      label = {
        Text(stringResource(id = R.string.referrals_empty_code_headline))
      },
      onValueChange = onCodeChanged,
      errorText = errorText,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .focusRequester(focusRequester)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigContainedButton(
      text = stringResource(id = R.string.general_save_button),
      onClick = { onSubmitCode() },
      isLoading = isLoading,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_cancel_button),
      onClick = { onDismiss() },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}
