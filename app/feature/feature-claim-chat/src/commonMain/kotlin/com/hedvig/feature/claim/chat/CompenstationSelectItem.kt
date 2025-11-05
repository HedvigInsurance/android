package com.hedvig.feature.claim.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


data class CompensationOption(
  val id: String,
  val title: String,
  val subtitle: String,
  val icon: Any? = null
)

@Composable
fun CompensationSelectionItem(
  option: CompensationOption,
  isSelected: Boolean,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp)
      .clickable { onSelect(option.id) },
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.size(36.dp).background(Color.LightGray, RoundedCornerShape(8.dp))) {
      Text(option.id.first().toString(), Modifier.align(Alignment.Center))
    }

    Spacer(Modifier.width(16.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = option.title,
        style = MaterialTheme.typography.titleMedium
      )
      Text(
        text = option.subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    RadioButton(
      selected = isSelected,
      onClick = { onSelect(option.id) }
    )
  }
}
