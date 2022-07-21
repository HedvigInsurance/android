package com.hedvig.android.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.designsystem.theme.HedvigTheme

@Composable
fun LargeOutlinedTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LargeOutlinedButton(
    modifier = modifier,
    content = {
      Text(text = text)
    },
    onClick = onClick,
  )
}

@Composable
fun LargeOutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  backgroundColor: Color = Color.Transparent,
  content: @Composable RowScope.() -> Unit,
) {
  OutlinedButton(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .then(modifier),
    border = ButtonDefaults.outlinedBorder.copy(brush = SolidColor(MaterialTheme.colors.primary)),
    colors = ButtonDefaults.outlinedButtonColors(backgroundColor = backgroundColor),
    shape = MaterialTheme.shapes.large,
    contentPadding = PaddingValues(16.dp),
    content = content,
  )
}

@Preview(
  name = "Outlined Button (Large)",
  group = "Buttons",
  showBackground = true,
)
@Composable
fun LargeOutlinedButtonPreview() {
  HedvigTheme {
    LargeOutlinedTextButton(text = "Outlined Button (Large)", onClick = {})
  }
}
