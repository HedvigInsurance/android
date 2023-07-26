package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material.ProvideTextStyle as ProvideTextStyleM2
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.ProvideTextStyle as ProvideTextStyleM3

@Composable
fun LargeOutlinedTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  LargeOutlinedButton(
    modifier = modifier,
    content = {
      Text(text = text)
    },
    onClick = onClick,
    enabled = enabled,
  )
}

@Composable
fun LargeOutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  content: @Composable RowScope.() -> Unit,
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = Material3Theme.shapes.large,
    contentPadding = PaddingValues(16.dp),
    enabled = enabled,
  ) {
    CompositionLocalProvider(
      LocalContentColor provides Material2Theme.colors.onBackground,
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
private fun PreviewLargeOutlinedButton() {
  HedvigTheme {
    LargeOutlinedTextButton(text = "Outlined Button (Large)", onClick = {})
  }
}
