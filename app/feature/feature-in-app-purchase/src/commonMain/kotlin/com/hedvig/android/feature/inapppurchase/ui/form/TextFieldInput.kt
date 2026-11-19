package com.hedvig.android.feature.inapppurchase.ui.form

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField
import kotlinx.coroutines.flow.drop

@Composable
internal fun TextFieldInput(
  field: InAppPurchaseFormField.Text,
  value: String?,
  errorMessage: String?,
  enabled: Boolean,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val textFieldState = rememberTextFieldState(value.orEmpty())
  LaunchedEffect(textFieldState) {
    snapshotFlow { textFieldState.text.toString() }
      .drop(1)
      .collect { onValueChange(it) }
  }
  HedvigTextField(
    state = textFieldState,
    labelText = field.title,
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    enabled = enabled,
    errorState = if (errorMessage != null) {
      HedvigTextFieldDefaults.ErrorState.Error.WithMessage(errorMessage)
    } else {
      HedvigTextFieldDefaults.ErrorState.NoError
    },
    modifier = modifier,
  )
}
