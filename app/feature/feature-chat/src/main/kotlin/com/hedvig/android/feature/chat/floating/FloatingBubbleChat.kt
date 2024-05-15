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
import androidx.compose.animation.core.ExperimentalAnimationSpecApi
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
            this@AnimatedContent,
            chatIcon = { modifier ->
              sharedChatIcon(this@AnimatedContent, floatingBubbleState::expand, modifier)
            },
          )
        }

        FloatingBubbleState.BubbleState.Expanded -> {
          MaximizedBubble(
            this@AnimatedContent,
            onClickOutside = floatingBubbleState::minimize,
            modifier = modifier,
            chatIcon = { modifier ->
              sharedChatIcon(this@AnimatedContent, floatingBubbleState::minimize, modifier)
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
  animatedContentScope: AnimatedContentScope,
  modifier: Modifier = Modifier,
  chatIcon: @Composable (Modifier) -> Unit,
) {
  chatIcon(
    modifier
      .fillMaxSize()
      .wrapContentSize(BiasAlignment(1f, -0.75f))
      .sharedBounds(rememberSharedContentState(SharedSurfaceKey), animatedContentScope),
  )
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
      .background(MaterialTheme.colorScheme.onBackground.copy(alpha = DisabledAlpha)) // Scrim
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
  IconButton(
    onClick,
    modifier.sharedElement(rememberSharedContentState("ChatCircle"), animatedContentScope),
  ) {
    Icon(
      imageVector = Icons.Hedvig.Chat,
      contentDescription = null,
      tint = Color.Unspecified,
      modifier = Modifier.size(48.dp),
    )
  }
}

@OptIn(ExperimentalTransitionApi::class)
private class FloatingBubbleState(
  private val coroutineScope: CoroutineScope,
) {
  val seekableTransition = SeekableTransitionState<BubbleState>(BubbleState.Minimized)

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
    PredictiveBackHandler(isBackHandlerEnabled) { progress ->
      try {
        progress.collect { backEvent ->
          seekableTransition.seekTo(backEvent.progress, BubbleState.Minimized)
        }
        seekableTransition.animateTo(BubbleState.Minimized)
      } catch (e: CancellationException) {
        seekableTransition.animateTo(BubbleState.Expanded)
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
