package com.hedvig.android.feature.home.claimdetail.ui

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
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun Pill(
  text: String,
  color: Color,
  contentColor: Color = contentColorFor(color),
) {
  Surface(
    shape = MaterialTheme.shapes.squircleExtraSmall,
    color = color,
    contentColor = contentColor,
    modifier = Modifier.heightIn(min = 24.dp),
  ) {
    Row(
      Modifier.padding(
        horizontal = 10.dp,
        vertical = 4.dp,
      ),
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

@HedvigPreview
@Composable
private fun PreviewPill() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Pill("PillText", MaterialTheme.colorScheme.primary)
    }
  }
}
