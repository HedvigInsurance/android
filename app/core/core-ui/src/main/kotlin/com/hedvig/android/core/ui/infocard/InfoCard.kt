package com.hedvig.android.core.ui.infocard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled

@Composable
fun VectorInfoCard(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Hedvig.InfoFilled,
  iconColor: Color = MaterialTheme.colorScheme.infoElement,
  colors: CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.infoContainer,
    contentColor = MaterialTheme.colorScheme.onInfoContainer,
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
fun VectorInfoCard(
  text: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Hedvig.InfoFilled,
  iconColor: Color = MaterialTheme.colorScheme.infoElement,
  colors: CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.infoContainer,
    contentColor = MaterialTheme.colorScheme.onInfoContainer,
  ),
  underTextContent: @Composable (ColumnScope.() -> Unit)?,
) {
  HedvigInfoCard(
    modifier = modifier,
    contentPadding = PaddingValues(
      start = 12.dp,
      top = 12.dp,
      end = 16.dp,
      bottom = 12.dp,
    ),
    colors = colors,
  ) {
    LayoutWithoutPlacement(
      sizeAdjustingContent = {
        Text(
          text = "H",
          style = MaterialTheme.typography.bodyMedium,
        )
      },
    ) {
      Icon(
        imageVector = icon,
        contentDescription = "info",
        modifier = Modifier.size(16.dp),
        tint = iconColor,
      )
    }
    Spacer(Modifier.width(8.dp))
    Column {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
      )
      if (underTextContent != null) {
        Spacer(Modifier.height(12.dp))
        underTextContent()
        Spacer(Modifier.height(4.dp))
      }
    }
  }
}

@HedvigPreview
@PreviewFontScale
@Composable
private fun PreviewVectorInfoCard() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      VectorInfoCard("Lorem ipsum")
    }
  }
}
