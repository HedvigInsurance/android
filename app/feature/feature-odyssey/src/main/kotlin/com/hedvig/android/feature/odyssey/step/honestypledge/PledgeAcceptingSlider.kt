package com.hedvig.android.feature.odyssey.step.honestypledge

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronRight
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private val circleDiameter: Dp = 62.dp

@Composable
internal fun PledgeAcceptingSlider(onAccepted: () -> Unit, text: String, modifier: Modifier = Modifier) {
  val offsetX: Animatable<Float, AnimationVector1D> = remember { Animatable(0f) }
  Box(
    modifier
      .requiredHeight(circleDiameter)
      .clip(CircleShape)
      .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
      .animatingSliderModifier(offsetX, LocalDensity.current, onAccepted),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurfaceVariant),
      modifier = Modifier
        .align(Alignment.Center)
        .graphicsLayer {
          val offset = offsetX.value
          val pointWhereTextShouldBeInvisible = (circleDiameter * 2).toPx()
          val ratioToInvisiblePoint = (offset / pointWhereTextShouldBeInvisible).coerceIn(0f, 1f)
          alpha = 1 - ratioToInvisiblePoint
        },
    )
    Box(
      Modifier
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .padding(4.dp)
        .size(circleDiameter - 8.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary),
    ) {
      Icon(
        imageVector = Icons.Hedvig.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.align(Alignment.Center),
      )
    }
  }
}

/**
 * Detects drag interactions in the component, and reports the necessary offest that needs to be se into the [offsetX]
 * [Animatable]. When the gesture goes all the way to the end, [onAccepted] is called.
 */
@SuppressLint("ReturnFromAwaitPointerEventScope", "MultipleAwaitPointerEventScopes")
private fun Modifier.animatingSliderModifier(
  offsetX: Animatable<Float, AnimationVector1D>,
  density: Density,
  onAccepted: () -> Unit,
): Modifier {
  return this
    .onPlaced {
      offsetX.updateBounds(
        lowerBound = 0f,
        upperBound = it.size.width - with(density) { circleDiameter.toPx() },
      )
    }
    .pointerInput(Unit) {
      val decay = splineBasedDecay<Float>(this)
      val halfCircleSize = (circleDiameter / 2).roundToPx()
      val getEndPoint = { this.size.width - circleDiameter.toPx() }
      val hasOffsetSlidedToTheEnd = {
        offsetX.value >= getEndPoint()
      }
      coroutineScope {
        while (isActive) {
          val velocityTracker = VelocityTracker()

          val firstDownPointer = awaitPointerEventScope { awaitFirstDown() }
          offsetX.stop()
          offsetX.animateTo(
            targetValue = firstDownPointer.position.x - halfCircleSize,
            animationSpec = spring(stiffness = Spring.StiffnessHigh * 10),
          )
          if (hasOffsetSlidedToTheEnd()) {
            onAccepted()
            return@coroutineScope
          }
          awaitPointerEventScope {
            horizontalDrag(firstDownPointer.id) { change: PointerInputChange ->
              val horizontalDragOffset = change.position.x - halfCircleSize
              launch(Dispatchers.Unconfined) {
                offsetX.snapTo(horizontalDragOffset)
              }
              if (hasOffsetSlidedToTheEnd()) {
                onAccepted()
                this@coroutineScope.cancel()
              }
              velocityTracker.addPosition(change.uptimeMillis, change.position)
              if (change.positionChange() != Offset.Zero) {
                change.consume()
              }
            }
          }
          val velocity: Float = velocityTracker.calculateVelocity().x
          val targetOffsetXAfterFlingEnd = decay.calculateTargetValue(offsetX.value, velocity)
          launch {
            if (targetOffsetXAfterFlingEnd <= getEndPoint()) {
              // Not enough velocity; Slide back to the default position.
              offsetX.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                  dampingRatio = Spring.DampingRatioMediumBouncy,
                  stiffness = Spring.StiffnessVeryLow,
                ),
                initialVelocity = velocity,
              )
            } else {
              // Enough velocity to finish the slide
              offsetX.animateDecay(velocity, decay)
              onAccepted()
            }
          }
        }
      }
    }
}

@HedvigPreview
@Composable
private fun PreviewPledgeAcceptingSlider() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PledgeAcceptingSlider(
        {},
        "Slide to start",
      )
    }
  }
}
