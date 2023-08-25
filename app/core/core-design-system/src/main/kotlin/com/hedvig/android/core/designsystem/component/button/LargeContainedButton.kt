package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.containedButtonContainer
import com.hedvig.android.core.designsystem.material3.onContainedButtonContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Deprecated("Use HedvigContainedTextButton instead")
@Composable
fun LargeContainedTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.containedButtonContainer,
    contentColor = MaterialTheme.colorScheme.onContainedButtonContainer,
    disabledContainerColor = MaterialTheme.colorScheme.containedButtonContainer.copy(
      alpha = 0.12f,
    ),
    disabledContentColor = MaterialTheme.colorScheme.onContainedButtonContainer.copy(
      alpha = 0.38f,
    ),
  ),
) {
  @Suppress("DEPRECATION")
  LargeContainedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
  ) {
    Text(text = text)
  }
}

@Deprecated("Use HedvigContainedButton instead")
@Composable
fun LargeContainedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.containedButtonContainer,
    contentColor = MaterialTheme.colorScheme.onContainedButtonContainer,
    disabledContainerColor = MaterialTheme.colorScheme.containedButtonContainer.copy(
      alpha = 0.12f,
    ),
    disabledContentColor = MaterialTheme.colorScheme.onContainedButtonContainer.copy(
      alpha = 0.38f,
    ),
  ),
  shape: Shape = MaterialTheme.shapes.large,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  content: @Composable RowScope.() -> Unit,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.fillMaxWidth(),
    shape = shape,
    contentPadding = contentPadding,
    colors = colors,
  ) {
    ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
      content()
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewLargeContainedButton() {
  HedvigTheme {
    @Suppress("DEPRECATION")
    LargeContainedTextButton(text = "Contained Button (Large)", onClick = {})
  }
}
