package com.hedvig.feature.claim.chat.formmessage

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun FormDate(
  value: String?,
  id: String,
  title: String?,
  onDateSelected: (Long) -> Unit,
  modifier: Modifier = Modifier,
  // Expect/Actual function to show native date picker (defined outside commonMain)
  showDatePicker: (onDateSelected: (Long) -> Unit) -> Unit,
) {
  FormContainer(modifier = modifier) {
    OutlinedButton(
      onClick = { showDatePicker(onDateSelected) },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(value ?: "Select Date")
    }
  }
}
