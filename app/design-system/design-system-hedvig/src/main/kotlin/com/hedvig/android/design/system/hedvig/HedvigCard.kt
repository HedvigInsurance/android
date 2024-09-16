package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun HedvigCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
  val modifierWithOnClick = if (onClick != null) {
    modifier
      .clip(HedvigTheme.shapes.cornerLarge)
      .clickable(onClick = onClick)
  } else {
    modifier.clip(HedvigTheme.shapes.cornerLarge)
  }

  Surface(
    modifier = modifierWithOnClick,
  ) {
    content()
  }
}
