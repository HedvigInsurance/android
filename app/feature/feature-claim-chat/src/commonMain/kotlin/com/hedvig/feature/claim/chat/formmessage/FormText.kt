package com.hedvig.feature.claim.chat.formmessage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun FormText(
  value: String?,
  id: String,
  title: String?,
  defaultValue: String?,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  FormContainer(modifier = modifier) {
    TextField(
      value = value ?: "",
      onValueChange = onValueChange,
      supportingText = {
        Text(title ?: "")
      },
      placeholder = { defaultValue?.let { Text(it) } },
      singleLine = true,
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
