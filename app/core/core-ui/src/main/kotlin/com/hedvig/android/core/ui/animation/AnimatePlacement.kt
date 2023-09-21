package com.hedvig.android.core.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kotlinx.coroutines.launch

/**
 * If the composable was laid out somewhere before and they get a new position, it keeps the old position initially and
 * starts an animation towards the new position with [animationSpec].
 */
fun Modifier.animatePlacement(
  animationSpec: AnimationSpec<IntOffset> = spring(stiffness = Spring.StiffnessMediumLow),
): Modifier = composed {
  val scope = rememberCoroutineScope()
  var targetOffset by remember { mutableStateOf(IntOffset.Zero) }
  var animatable by remember {
    mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null)
  }
  this
    .onPlaced {
      // Calculate the position in the parent layout
      targetOffset = it.positionInParent().round()
    }
    .offset {
      // Animate to the new target offset when alignment changes.
      val anim = animatable ?: Animatable(targetOffset, IntOffset.VectorConverter)
        .also { animatable = it }
      if (anim.targetValue != targetOffset) {
        scope.launch {
          anim.animateTo(targetOffset, animationSpec)
        }
      }
      // Offset the child in the opposite direction to the targetOffset, and slowly catch
      // up to zero offset via an animation to achieve an overall animated movement.
      animatable?.let { it.value - targetOffset } ?: IntOffset.Zero
    }
}
