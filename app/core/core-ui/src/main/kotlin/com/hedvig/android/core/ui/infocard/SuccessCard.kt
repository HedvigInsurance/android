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
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.Checkmark

@Composable
fun VectorSuccessCard(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Hedvig.Checkmark,
  iconColor: Color = MaterialTheme.colorScheme.typeElement,
  colors: CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.typeContainer,
    contentColor = MaterialTheme.colorScheme.onTypeContainer,
  ),
) {
  VectorSuccessCard(
    text = text,
    modifier = modifier,
    icon = icon,
    iconColor = iconColor,
    colors = colors,
    underTextContent = null,
  )
}

@Composable
fun VectorSuccessCard(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Hedvig.Checkmark,
  iconColor: Color = MaterialTheme.colorScheme.typeElement,
  colors: CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.typeContainer,
    contentColor = MaterialTheme.colorScheme.onTypeContainer,
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
private fun PreviewVectorSuccessCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      VectorSuccessCard("Lorem ipsum")
    }
  }
}
