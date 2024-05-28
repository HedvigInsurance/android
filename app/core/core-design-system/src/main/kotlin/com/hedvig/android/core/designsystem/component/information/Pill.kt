package com.hedvig.android.core.designsystem.component.information

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircleExtraSmall

@Composable
fun HedvigPill(
  text: String,
  color: Color,
  modifier: Modifier = Modifier,
  contentColor: Color = contentColorFor(color),
) {
  Surface(
    shape = MaterialTheme.shapes.squircleExtraSmall,
    color = color,
    contentColor = contentColor,
    modifier = modifier.heightIn(min = 24.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
      )
    }
  }
}
