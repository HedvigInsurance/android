package com.hedvig.android.core.designsystem.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults

@ExperimentalAnimationApi
@Composable
fun FadeAnimatedVisibility(
  isLoading: Boolean,
  content: @Composable () -> Unit,
) {
  HedvigFullScreenCenterAlignedProgressDebounced(show = isLoading)
  AnimatedVisibility(
    visible = !isLoading,
    enter = MotionDefaults.fadeThroughEnter,
    exit = MotionDefaults.fadeThroughExit,
  ) {
    content()
  }
}

@ExperimentalAnimationApi
@Composable
fun <S> FadeAnimatedContent(
  targetState: S,
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.TopStart,
  label: String = "AnimatedContent",
  content: @Composable AnimatedVisibilityScope.(targetState: S) -> Unit,
) = AnimatedContent(
  targetState = targetState,
  modifier = modifier,
  transitionSpec = {
    MotionDefaults.fadeThroughEnter with MotionDefaults.fadeThroughExit
  },
  label = label,
  content = content,
  contentAlignment = contentAlignment,
)
