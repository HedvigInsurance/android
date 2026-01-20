package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <S> FadeAnimatedContent(
  targetState: S,
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.TopStart,
  label: String = "FadeAnimatedContent",
  contentKey: (targetState: S) -> Any? = { it },
  content: @Composable AnimatedVisibilityScope.(targetState: S) -> Unit,
) = AnimatedContent(
  targetState = targetState,
  modifier = modifier,
  transitionSpec = {
    fadeIn().togetherWith(fadeOut())
  },
  label = label,
  contentKey = contentKey,
  content = content,
  contentAlignment = contentAlignment,
)
