package com.hedvig.android.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.animation.CheckItemAnimation
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider

@Composable
fun SelectIndicationCircle(
  selected: Boolean,
  modifier: Modifier = Modifier,
  selectedIndicationColor: Color = LocalContentColor.current,
  unselectedCircleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
  CheckItemAnimation(selected = selected) { isSelected: Boolean ->
    Box(
      contentAlignment = Alignment.Center,
    ) {
      Spacer(
        modifier
          .size(24.dp)
          .clip(CircleShape)
          .then(
            if (isSelected) {
              Modifier.background(selectedIndicationColor)
            } else {
              Modifier.border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
            },
          ),
      )
      if (isSelected) {
        Spacer(
          modifier
            .size(8.dp)
            .clip(CircleShape)
            .then(Modifier.background(MaterialTheme.colorScheme.background)),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSelectIndicationCircle(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) selected: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      SelectIndicationCircle(selected, Modifier.padding(16.dp))
    }
  }
}
