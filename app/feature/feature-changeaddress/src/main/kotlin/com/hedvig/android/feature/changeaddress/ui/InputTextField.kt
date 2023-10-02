package com.hedvig.android.feature.changeaddress.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.ui.ValidatedInput

@Composable
fun InputTextField(
  value: ValidatedInput<String?>,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  placeholder: String? = null,
  trailingText: String? = null,
) {
  HedvigTextField(
    value = value.input ?: "",
    onValueChange = { onValueChange(it) },
    errorText = value.errorMessageRes?.let { stringResource(it) },
    label = { Text(label) },
    placeholder = if (placeholder != null) {
      { Text(placeholder) }
    } else {
      null
    },
    trailingIcon = if (trailingText != null) {
      { Text(trailingText) }
    } else {
      null
    },
    keyboardOptions = keyboardOptions,
    withNewDesign = true,
    modifier = modifier.fillMaxWidth(),
  )
}
