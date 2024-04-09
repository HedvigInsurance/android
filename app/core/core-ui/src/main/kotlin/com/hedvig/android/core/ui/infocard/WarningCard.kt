package com.hedvig.android.core.ui.infocard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled

@Composable
fun VectorWarningCard(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Hedvig.WarningFilled,
  iconColor: Color = MaterialTheme.colorScheme.warningElement,
  colors: CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.warningContainer,
    contentColor = MaterialTheme.colorScheme.onWarningContainer,
  ),
) {
  VectorInfoCard(
    text = text,
    modifier = modifier,
    icon = icon,
    iconColor = iconColor,
    colors = colors,
    underTextContent = null,
  )
}

@Composable
fun VectorWarningCard(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Hedvig.WarningFilled,
  iconColor: Color = MaterialTheme.colorScheme.warningElement,
  colors: CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.warningContainer,
    contentColor = MaterialTheme.colorScheme.onWarningContainer,
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
      VectorInfoCard("Lorem ipsum")
    }
  }
}
