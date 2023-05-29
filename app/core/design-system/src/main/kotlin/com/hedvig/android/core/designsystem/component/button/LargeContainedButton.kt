package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.containedButtonContainer
import com.hedvig.android.core.designsystem.material3.onContainedButtonContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material.ProvideTextStyle as ProvideTextStyleM2
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.ProvideTextStyle as ProvideTextStyleM3

@Composable
fun LargeContainedTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = Material3Theme.colorScheme.containedButtonContainer,
    contentColor = Material3Theme.colorScheme.onContainedButtonContainer,
    disabledContainerColor = Material3Theme.colorScheme.containedButtonContainer.copy(
      alpha = 0.12f,
    ),
    disabledContentColor = Material3Theme.colorScheme.onContainedButtonContainer.copy(
      alpha = 0.38f,
    ),
  ),
) {
  LargeContainedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
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
    containerColor = Material3Theme.colorScheme.containedButtonContainer,
    contentColor = Material3Theme.colorScheme.onContainedButtonContainer,
    disabledContainerColor = Material3Theme.colorScheme.containedButtonContainer.copy(
      alpha = 0.12f,
    ),
    disabledContentColor = Material3Theme.colorScheme.onContainedButtonContainer.copy(
      alpha = 0.38f,
    ),
  ),
  shape: Shape = Material3Theme.shapes.large,
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
    CompositionLocalProvider(
      androidx.compose.material.LocalContentColor provides LocalContentColor.current,
    ) {
      ProvideTextStyleM3(Material3Theme.typography.bodyLarge) {
        ProvideTextStyleM2(Material2Theme.typography.button) {
          content()
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewLargeContainedButton() {
  HedvigTheme {
    LargeContainedTextButton(text = "Contained Button (Large)", onClick = {})
  }
}
