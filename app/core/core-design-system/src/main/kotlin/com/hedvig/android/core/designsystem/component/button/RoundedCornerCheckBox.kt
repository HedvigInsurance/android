package com.hedvig.android.core.designsystem.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircleExtraSmall
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.Checkmark

@Composable
fun RoundedCornerCheckBox(
  isChecked: Boolean,
  onCheckedChange: ((Boolean) -> Unit)?,
  checkMarkColor: Color = MaterialTheme.colorScheme.onPrimary,
  checkColor: Color = MaterialTheme.colorScheme.primary,
  uncheckedColor: Color = MaterialTheme.colorScheme.outlineVariant,
) {
  Box(
    modifier = Modifier
      .size(24.dp)
      .background(
        color = if (isChecked) checkColor else Color.Transparent,
        shape = MaterialTheme.shapes.squircleExtraSmall,
      )
      .border(
        width = 1.dp,
        color = if (isChecked) checkColor else uncheckedColor,
        shape = MaterialTheme.shapes.squircleExtraSmall,
      )
      .clip(MaterialTheme.shapes.squircleExtraSmall)
      .clickable {
        if (onCheckedChange != null) {
          onCheckedChange(isChecked)
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    if (isChecked) {
      Icon(Icons.Hedvig.Checkmark, contentDescription = null, tint = checkMarkColor)
    }
  }
}
