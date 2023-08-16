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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.design.system.R
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onInfoContainer
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
    contentPadding = PaddingValues(12.dp),
    colors = colors,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = "info",
      modifier = Modifier
        .padding(top = 2.dp)
        .size(16.dp)
        .padding(1.dp),
      tint = iconColor,
    )
    Spacer(Modifier.width(8.dp))
    Column {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
      )
      if (underTextContent != null) {
        Spacer(Modifier.height(8.dp))
        underTextContent()
      }
    }
  }
}

@Composable
fun DrawableInfoCard(
  modifier: Modifier = Modifier,
  title: String?,
  text: String,
  icon: Painter = painterResource(id = R.drawable.ic_checkmark_success),
  iconColor: Color = MaterialTheme.colorScheme.infoElement,
  colors: CardColors = CardDefaults.outlinedCardColors(
    containerColor = MaterialTheme.colorScheme.infoContainer,
    contentColor = MaterialTheme.colorScheme.onInfoContainer,
  ),
) {
  HedvigInfoCard(
    modifier = modifier,
    contentPadding = PaddingValues(12.dp),
    colors = colors,
  ) {
    Icon(
      painter = icon,
      contentDescription = "info",
      modifier = Modifier
        .padding(top = 2.dp)
        .size(16.dp)
        .padding(1.dp),
      tint = iconColor,
    )
    Spacer(modifier = Modifier.padding(start = 8.dp))
    Column {
      if (title != null) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.padding(2.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary,
      )
    }
  }
}
