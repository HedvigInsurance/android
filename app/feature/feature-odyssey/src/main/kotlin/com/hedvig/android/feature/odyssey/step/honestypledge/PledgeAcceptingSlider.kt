package com.hedvig.android.feature.odyssey.step.honestypledge

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.ChevronRight
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.odyssey.step.honestypledge.PledgeAcceptingSliderPosition.Accepted
import com.hedvig.android.feature.odyssey.step.honestypledge.PledgeAcceptingSliderPosition.Resting
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val circleDiameter: Dp = 62.dp

private enum class PledgeAcceptingSliderPosition {
  Resting,
  Accepted,
}

private interface PledgeAcceptingSliderState {
  val xOffset: Float
  val isInAcceptedPosition: Boolean

  fun Modifier.thumbDraggableModifier(): Modifier

  fun updateAnchors(layoutSize: IntSize)

  @Composable
  fun ReportAcceptedEffect(onAccepted: () -> Unit)
}

private class PledgeAcceptingSliderStateImpl(
  val coroutineScope: CoroutineScope,
  val circleDiameterPx: Float,
) : PledgeAcceptingSliderState {
  private var onDragStoppedJob: Job? = null
  private var anchors by mutableStateOf(DraggableAnchors<PledgeAcceptingSliderPosition> {})
  override var xOffset: Float by mutableFloatStateOf(0f)
  private val draggableState = DraggableState { delta ->
    if (isInAcceptedPosition) return@DraggableState
    cancelOnDragStoppedJob()
    xOffset = (xOffset + delta).coerceIn(anchors.minPosition(), anchors.maxPosition())
  }
  override val isInAcceptedPosition: Boolean by derivedStateOf {
    xOffset >= anchors.positionOf(PledgeAcceptingSliderPosition.Accepted)
  }

  override fun updateAnchors(layoutSize: IntSize) {
    anchors = DraggableAnchors {
      Resting at 0f
      Accepted at layoutSize.width - circleDiameterPx
    }
  }

  override fun Modifier.thumbDraggableModifier(): Modifier {
    return this.draggable(
      state = draggableState,
      orientation = Horizontal,
      startDragImmediately = true,
      onDragStopped = { velocity ->
        onDragStopped(velocity)
      },
    )
  }

  @Composable
  override fun ReportAcceptedEffect(onAccepted: () -> Unit) {
    val updatedOnAccepted by rememberUpdatedState(onAccepted)
    LaunchedEffect(this) {
      snapshotFlow { isInAcceptedPosition }.collect { isInAcceptedPosition ->
        if (isInAcceptedPosition) {
          delay(1.seconds)
          updatedOnAccepted()
          delay(1.seconds)
          resetState()
        }
      }
    }
  }

  private fun onDragStopped(velocity: Float) {
    if (isInAcceptedPosition) return
    val sliderAlmostAtEnd = xOffset >= anchors.positionOf(PledgeAcceptingSliderPosition.Accepted) - circleDiameterPx
    val closestAnchor = anchors.positionOf(
      if (sliderAlmostAtEnd) {
        PledgeAcceptingSliderPosition.Accepted
      } else {
        PledgeAcceptingSliderPosition.Resting
      },
    )
    cancelOnDragStoppedJob()
    onDragStoppedJob = coroutineScope.launch {
      animate(
        initialValue = xOffset,
        targetValue = closestAnchor,
        initialVelocity = velocity,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioNoBouncy,
          stiffness = if (sliderAlmostAtEnd) Spring.StiffnessMedium else Spring.StiffnessVeryLow,
          visibilityThreshold = 2f,
        ),
      ) { value, _ ->
        xOffset = value.coerceIn(anchors.minPosition(), anchors.maxPosition())
        if (isInAcceptedPosition) cancelOnDragStoppedJob()
      }
    }
  }

  private fun resetState() {
    cancelOnDragStoppedJob()
    xOffset = 0f
  }

  private fun cancelOnDragStoppedJob() {
    onDragStoppedJob?.cancel()
    onDragStoppedJob = null
  }
}

@Composable
private fun rememberPledgeAcceptingSliderState(
  circleDiameterPx: Float,
  onAccepted: () -> Unit,
): PledgeAcceptingSliderState {
  val coroutineScope = rememberCoroutineScope()
  val state = remember(coroutineScope, circleDiameterPx) {
    PledgeAcceptingSliderStateImpl(coroutineScope, circleDiameterPx)
  }
  state.ReportAcceptedEffect(onAccepted)
  return state
}

@Composable
internal fun PledgeAcceptingSlider(onAccepted: () -> Unit, text: String, modifier: Modifier = Modifier) {
  val circleDiameterPx = with(LocalDensity.current) { circleDiameter.toPx() }
  val state = rememberPledgeAcceptingSliderState(circleDiameterPx, onAccepted)
  val isAcceptedTransition = updateTransition(state.isInAcceptedPosition)
  val boxColor by isAcceptedTransition.animateColor { isAccepted ->
    if (isAccepted) {
      HedvigTheme.colorScheme.highlightGreenFill3
    } else {
      HedvigTheme.colorScheme.fillPrimary
    }
  }
  Box(
    modifier
      .requiredHeight(circleDiameter)
      .clip(CircleShape)
      .background(HedvigTheme.colorScheme.borderSecondary, CircleShape)
      .onSizeChanged(state::updateAnchors),
  ) {
    HedvigText(
      text = text,
      color = HedvigTheme.colorScheme.textTertiary,
      modifier = Modifier
        .align(Alignment.Center)
        .graphicsLayer {
          val pointWhereTextShouldBeInvisible = (circleDiameter * 2).toPx()
          val ratioToInvisiblePoint = (state.xOffset / pointWhereTextShouldBeInvisible).coerceIn(0f, 1f)
          alpha = 1 - ratioToInvisiblePoint
        },
    )
    Box(
      Modifier
        .offset { IntOffset(state.xOffset.roundToInt(), 0) }
        .then(with(state) { Modifier.thumbDraggableModifier() })
        .size(circleDiameter)
        .padding(4.dp)
        .background({ boxColor }, CircleShape),
    ) {
      isAcceptedTransition.AnimatedContent(
        contentAlignment = Alignment.Center,
        modifier = Modifier.align(Alignment.Center),
      ) { isAccepted ->
        if (isAccepted) {
          Icon(
            imageVector = HedvigIcons.Checkmark,
            contentDescription = null,
            tint = HedvigTheme.colorScheme.fillBlack,
          )
        } else {
          Icon(
            imageVector = HedvigIcons.ChevronRight,
            contentDescription = null,
            tint = HedvigTheme.colorScheme.fillNegative,
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPledgeAcceptingSlider() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PledgeAcceptingSlider(
        {},
        "Slide to start",
      )
    }
  }
}

private fun Modifier.background(colorProvider: () -> Color, shape: Shape) = this.background(colorProvider(), shape)
