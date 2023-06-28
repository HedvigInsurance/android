package com.hedvig.android.core.designsystem.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextFieldDefaults
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextFieldTokens
import com.hedvig.android.core.designsystem.material3.toColor
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

/**
 * The card which looks like a TextField, but functions as a button which has the text in the positions of a button.
 * https://www.figma.com/file/qUhLjrKl98PAzHov9ilaDH/New-Web-UI-Kit?type=design&node-id=114-2605&t=NMbwHBp5OhuKjgZ4-4
 */
@Composable
fun HedvigCardButton(
  onClick: () -> Unit,
  hintText: String,
  inputText: String?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  HedvigTextField(
    value = inputText ?: "",
    label = {
      Text(text = hintText)
    },
    onValueChange = {},
    enabled = enabled,
    readOnly = true,
    colors = HedvigTextFieldDefaults.colors(typingContainerColor = HedvigTextFieldTokens.TypeContainerColor.toColor()),
    modifier = modifier.clickable(enabled = enabled) { onClick() }, // todo check that clickability looks good
  )
}

@HedvigPreview
@Composable
private fun PreviewHedvigCardButtonWithInput() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      Box(Modifier.padding(16.dp)) {
        HedvigCardButton(
          onClick = {},
          hintText = "Hint",
          inputText = "Input text",
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigCardButton() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      Box(Modifier.padding(16.dp)) {
        HedvigCardButton(
          onClick = {},
          hintText = "Hint",
          inputText = null,
        )
      }
    }
  }
}
