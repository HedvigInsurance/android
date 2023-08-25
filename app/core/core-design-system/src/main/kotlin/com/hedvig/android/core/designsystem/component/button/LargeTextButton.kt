package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Deprecated("Use HedvigTextButton instead")
@Composable
fun LargeTextButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = MaterialTheme.shapes.large,
  content: @Composable RowScope.() -> Unit,
) {
  TextButton(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    enabled = enabled,
    shape = shape,
    contentPadding = PaddingValues(16.dp),
  ) {
    ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
      content()
    }
  }
}
