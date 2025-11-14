package com.hedvig.feature.claim.chat.formmessage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun FormNumber(
  value: String?,
  id: String,
  title: String?,
  defaultValue: String?,
  suffix: String?,
  minValue: String?,
  maxValue: String?,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  FormContainer(modifier = modifier) {
    TextField(
      value = value ?: "",
      onValueChange = { newValue ->
        // Basic check to only allow digits and potentially one decimal point
        if (newValue.all { it.isDigit() || it == '.' }) {
          onValueChange(newValue)
        }
      },
      placeholder = { defaultValue?.let { Text(it) } },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      colors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
      ),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
