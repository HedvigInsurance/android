package com.hedvig.android.feature.onboarding.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.hedvig.android.compose.ui.LocalSharedTransitionScope
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.onboarding.data.OnboardingSession
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import hedvig.resources.Res
import hedvig.resources.general_back_button
import hedvig.resources.general_close_button
import org.jetbrains.compose.resources.stringResource

internal data class OnboardingProgress(
  val totalSteps: Int,
  val currentIndex: Int,
)

/**
 * Welcome (stepId == null) sits at progress position 0; path steps follow it. If [stepId] is not
 * in the path (possible only when a process-death refetch rebuilt a different path under a
 * restored step), this degrades to index 0 rather than crashing; the step stays fully usable.
 */
internal fun OnboardingSession.progressFor(stepId: OnboardingStepId?): OnboardingProgress {
  return OnboardingProgress(
    totalSteps = path.size + 1,
    currentIndex = if (stepId == null) 0 else path.indexOf(stepId) + 1,
  )
}

/**
 * Ties every in-flow step's header to the same shared element so it stays pinned while the page
 * content below runs the normal transition. Steps hosted by different destinations (Forever, the
 * external connect-payment flow) don't render this scaffold, so they never match it.
 */
private const val OnboardingTopBarSharedKey = "onboarding-top-bar"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun OnboardingStepScaffold(
  progress: OnboardingProgress?,
  showBackButton: Boolean,
  onBackClick: () -> Unit,
  onCloseClick: () -> Unit,
  modifier: Modifier = Modifier,
  progressAnimation: OnboardingProgressBarAnimation? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  // Null only in isolated previews, where there is no NavEntry to read the animated-content scope
  // from; the header then renders plainly and the progress bar draws its fill statically.
  val sharedTransitionScope = LocalSharedTransitionScope.current
  val animatedVisibilityScope = if (sharedTransitionScope != null) LocalNavAnimatedContentScope.current else null
  val headerModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
    with(sharedTransitionScope) {
      Modifier.sharedBounds(
        sharedContentState = rememberSharedContentState(OnboardingTopBarSharedKey),
        animatedVisibilityScope = animatedVisibilityScope,
      )
    }
  } else {
    Modifier
  }
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = modifier.fillMaxSize(),
  ) {
    Column(
      Modifier.windowInsetsPadding(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
      ),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = headerModifier
          .fillMaxWidth()
          .height(56.dp)
          .padding(horizontal = 16.dp),
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = if (!showBackButton) {
            Modifier
              .clearAndSetSemantics {}
              .withoutPlacement()
          } else {
            Modifier
          },
        ) {
          Icon(
            imageVector = HedvigIcons.ArrowLeft,
            contentDescription = stringResource(Res.string.general_back_button),
            modifier = Modifier.size(24.dp),
          )
        }
        if (progress != null) {
          OnboardingProgressBar(
            progress = progress,
            animation = progressAnimation,
            animatedVisibilityScope = animatedVisibilityScope,
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 24.dp),
          )
        } else {
          Spacer(Modifier.weight(1f))
        }
        IconButton(
          onClick = onCloseClick,
        ) {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = stringResource(Res.string.general_close_button),
            modifier = Modifier.size(24.dp),
          )
        }
      }
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState()),
        content = content,
      )
    }
  }
}

@Composable
private fun OnboardingProgressBar(
  progress: OnboardingProgress,
  animation: OnboardingProgressBarAnimation?,
  animatedVisibilityScope: AnimatedVisibilityScope?,
  modifier: Modifier = Modifier,
) {
  // currentIndex is 0-based with welcome at 0, so this step is human step number currentIndex + 1.
  val stepNumber = progress.currentIndex + 1
  val filledStepCount = if (animation != null && animatedVisibilityScope != null) {
    // 1 while this step is fully on screen, 0 once it is gone; Nav3 moves this with the transition,
    // including a predictive-back gesture, so the shared bar follows the gesture frame for frame.
    val visibleAmount = animatedVisibilityScope.transition.animateFloat(
      transitionSpec = { tween(durationMillis = 300, easing = FastOutSlowInEasing) },
      label = "onboardingStepVisibleAmount",
    ) { state -> if (state == EnterExitState.Visible) 1f else 0f }
    val key = remember { Any() }
    DisposableEffect(animation, key) {
      onDispose { animation.removeStep(key) }
    }
    LaunchedEffect(animation, key, stepNumber) {
      snapshotFlow { visibleAmount.value }.collect { animation.setVisibleStep(key, stepNumber, it) }
    }
    animation.filledStepCount
  } else {
    // Isolated preview: no transition, so simply fill up to this step.
    stepNumber.toFloat()
  }
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
    repeat(progress.totalSteps) { index ->
      // Slice `index` fills as filledStepCount passes it: full once the count reaches the next whole
      // number, partially while it is crossing this slice. Dividers stay visible between slices.
      val sliceFill = (filledStepCount - index).coerceIn(0f, 1f)
      Box(
        Modifier
          .weight(1f)
          .height(4.dp)
          .clip(CircleShape)
          .background(HedvigTheme.colorScheme.surfaceSecondary),
      ) {
        if (sliceFill > 0f) {
          Box(
            Modifier
              .fillMaxWidth(sliceFill)
              .height(4.dp)
              .clip(CircleShape)
              .background(HedvigTheme.colorScheme.fillPrimary),
          )
        }
      }
    }
  }
}

@Composable
internal fun OnboardingStepHeader(title: String, modifier: Modifier = Modifier, description: String? = null) {
  Column(modifier.padding(horizontal = 16.dp)) {
    HedvigText(text = title)
    if (description != null) {
      Spacer(Modifier.height(4.dp))
      HedvigText(text = description, color = HedvigTheme.colorScheme.textSecondary)
    }
  }
}

/** Bottom-anchored button area with full-pill shapes. Primary and optional secondary buttons. */
@Composable
internal fun ColumnScope.OnboardingStepButtons(
  primaryText: String,
  onPrimaryClick: () -> Unit,
  primaryEnabled: Boolean = true,
  secondaryText: String? = null,
  onSecondaryClick: (() -> Unit)? = null,
  secondaryAbovePrimary: Boolean = false,
) {
  Spacer(Modifier.height(16.dp))
  val hapticFeedback = LocalHapticFeedback.current
  val primaryButton = @Composable {
    HedvigButton(
      text = primaryText,
      onClick = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        onPrimaryClick()
      },
      enabled = primaryEnabled,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .clip(CircleShape),
    )
  }
  val secondaryButton: (@Composable () -> Unit)? = if (secondaryText != null && onSecondaryClick != null) {
    @Composable {
      HedvigButton(
        text = secondaryText,
        onClick = {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
          onSecondaryClick()
        },
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .clip(CircleShape),
      )
    }
  } else {
    null
  }
  if (secondaryAbovePrimary && secondaryButton != null) {
    secondaryButton()
    Spacer(Modifier.height(8.dp))
    primaryButton()
  } else {
    primaryButton()
    if (secondaryButton != null) {
      Spacer(Modifier.height(8.dp))
      secondaryButton()
    }
  }
  Spacer(Modifier.height(16.dp))
  Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
}

@HedvigPreview
@Composable
private fun PreviewOnboardingStepScaffold() {
  HedvigTheme {
    OnboardingStepScaffold(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 2),
      showBackButton = true,
      onBackClick = {},
      onCloseClick = {},
    ) {
      OnboardingStepHeader(
        title = "Step title",
        description = "Step description with some helpful text.",
      )
      Spacer(Modifier.weight(1f))
      OnboardingStepButtons(
        primaryText = "Continue",
        onPrimaryClick = {},
        secondaryText = "Skip",
        onSecondaryClick = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingStepScaffoldNoProgress() {
  HedvigTheme {
    OnboardingStepScaffold(
      progress = null,
      showBackButton = false,
      onBackClick = {},
      onCloseClick = {},
    ) {
      OnboardingStepHeader(
        title = "Welcome",
        description = "Welcome to Hedvig onboarding.",
      )
      Spacer(Modifier.weight(1f))
      OnboardingStepButtons(
        primaryText = "Get started",
        onPrimaryClick = {},
      )
    }
  }
}
