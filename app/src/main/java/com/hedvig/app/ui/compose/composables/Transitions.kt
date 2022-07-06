package com.hedvig.app.ui.compose.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable

@Composable
fun FadeWhen(
  visible: Boolean,
  content: @Composable (AnimatedVisibilityScope.() -> Unit),
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(),
    exit = fadeOut(),
    content = content,
  )
}
