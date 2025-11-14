package com.hedvig.feature.claim.chat.formmessage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FormSingleSelect(
  value: String?,
  id: String,
  title: String?,
  defaultValue: String?,
  options: List<String>,
  onOptionSelected: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  FormContainer(modifier = modifier) {
    Column {
      Text("Choose one:", style = MaterialTheme.typography.labelMedium)
      Spacer(Modifier.height(4.dp))

      options.forEach { option ->
        val isSelected = option == value
        Text(
          text = option,
          color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
          modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .clickable { onOptionSelected(option) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        )
        Spacer(Modifier.height(4.dp))
      }
    }
  }
}
