package com.hedvig.android.sample.design.showcase.util

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal fun Modifier.freeScroll(state: FreeScrollState, enabled: Boolean = true): Modifier = composed {
  val velocityTracker = remember { VelocityTracker() }
  val fling = ScrollableDefaults.flingBehavior()

  this
    .horizontalScroll(
      state = state.horizontalScrollState,
      enabled = false,
    )
    .verticalScroll(
      state = state.verticalScrollState,
      enabled = false,
    )
    .pointerInput(state, velocityTracker, enabled) {
      if (!enabled) return@pointerInput
      coroutineScope {
        detectDragGestures(
          onDrag = { change, dragAmount ->
            change.consume()
            onDrag(
              change = change,
              dragAmount = dragAmount,
              state = state,
              velocityTracker = velocityTracker,
              coroutineScope = this,
            )
          },
          onDragEnd = {
            onEnd(
              velocityTracker = velocityTracker,
              state = state,
              flingBehavior = fling,
              coroutineScope = this,
            )
          },
        )
      }
    }
}

@Stable
internal class FreeScrollState(
  val horizontalScrollState: ScrollState,
  val verticalScrollState: ScrollState,
)

@Composable
internal fun rememberFreeScrollState(initialX: Int = 0, initialY: Int = 0): FreeScrollState {
  val horizontalScrollState = rememberScrollState(initialX)
  val verticalScrollState = rememberScrollState(initialY)
  return remember {
    FreeScrollState(horizontalScrollState, verticalScrollState)
  }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun onDrag(
  change: PointerInputChange?,
  dragAmount: Offset,
  state: FreeScrollState,
  velocityTracker: VelocityTracker,
  coroutineScope: CoroutineScope,
) {
  coroutineScope.launch {
    state.horizontalScrollState.scrollBy(-dragAmount.x)
    state.verticalScrollState.scrollBy(-dragAmount.y)
  }

  if (change == null) {
    velocityTracker.resetTracking()
    return
  }

  // Add historical position to velocity tracker to increase accuracy
  val changeList = change.historical.map {
    it.uptimeMillis to it.position
  } + (change.uptimeMillis to change.position)

  changeList.forEach { (time, pos) ->
    val position = Offset(
      x = pos.x - state.horizontalScrollState.value,
      y = pos.y - state.verticalScrollState.value,
    )
    velocityTracker.addPosition(time, position)
  }
}

private fun onEnd(
  velocityTracker: VelocityTracker,
  state: FreeScrollState,
  flingBehavior: FlingBehavior,
  coroutineScope: CoroutineScope,
) {
  val velocity = velocityTracker.calculateVelocity()
  velocityTracker.resetTracking()

  // Launch two animation separately to make sure they work simultaneously.
  coroutineScope.launch {
    state.horizontalScrollState.scroll {
      with(flingBehavior) {
        performFling(-velocity.x)
      }
    }
  }
  coroutineScope.launch {
    state.verticalScrollState.scroll {
      with(flingBehavior) {
        performFling(-velocity.y)
      }
    }
  }
}
