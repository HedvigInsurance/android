package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun HedvigCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
  Surface(
    modifier = modifier
      .clip(HedvigTheme.shapes.cornerXLarge)
      .then(
        if (onClick != null) {
          Modifier.clickable(onClick = onClick)
        } else {
          Modifier
        },
      ),
  ) {
    content()
  }
}
