package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircleMedium

@Composable
fun HedvigContainedSmallButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
  ),
  elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
) {
  HedvigContainedSmallButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    elevation = elevation,
    contentPadding = contentPadding,
    colors = colors,
  ) {
    ButtonText(text)
  }
}

@Composable
private fun HedvigContainedSmallButton(
  onClick: () -> Unit,
  modifier: Modifier,
  enabled: Boolean,
  colors: ButtonColors,
  elevation: ButtonElevation?,
  contentPadding: PaddingValues,
  content: @Composable RowScope.() -> Unit,
) {
  Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = MaterialTheme.shapes.squircleMedium,
    colors = colors,
    elevation = elevation,
    contentPadding = contentPadding,
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
