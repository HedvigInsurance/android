package com.hedvig.android.design.system.hedvig.internal

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity

@Composable
internal fun <T : Any> rememberAnchorDraggableState(
  positionalThreshold: (Float) -> Float,
  velocityThreshold: () -> Float,
  snapAnimationSpec: TweenSpec<Float>,
  decayAnimationSpec: DecayAnimationSpec<Float>,
  initialValue: T,
): AnchoredDraggableState<T> {
  val density = LocalDensity.current
  return rememberSaveable(
    density,
    saver = AnchoredDraggableState.Saver(
      snapAnimationSpec = snapAnimationSpec,
      decayAnimationSpec = decayAnimationSpec,
      positionalThreshold = positionalThreshold,
      velocityThreshold = velocityThreshold,
    ),
  ) {
    AnchoredDraggableState(
      initialValue = initialValue,
      positionalThreshold = positionalThreshold,
      velocityThreshold = velocityThreshold,
      snapAnimationSpec = snapAnimationSpec,
      decayAnimationSpec = decayAnimationSpec,
    )
  }
}
