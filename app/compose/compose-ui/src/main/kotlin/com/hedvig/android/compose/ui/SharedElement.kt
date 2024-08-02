@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.hedvig.android.compose.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.contentSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.ScaleToBounds
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * A local which contains the SharedTransitionScope wrapping the entire app. This is always taking up the entire
 * screen's size and must be provided by the app's main activity
 */
val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> = compositionLocalOf {
  error("The app must provide a SharedTransitionScope to the entire compose hierarchy")
}

@Composable
fun rememberSharedContentState(key: Any): SharedContentState =
  LocalSharedTransitionScope.current.rememberSharedContentState(key)

fun Modifier.sharedElement(
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedVisibilityScope,
  state: SharedContentState,
  boundsTransform: BoundsTransform = DefaultBoundsTransform,
  placeHolderSize: PlaceHolderSize = contentSize,
  renderInOverlayDuringTransition: Boolean = true,
  zIndexInOverlay: Float = 0f,
  clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier = with(sharedTransitionScope) {
  this@sharedElement.sharedElement(
    state = state,
    animatedVisibilityScope = animatedVisibilityScope,
    boundsTransform = boundsTransform,
    placeHolderSize = placeHolderSize,
    renderInOverlayDuringTransition = renderInOverlayDuringTransition,
    zIndexInOverlay = zIndexInOverlay,
    clipInOverlayDuringTransition = clipInOverlayDuringTransition,
  )
}

fun Modifier.sharedBounds(
  sharedTransitionScope: SharedTransitionScope,
  animatedVisibilityScope: AnimatedVisibilityScope,
  state: SharedContentState,
  enter: EnterTransition = fadeIn(),
  exit: ExitTransition = fadeOut(),
  boundsTransform: BoundsTransform = DefaultBoundsTransform,
  resizeMode: ResizeMode = ScaleToBounds(ContentScale.FillWidth, Center),
  placeHolderSize: PlaceHolderSize = contentSize,
  renderInOverlayDuringTransition: Boolean = true,
  zIndexInOverlay: Float = 0f,
  clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier = with(sharedTransitionScope) {
  this@sharedBounds.sharedBounds(
    sharedContentState = state,
    animatedVisibilityScope = animatedVisibilityScope,
    enter = enter,
    exit = exit,
    boundsTransform = boundsTransform,
    resizeMode = resizeMode,
    placeHolderSize = placeHolderSize,
    renderInOverlayDuringTransition = renderInOverlayDuringTransition,
    zIndexInOverlay = zIndexInOverlay,
    clipInOverlayDuringTransition = clipInOverlayDuringTransition,
  )
}

private val DefaultBoundsTransform: BoundsTransform = BoundsTransform { _, _ -> DefaultSpring }

private val DefaultSpring: SpringSpec<Rect> = spring(
  stiffness = StiffnessMediumLow,
  visibilityThreshold = Rect.VisibilityThreshold,
)

private val ParentClip: OverlayClip = object : OverlayClip {
  override fun getClipPath(
    state: SharedContentState,
    bounds: Rect,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Path? = state.parentSharedContentState?.clipPathInOverlay
}
