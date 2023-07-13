package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircle

@Composable
fun HedvigContainedSmallButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
  ),
) {
  HedvigContainedSmallButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    contentPadding = contentPadding,
    colors = colors,
  ) {
    ButtonText(text)
  }
}

@Composable
private fun HedvigContainedSmallButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues,
  enabled: Boolean = true,
  colors: ButtonColors,
  content: @Composable RowScope.() -> Unit,
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    shape = MaterialTheme.shapes.squircle,
    contentPadding = contentPadding,
    colors = colors,
  ) {
    content()
  }
}

@Composable
private fun ButtonText(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    style = MaterialTheme.typography.bodyLarge,
    modifier = modifier,
  )
}
