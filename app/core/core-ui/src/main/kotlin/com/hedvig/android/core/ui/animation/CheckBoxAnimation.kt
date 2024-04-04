package com.hedvig.android.core.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun CheckItemAnimation(selected: Boolean, content: @Composable (isSelected: Boolean) -> Unit) {
  val selectedTransition = updateTransition(selected)
  selectedTransition.AnimatedContent(
    transitionSpec = {
      fadeIn(tween(durationMillis = 200)) togetherWith fadeOut(tween(200))
    },
    contentAlignment = Alignment.Center,
  ) { isSelected ->
    content(isSelected)
  }
}
