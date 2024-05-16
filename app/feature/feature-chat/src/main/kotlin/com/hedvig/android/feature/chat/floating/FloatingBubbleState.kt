package com.hedvig.android.feature.chat.floating

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class FloatingBubbleState(
  private val coroutineScope: CoroutineScope,
) {
  val seekableTransition = SeekableTransitionState<BubbleState>(BubbleState.Minimized)

  // Offset position for the minimized version of the bubble
  val offset: Animatable<Offset, AnimationVector2D> = Animatable(
    Offset.Zero,
    Offset.VectorConverter,
    Offset.VisibilityThreshold,
  )
  private var savedOffsetBeforeGoingToHomeScreen: Offset? by mutableStateOf(null)
  private var offsetForHomeScreen: Offset? by mutableStateOf(null)

  val isReady: Boolean
    get() = offsetForHomeScreen != null

  fun minimize() {
    coroutineScope.launch {
      seekableTransition.animateTo(BubbleState.Minimized)
    }
  }

  fun expand() {
    coroutineScope.launch {
      seekableTransition.animateTo(BubbleState.Expanded)
    }
  }

  fun enteredHomeScreen() {
    offsetForHomeScreen?.let { offsetForHomeScreen ->
      coroutineScope.launch {
        savedOffsetBeforeGoingToHomeScreen = offset.targetValue
        offset.animateTo(offsetForHomeScreen, spring(stiffness = Spring.StiffnessLow))
      }
    }
  }

  /**
   * When exiting the home screen, if we have *not* moved from the new position, we want to animate back to the
   * position we were at before coming to home
   */
  fun exitedHomeScreen() {
    savedOffsetBeforeGoingToHomeScreen?.let { savedOffsetBeforeGoingToHomeScreen ->
      if (offset.targetValue == offsetForHomeScreen) {
        coroutineScope.launch {
          offset.animateTo(savedOffsetBeforeGoingToHomeScreen, spring(stiffness = Spring.StiffnessLow))
          this@FloatingBubbleState.savedOffsetBeforeGoingToHomeScreen = null
        }
      }
    }
  }

  fun saveHomeScreenOffset(offsetForHomeScreen: Offset) {
    this.offsetForHomeScreen = offsetForHomeScreen
  }

  @Composable
  fun PredictiveBackHandler() {
    val isBackHandlerEnabled = !(
      seekableTransition.currentState == BubbleState.Minimized &&
        seekableTransition.targetState == BubbleState.Minimized
    )
    var progress by remember { mutableFloatStateOf(0f) }
    var inPredictiveBack by remember { mutableStateOf(false) }
    var finishedBack by remember { mutableStateOf(false) }
    var cancelledBack by remember { mutableStateOf(false) }
    PredictiveBackHandler(isBackHandlerEnabled) { backEvent ->
      progress = 0f
      cancelledBack = false
      finishedBack = false
      try {
        backEvent.collect {
          inPredictiveBack = true
          progress = it.progress
        }
        inPredictiveBack = false
        finishedBack = true
      } catch (e: CancellationException) {
        inPredictiveBack = false
        cancelledBack = true
      }
    }
    if (inPredictiveBack) {
      LaunchedEffect(progress) {
        seekableTransition.seekTo(progress, BubbleState.Minimized)
      }
    }
    if (finishedBack) {
      LaunchedEffect(Unit) {
        seekableTransition.animateTo(BubbleState.Minimized)
      }
    }
    if (cancelledBack) {
      LaunchedEffect(Unit) {
        seekableTransition.snapTo(BubbleState.Expanded)
      }
    }
  }

  enum class BubbleState {
    Minimized,
    Expanded,
  }
}

@Composable
internal fun rememberFloatingBubbleState(isInHomeScreen: Boolean): FloatingBubbleState {
  val coroutineScope = rememberCoroutineScope()
  val floatingBubbleState = remember { FloatingBubbleState(coroutineScope) }
  LaunchedEffect(floatingBubbleState.isReady, isInHomeScreen) {
    if (isInHomeScreen) {
      floatingBubbleState.enteredHomeScreen()
    } else {
      floatingBubbleState.exitedHomeScreen()
    }
  }
  return floatingBubbleState
}
