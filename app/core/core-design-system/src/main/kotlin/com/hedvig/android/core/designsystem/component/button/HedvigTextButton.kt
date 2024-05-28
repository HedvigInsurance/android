package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircleMedium

@Composable
fun HedvigTextButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.textButtonColors(),
) {
  TextButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.squircleMedium,
    contentPadding = contentPadding,
    colors = colors,
  ) {
    ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
      Text(
        text = text,
        textAlign = TextAlign.Center,
      )
    }
  }
}
