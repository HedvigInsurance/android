package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun LargeContainedTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  LargeContainedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
  ) {
    Text(text = text)
  }
}

@Composable
fun LargeContainedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    backgroundColor = if (MaterialTheme.colors.isLight) {
      MaterialTheme.colors.primary
    } else {
      MaterialTheme.colors.secondary
    },
    contentColor = MaterialTheme.colors.onPrimary,
  ),
  content: @Composable RowScope.() -> Unit,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    contentPadding = PaddingValues(16.dp),
    colors = colors,
    content = content,
  )
}

@Preview(
  name = "Contained Button (Large)",
  group = "Buttons",
)
@Composable
fun LargeContainedButtonPreview() {
  HedvigTheme {
    LargeContainedTextButton(text = "Contained Button (Large)", onClick = {})
  }
}
