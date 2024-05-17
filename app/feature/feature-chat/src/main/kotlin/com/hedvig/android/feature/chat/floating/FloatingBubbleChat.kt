@file:OptIn(
  ExperimentalTransitionApi::class,
  ExperimentalSharedTransitionApi::class,
  ExperimentalAnimationSpecApi::class,
)

package com.hedvig.android.feature.chat.floating

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.material3.squircleLarge
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.colored.hedvig.Chat
import com.hedvig.android.core.icons.hedvig.compose.notificationCircle
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.ui.ChatDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun FloatingBubbleChat(
  isInHomeScreen: Boolean,
  imageLoader: ImageLoader,
  appPackageId: String,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val viewModel: ChatViewModel = koinViewModel()
  val floatingBubbleViewModel: FloatingBubbleViewModel = koinViewModel()
  val floatingBubbleUiState by floatingBubbleViewModel.uiState.collectAsStateWithLifecycle()
  val floatingBubbleState = rememberFloatingBubbleState(isInHomeScreen)
  FloatingBubble(
    modifier = modifier,
    floatingBubbleState = floatingBubbleState,
    showWelcomeTooltip = floatingBubbleUiState.showWelcomeTooltip,
    onWelcomeTooltipShown = { floatingBubbleViewModel.emit(FloatingBubbleEvent.SeenTooltip) },
    hasUnseenChatMessages = floatingBubbleUiState.hasUnseenChatMessages,
  ) {
    ChatDestination(
      viewModel = viewModel,
      imageLoader = imageLoader,
      appPackageId = appPackageId,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      openUrl = openUrl,
      onNavigateUp = {
        floatingBubbleState.minimize()
      },
    )
  }
}

@Composable
private fun FloatingBubble(
  modifier: Modifier = Modifier,
  floatingBubbleState: FloatingBubbleState,
  showWelcomeTooltip: Boolean,
  onWelcomeTooltipShown: () -> Unit,
  hasUnseenChatMessages: Boolean,
  expandedContent: @Composable () -> Unit,
) {
  val transition = rememberTransition(floatingBubbleState.seekableTransition)
  floatingBubbleState.PredictiveBackHandler()
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
            floatingBubbleState = floatingBubbleState,
            animatedContentScope = this@AnimatedContent,
            showWelcomeTooltip = showWelcomeTooltip,
            onWelcomeTooltipShown = onWelcomeTooltipShown,
            hasUnseenChatMessages = hasUnseenChatMessages,
            chatIcon = { chatModifier ->
              sharedChatIcon(this@AnimatedContent, floatingBubbleState::expand, chatModifier)
            },
          )
        }

        FloatingBubbleState.BubbleState.Expanded -> {
          ExpandedBubble(
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
  showWelcomeTooltip: Boolean,
  onWelcomeTooltipShown: () -> Unit,
  hasUnseenChatMessages: Boolean,
  chatIcon: @Composable (Modifier) -> Unit,
) {
  val density = LocalDensity.current
  Column(
    Modifier.draggableBubble(floatingBubbleState, density),
  ) {
    chatIcon(
      Modifier
        .notificationCircle(-SpaceFromScreenEdge, hasUnseenChatMessages)
        .sharedBounds(rememberSharedContentState(SharedSurfaceKey), animatedContentScope),
    )
    if (showWelcomeTooltip) {
      ChatTooltip(
        showTooltip = showWelcomeTooltip,
        tooltipShown = { onWelcomeTooltipShown() },
        modifier = Modifier
          .align(Alignment.End)
          .padding(horizontal = 16.dp),
      )
    }
  }
}

private fun Modifier.draggableBubble(floatingBubbleState: FloatingBubbleState, density: Density): Modifier =
  this.composed {
    val offset = floatingBubbleState.offset
    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
    SetInitialAndHomeOffsetsEffect(floatingBubbleState, layoutCoordinates, density)
    this
      .fillMaxSize()
      .safeDrawingPadding()
      .onPlaced { layoutCoordinates = it }
      .then(
        if (layoutCoordinates == null || !floatingBubbleState.isReady) {
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
        val chatCircleWidth: Float = ChatCircleWidth.toPx()
        val chatCircleHeight: Float = ChatCircleHeight.toPx()
        val decay = splineBasedDecay<Offset>(this)
        val velocityTracker = VelocityTracker()
        coroutineScope {
          detectDragGestures(
            onDrag = { change, dragAmount ->
              val offsetValue = offset.value
              val targetValue = Offset(
                offsetValue.x + dragAmount.x,
                offsetValue.y + dragAmount.y,
              )
              velocityTracker.addPosition(change.uptimeMillis, targetValue)
              launch(Dispatchers.Unconfined) {
                offset.snapTo(targetValue)
              }
            },
            onDragEnd = {
              val velocity: Velocity = velocityTracker.calculateVelocity()
              val initialVelocity = Offset(velocity.x, velocity.y)
              velocityTracker.resetTracking()
              val targetOffsetAfterFlingEnd = decay.calculateTargetValue(
                typeConverter = Offset.VectorConverter,
                initialValue = offset.value,
                initialVelocity = initialVelocity,
              )
              val middleHorizontalPoint = layoutCoordinates.size.width / 2 - chatCircleWidth / 2
              val targetX = if (targetOffsetAfterFlingEnd.x < middleHorizontalPoint) {
                0f
              } else {
                layoutCoordinates.size.width - chatCircleWidth
              }
              val targetY = targetOffsetAfterFlingEnd.y.coerceIn(
                0f,
                layoutCoordinates.size.height - chatCircleHeight,
              )
              launch {
                offset.animateTo(
                  targetValue = Offset(targetX, targetY),
                  animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
                  initialVelocity = initialVelocity,
                )
              }
            },
          )
        }
      }
      .systemGestureExclusion()
  }

@Composable
private fun SetInitialAndHomeOffsetsEffect(
  floatingBubbleState: FloatingBubbleState,
  layoutCoordinates: LayoutCoordinates?,
  density: Density,
) {
  LaunchedEffect(floatingBubbleState, layoutCoordinates, density) {
    @Suppress("NAME_SHADOWING")
    val layoutCoordinates = layoutCoordinates ?: return@LaunchedEffect
    with(density) {
      val endOfScreen = layoutCoordinates.size.width - ChatCircleWidth.toPx()
      val homeYPosition = ((TopActionsHeight - ChatCircleHeight) / 2).toPx()
      val targetValueForHomeScreen = Offset(endOfScreen, homeYPosition)
      floatingBubbleState.saveHomeScreenOffset(targetValueForHomeScreen)
      if (floatingBubbleState.offset.value == Offset.Zero) {
        floatingBubbleState.offset.snapTo(Offset(endOfScreen, 0f))
      }
    }
  }
}

@Composable
private fun SharedTransitionScope.ExpandedBubble(
  animatedContentScope: AnimatedContentScope,
  onClickOutside: () -> Unit,
  modifier: Modifier = Modifier,
  chatIcon: @Composable (Modifier) -> Unit,
  expandedContent: @Composable () -> Unit,
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
      .padding(8.dp)
      .sharedBounds(rememberSharedContentState(SharedSurfaceKey), animatedContentScope),
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      chatIcon(Modifier)
      Spacer(Modifier.height(4.dp))
      val arrowColor = MaterialTheme.colorScheme.background
      Spacer(
        Modifier
          .size(height = 8.dp, width = 12.dp)
          .drawChatBubbleArrow(arrowColor),
      )
    }
    Surface(
      Modifier.weight(1f),
      MaterialTheme.shapes.squircleLarge,
    ) {
      expandedContent()
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
      .size(ChatCircleWidth, ChatCircleHeight)
      .clickable(
        interactionSource = null,
        indication = null,
        onClick = onClick,
      )
      .padding(horizontal = SpaceFromScreenEdge)
      .sharedElement(rememberSharedContentState("ChatCircle"), animatedContentScope),
    propagateMinConstraints = true,
  ) {
    Icon(
      imageVector = Icons.Hedvig.Chat,
      contentDescription = null,
      tint = Color.Unspecified,
    )
  }
}

private fun Modifier.drawChatBubbleArrow(color: Color): Modifier = drawBehind {
  drawPath(
    path = Path().apply {
      moveTo(0f, size.height)
      lineTo(size.width / 2, 0f)
      lineTo(size.width, size.height)
      close()
    },
    color = color,
  )
}

private const val SharedSurfaceKey = "SharedSurfaceKey"

private val SpaceFromScreenEdge = 16.dp
private val ChatCircleHeight: Dp = 40.dp
private val ChatCircleWidth: Dp = 72.dp

/**
 * The height that the icons in the home screen are centered in
 */
private val TopActionsHeight = 64.dp
