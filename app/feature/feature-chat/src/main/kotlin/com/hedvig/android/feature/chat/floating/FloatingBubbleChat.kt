@file:OptIn(
  ExperimentalTransitionApi::class,
  ExperimentalSharedTransitionApi::class,
  ExperimentalAnimationSpecApi::class,
)

package com.hedvig.android.feature.chat.floating

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.material3.squircleLarge
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.colored.hedvig.Chat
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.ui.ChatDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun FloatingBubbleChat(
  imageLoader: ImageLoader,
  appPackageId: String,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val viewModel: ChatViewModel = koinViewModel()
  val floatingBubbleState = rememberFloatingBubbleState()
  FloatingBubble(modifier, floatingBubbleState) { expandedContentModifier ->
    ChatDestination(
      viewModel = viewModel,
      imageLoader = imageLoader,
      appPackageId = appPackageId,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      openUrl = openUrl,
      onNavigateUp = {
        floatingBubbleState.minimize()
      },
      modifier = expandedContentModifier,
    )
  }
}

@Composable
private fun FloatingBubble(
  modifier: Modifier = Modifier,
  floatingBubbleState: FloatingBubbleState,
  expandedContent: @Composable (Modifier) -> Unit,
) {
  val transition = rememberTransition(floatingBubbleState.seekableTransition)
  floatingBubbleState.PredictiveBackHandler()
//  val sharedChatIcon = movableContentWithReceiverOf<
//    SharedTransitionScope,
//    AnimatedContentScope,
//    () -> Unit,
//    Modifier,
//  > { animatedContentScope, onClick, movableContentModifier ->
//    ChatCircle(animatedContentScope, onClick, movableContentModifier)
//  }
  val sharedChatIcon: @Composable SharedTransitionScope.(
    AnimatedContentScope,
    () -> Unit,
    Modifier,
  ) -> Unit = { animatedContentScope, onClick, movableContentModifier ->
    ChatCircle(animatedContentScope, onClick, movableContentModifier)
  }
  SharedTransitionLayout(modifier) {
    transition.AnimatedContent { bubbleState: FloatingBubbleState.BubbleState ->
      when (bubbleState) {
        FloatingBubbleState.BubbleState.Minimized -> {
          MinimizedBubble(
            floatingBubbleState,
            this@AnimatedContent,
            chatIcon = { chatModifier ->
              sharedChatIcon(this@AnimatedContent, floatingBubbleState::expand, chatModifier)
            },
          )
        }

        FloatingBubbleState.BubbleState.Expanded -> {
          MaximizedBubble(
            this@AnimatedContent,
            onClickOutside = floatingBubbleState::minimize,
            modifier = modifier,
            chatIcon = { chatModifier ->
              sharedChatIcon(this@AnimatedContent, floatingBubbleState::minimize, chatModifier)
            },
            expandedContent = expandedContent,
          )
        }
      }
    }
  }
}

@Composable
private fun SharedTransitionScope.MinimizedBubble(
  floatingBubbleState: FloatingBubbleState,
  animatedContentScope: AnimatedContentScope,
  modifier: Modifier = Modifier,
  chatIcon: @Composable (Modifier) -> Unit,
) {
  val density = LocalDensity.current
  chatIcon(
    modifier
      .fillMaxSize()
      .safeDrawingPadding()
      .draggableBubble(floatingBubbleState, density)
      .sharedBounds(rememberSharedContentState(SharedSurfaceKey), animatedContentScope),
  )
}

private fun Modifier.draggableBubble(floatingBubbleState: FloatingBubbleState, density: Density): Modifier =
  this.composed {
    val offset = floatingBubbleState.offset
    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
    MakeInitialPositionTopRightEffect(offset, layoutCoordinates, density)
    val spaceToEdges = 8.dp
    this
      .padding(spaceToEdges)
      .onPlaced { coordinates ->
        layoutCoordinates = coordinates
      }
      .then(
        if (layoutCoordinates == null) {
//          Modifier.withoutPlacement() // This breaks with "Error: Placement happened before lookahead."
          Modifier.alpha(0f)
        } else {
          Modifier
        },
      )
      .wrapContentSize(Alignment.TopStart)
      .offset { offset.value.round() }
      .pointerInput(layoutCoordinates, offset) {
        @Suppress("NAME_SHADOWING")
        val layoutCoordinates = layoutCoordinates ?: return@pointerInput
        val chatCircleDiameter: Float = ChatCircleDiameter.toPx()
        val halfChatCircleDiameter: Float = chatCircleDiameter / 2
        val decay = splineBasedDecay<Offset>(this)
        val velocityTracker = VelocityTracker()
        coroutineScope {
          detectDragGestures(
            onDrag = { change, dragAmount ->
              val offsetValue = offset.value
              val targetValue = Offset(
                offsetValue.x + dragAmount.x,
                offset.value.y + dragAmount.y,
              )
              velocityTracker.addPosition(change.uptimeMillis, targetValue)
              launch(Dispatchers.Unconfined) {
                offset.snapTo(targetValue)
              }
            },
            onDragEnd = {
              val velocity: Velocity = velocityTracker.calculateVelocity()
              velocityTracker.resetTracking()
              val targetOffsetAfterFlingEnd = decay.calculateTargetValue(
                typeConverter = Offset.VectorConverter,
                initialValue = offset.value,
                initialVelocity = Offset(velocity.x, velocity.y), // maybe?
              )
              val middleHorizontalPoint = layoutCoordinates.size.width / 2 - halfChatCircleDiameter
              val targetX = if (targetOffsetAfterFlingEnd.x < middleHorizontalPoint) {
                0f
              } else {
                layoutCoordinates.size.width - chatCircleDiameter
              }
              val targetY = targetOffsetAfterFlingEnd.y.coerceIn(
                0f,
                layoutCoordinates.size.height - chatCircleDiameter,
              )
              launch {
                offset.animateTo(
                  targetValue = Offset(targetX, targetY),
                  animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
                  initialVelocity = Offset(velocity.x, velocity.y),
                )
              }
            },
          )
        }
      }
      .systemGestureExclusion { coordinates ->
        val boundsInRoot = coordinates.boundsInRoot()
        Rect(
          boundsInRoot.left,
          boundsInRoot.top,
          boundsInRoot.right,
          boundsInRoot.bottom,
        ).inflate(with(density) { (spaceToEdges * 2).toPx() })
      }
  }

/**
 * If there was no offset already it means we just started the app so we want to move the offset to the top right
 */
@Composable
private fun MakeInitialPositionTopRightEffect(
  offset: Animatable<Offset, AnimationVector2D>,
  layoutCoordinates: LayoutCoordinates?,
  density: Density,
) {
  LaunchedEffect(offset, layoutCoordinates, density) {
    @Suppress("NAME_SHADOWING")
    val layoutCoordinates = layoutCoordinates ?: return@LaunchedEffect
    if (offset.value == Offset.Zero) {
      val endOfScreen = layoutCoordinates.size.width - with(density) { ChatCircleDiameter.toPx() }
      offset.snapTo(Offset(endOfScreen, 0f))
    }
  }
}

@Composable
private fun SharedTransitionScope.MaximizedBubble(
  animatedContentScope: AnimatedContentScope,
  onClickOutside: () -> Unit,
  modifier: Modifier = Modifier,
  chatIcon: @Composable (Modifier) -> Unit,
  expandedContent: @Composable (Modifier) -> Unit,
) {
  Column(
    modifier
      .fillMaxSize()
      .clickable(
        interactionSource = null,
        indication = null,
        onClick = onClickOutside,
      )
      .background(MaterialTheme.colorScheme.onBackground.copy(alpha = DisabledAlpha))
      .safeDrawingPadding()
      .padding(8.dp),
  ) {
    chatIcon(Modifier)
    Spacer(Modifier.height(8.dp)) // make arrow-like shape here instead
    Surface(
      Modifier.weight(1f),
      MaterialTheme.shapes.squircleLarge,
    ) {
      expandedContent(
        Modifier.sharedBounds(rememberSharedContentState(SharedSurfaceKey), animatedContentScope),
      )
    }
  }
}

@Composable
private fun SharedTransitionScope.ChatCircle(
  animatedContentScope: AnimatedContentScope,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier
      .clickable(
        interactionSource = null,
        indication = null,
        onClick = onClick,
      )
      .size(ChatCircleDiameter)
      .sharedElement(rememberSharedContentState("ChatCircle"), animatedContentScope),
  ) {
    Icon(
      imageVector = Icons.Hedvig.Chat,
      contentDescription = null,
      tint = Color.Unspecified,
      modifier = Modifier.size(ChatCircleDiameter),
    )
  }
}

@OptIn(ExperimentalTransitionApi::class)
private class FloatingBubbleState(
  private val coroutineScope: CoroutineScope,
) {
  val seekableTransition = SeekableTransitionState<BubbleState>(BubbleState.Minimized)

  // Offset position for the minimized version of the bubble
  val offset: Animatable<Offset, AnimationVector2D> = Animatable(
    Offset.Zero,
    Offset.VectorConverter,
    Offset.VisibilityThreshold,
  )

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
private fun rememberFloatingBubbleState(): FloatingBubbleState {
  val coroutineScope = rememberCoroutineScope()
  return remember { FloatingBubbleState(coroutineScope) }
}

private const val SharedSurfaceKey = "SharedSurfaceKey"

private val ChatCircleDiameter: Dp = 48.dp
