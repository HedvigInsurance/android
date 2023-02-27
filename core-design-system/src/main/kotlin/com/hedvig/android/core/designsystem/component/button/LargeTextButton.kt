package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.MaterialTheme as Material2Theme
import androidx.compose.material.ProvideTextStyle as ProvideTextStyleM2
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.ProvideTextStyle as ProvideTextStyleM3

@Composable
fun LargeTextButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  TextButton(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    shape = Material3Theme.shapes.large,
    contentPadding = PaddingValues(16.dp),
  ) {
    CompositionLocalProvider(
      androidx.compose.material.LocalContentColor provides Material2Theme.colors.onBackground,
    ) {
      ProvideTextStyleM3(Material3Theme.typography.bodyLarge) {
        ProvideTextStyleM2(Material2Theme.typography.button) {
          content()
        }
      }
    }
  }
}
