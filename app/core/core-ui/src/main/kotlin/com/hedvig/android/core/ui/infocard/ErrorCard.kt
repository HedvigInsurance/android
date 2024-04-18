package com.hedvig.android.core.ui.infocard

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled

@Composable
fun VectorErrorCard(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Hedvig.WarningFilled,
  iconColor: Color = MaterialTheme.colorScheme.error,
  colors: CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.errorContainer,
    contentColor = MaterialTheme.colorScheme.onErrorContainer,
  ),
) {
  VectorErrorCard(
    text = text,
    modifier = modifier,
    icon = icon,
    iconColor = iconColor,
    colors = colors,
    underTextContent = null,
  )
}

@Composable
fun VectorErrorCard(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Hedvig.WarningFilled,
  iconColor: Color = MaterialTheme.colorScheme.error,
  colors: CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.errorContainer,
    contentColor = MaterialTheme.colorScheme.onErrorContainer,
  ),
  underTextContent: @Composable (ColumnScope.() -> Unit)?,
) {
  VectorInfoCard(
    text = text,
    modifier = modifier,
    icon = icon,
    iconColor = iconColor,
    colors = colors,
    underTextContent = underTextContent,
  )
}

@HedvigPreview
@Composable
private fun PreviewVectorInfoCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      VectorErrorCard("Lorem ipsum")
    }
  }
}
