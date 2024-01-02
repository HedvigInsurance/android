package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard

@Composable
fun ClickableOption(
  text: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  cardColors: CardColors = CardDefaults.outlinedCardColors(),
) {
  HedvigCard(
    onClick = onClick,
    colors = cardColors,
  ) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .heightIn(56.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
      )
      Icon(imageVector = icon, contentDescription = "library icon")
    }
  }
}

@Composable
fun ClickableOption(text: String, onClick: () -> Unit, cardColors: CardColors = CardDefaults.outlinedCardColors()) {
  HedvigCard(
    onClick = onClick,
    colors = cardColors,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .heightIn(56.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
      )
    }
  }
}
