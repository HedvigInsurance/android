package com.hedvig.android.feature.chat.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest

/**
 * todo: Remove after bumping material 3 dependency which should then handle hiding the top app bar even when
 *  window insets are involved.
 */
@Composable
internal fun chatTopAppBarWindowInsets(
  windowInsets: WindowInsets,
  topAppBarScrollBehavior: TopAppBarScrollBehavior,
): WindowInsets {
  val density = LocalDensity.current
  val layoutDirection = LocalLayoutDirection.current
  var resultingInsets by remember { mutableStateOf(windowInsets) }
  LaunchedEffect(topAppBarScrollBehavior.state, density, layoutDirection) {
    snapshotFlow { topAppBarScrollBehavior.state.collapsedFraction }.collectLatest { collapsedFraction ->
      resultingInsets = windowInsets.adaptToCollapsedFraction(collapsedFraction, density, layoutDirection)
    }
  }
  return resultingInsets
}

private fun WindowInsets.adaptToCollapsedFraction(
  collapsedFraction: Float,
  density: Density,
  layoutDirection: LayoutDirection,
): WindowInsets {
  val multiplyBy = 1 - collapsedFraction
  return WindowInsets(
    left = (getLeft(density, layoutDirection) * multiplyBy).roundToInt(),
    right = (getRight(density, layoutDirection) * multiplyBy).roundToInt(),
    top = (getTop(density) * multiplyBy).roundToInt(),
    bottom = (getBottom(density) * multiplyBy).roundToInt(),
  )
}

@Suppress("UnusedReceiverParameter")
@Composable
internal fun TopAppBarDefaults.chatScrollBehavior(
  state: TopAppBarState = rememberTopAppBarState(),
  canScroll: () -> Boolean = { true },
  snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
  flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
): TopAppBarScrollBehavior = ChatScrollBehavior(
  state = state,
  snapAnimationSpec = snapAnimationSpec,
  flingAnimationSpec = flingAnimationSpec,
  canScroll = canScroll,
)

/**
 * From:
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/AppBar.kt;l=2791?q=TopAppBarScrollBehavior&ss=androidx%2Fplatform%2Fframeworks%2Fsupport
 */
private class ChatScrollBehavior(
  override val state: TopAppBarState,
  override val snapAnimationSpec: AnimationSpec<Float>?,
  override val flingAnimationSpec: DecayAnimationSpec<Float>?,
  val canScroll: () -> Boolean = { true },
) : TopAppBarScrollBehavior {
  override val isPinned: Boolean = false
  override var nestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
      if (!canScroll()) return Offset.Zero
      val prevHeightOffset = state.heightOffset
      state.heightOffset = state.heightOffset + available.y
      return if (prevHeightOffset != state.heightOffset) {
        // We're in the middle of top app bar collapse or expand.
        // Consume only the scroll on the Y axis.
        available.copy(x = 0f)
      } else {
        Offset.Zero
      }
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
      if (!canScroll()) return Offset.Zero
      state.contentOffset += consumed.y
      if (state.heightOffset == 0f || state.heightOffset == state.heightOffsetLimit) {
        if (consumed.y == 0f && available.y > 0f) {
          // Reset the total content offset to zero when scrolling all the way down.
          // This will eliminate some float precision inaccuracies.
          state.contentOffset = 0f
        }
      }
      state.heightOffset = state.heightOffset + consumed.y
      return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
      val superConsumed = super.onPostFling(consumed, available)
      return superConsumed + settleAppBar(state, available.y, flingAnimationSpec, snapAnimationSpec)
    }
  }
}

private suspend fun settleAppBar(
  state: TopAppBarState,
  velocity: Float,
  flingAnimationSpec: DecayAnimationSpec<Float>?,
  snapAnimationSpec: AnimationSpec<Float>?,
): Velocity {
  // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
  // and just return Zero Velocity.
  // Note that we don't check for 0f due to float precision with the collapsedFraction
  // calculation.
  if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
    return Velocity.Zero
  }
  var remainingVelocity = velocity
  // In case there is an initial velocity that was left after a previous user fling, animate to
  // continue the motion to expand or collapse the app bar.
  if (flingAnimationSpec != null && abs(velocity) > 1f) {
    var lastValue = 0f
    AnimationState(
      initialValue = 0f,
      initialVelocity = velocity,
    ).animateDecay(flingAnimationSpec) {
      val delta = value - lastValue
      val initialHeightOffset = state.heightOffset
      state.heightOffset = initialHeightOffset + delta
      val consumed = abs(initialHeightOffset - state.heightOffset)
      lastValue = value
      remainingVelocity = this.velocity
      // avoid rounding errors and stop if anything is unconsumed
      if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
    }
  }
  // Snap if animation specs were provided.
  if (snapAnimationSpec != null) {
    if (state.heightOffset < 0 &&
      state.heightOffset > state.heightOffsetLimit
    ) {
      AnimationState(initialValue = state.heightOffset).animateTo(
        if (state.collapsedFraction < 0.5f) {
          0f
        } else {
          state.heightOffsetLimit
        },
        animationSpec = snapAnimationSpec,
      ) { state.heightOffset = value }
    }
  }

  return Velocity(0f, remainingVelocity)
}
