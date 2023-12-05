package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun AddCoInsuredBottomSheetContent(
  displayName: String,
  ssn: String?,
  birthDate: LocalDate?,
  errorMessage: String?,
  isLoading: Boolean,
  onSsnChanged: (String) -> Unit,
  onSave: () -> Unit,
  onFetchInfo: () -> Unit,
  onDismiss: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    Text(stringResource(id = R.string.CONTRACT_ADD_COINSURED))
    Spacer(Modifier.height(24.dp))
    HedvigTextField(
      value = ssn ?: "",
      label = {
        Text(stringResource(id = R.string.CONTRACT_PERSONAL_IDENTITY))
      },
      onValueChange = onSsnChanged,
      errorText = errorMessage,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done,
      ),
      keyboardActions = KeyboardActions(
        onDone = {
          onFetchInfo()
        },
      ),
      withNewDesign = true,
      modifier = Modifier.fillMaxWidth(),
    )
    AnimatedVisibility(
      visible = displayName?.isNotBlank() == true,
      modifier = Modifier.padding(top = 4.dp),
    ) {
      HedvigTextField(
        value = displayName,
        onValueChange = {},
        label = {
          Text(stringResource(id = R.string.FULL_NAME_TEXT))
        },
        enabled = false,
        withNewDesign = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = if (displayName.isNotBlank()) {
        stringResource(id = R.string.CONTRACT_ADD_COINSURED)
      } else {
        stringResource(id = R.string.CONTRACT_SSN_FETCH_INFO)
      },
      enabled = ssn != null,
      onClick = {
        if (displayName.isNotBlank()) {
          onSave()
        } else {
          onFetchInfo()
        }
      },
      isLoading = isLoading,
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      onClick = onDismiss,
      text = stringResource(id = R.string.general_cancel_button),
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
  }
}

@Composable
@HedvigPreview
private fun AddCoInsuredBottomSheetContentPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AddCoInsuredBottomSheetContent(
        onSave = {},
        onFetchInfo = {},
        onDismiss = {},
        isLoading = false,
        errorMessage = null,
        displayName = "Test Testersson",
        ssn = "1234",
        birthDate = null,
        onSsnChanged = {},
      )
    }
  }
}

@Composable
@HedvigPreview
private fun AddCoInsuredBottomSheetContentWithCoInsuredPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AddCoInsuredBottomSheetContent(
        onSave = {},
        onFetchInfo = {},
        onDismiss = {},
        isLoading = false,
        errorMessage = null,
        displayName = "Test Testersson",
        ssn = "1234",
        birthDate = null,
        onSsnChanged = {},
      )
    }
  }
}
