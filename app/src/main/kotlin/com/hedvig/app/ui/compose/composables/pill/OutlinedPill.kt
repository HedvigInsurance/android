package com.hedvig.app.ui.compose.composables.pill

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvigContentColorFor

@Composable
fun OutlinedPill(
  text: String,
) {
  val backgroundColor = Color.Transparent
  Surface(
    shape = MaterialTheme.shapes.small,
    color = backgroundColor,
    contentColor = hedvigContentColorFor(backgroundColor),
    border = BorderStroke(
      width = ButtonDefaults.OutlinedBorderSize,
      color = MaterialTheme.colors.onSurface,
    ),
    modifier = Modifier.heightIn(min = 24.dp),
  ) {
    Row(
      Modifier.padding(
        horizontal = 8.dp,
        vertical = 5.dp,
      ),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text,
        style = MaterialTheme.typography.caption,
        maxLines = 1,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOutlinedPill() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      OutlinedPill("PillText")
    }
  }
}
