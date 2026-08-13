package com.hedvig.android.feature.onboarding.ui.invite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingProgressBarAnimation
import com.hedvig.android.feature.onboarding.ui.OnboardingStepButtons
import com.hedvig.android.feature.onboarding.ui.OnboardingStepHeader
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
import hedvig.resources.ONBOARDING_INVITE_FRIEND_SUBTITLE
import hedvig.resources.ONBOARDING_INVITE_FRIEND_TITLE
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingInviteDestination(viewModel: OnboardingInviteViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingInviteScreen(
    uiState = uiState,
    progressAnimation = viewModel.progressBarAnimation,
    navigateUp = navigateUp,
    onClose = { viewModel.emit(OnboardingInviteEvent.Close) },
    onRetry = { viewModel.emit(OnboardingInviteEvent.Retry) },
    onContinue = { viewModel.emit(OnboardingInviteEvent.Continue) },
    onInviteFriend = { viewModel.emit(OnboardingInviteEvent.InviteFriend) },
    onInviteCardAnimationCompleted = { viewModel.emit(OnboardingInviteEvent.InviteCardAnimationCompleted) },
  )
}

@Composable
private fun OnboardingInviteScreen(
  uiState: OnboardingInviteUiState,
  progressAnimation: OnboardingProgressBarAnimation,
  navigateUp: () -> Unit,
  onClose: () -> Unit,
  onRetry: () -> Unit,
  onContinue: () -> Unit,
  onInviteFriend: () -> Unit,
  onInviteCardAnimationCompleted: () -> Unit,
) {
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingInviteUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = onClose,
    progressAnimation = progressAnimation,
  ) {
    when (uiState) {
      OnboardingInviteUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingInviteUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
        )
      }

      is OnboardingInviteUiState.Content -> {
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          title = stringResource(Res.string.ONBOARDING_INVITE_FRIEND_TITLE),
          description = stringResource(Res.string.ONBOARDING_INVITE_FRIEND_SUBTITLE, uiState.incentiveDisplay),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        // Outer box takes a fraction of the width so the card keeps side margins in portrait; the
        // inner cap keeps it from stretching (and its rows spreading) on wide, e.g. landscape, layouts.
        Box(
          modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(0.72f),
          contentAlignment = Alignment.Center,
        ) {
          ExampleReferralsCard(
            incentiveDisplay = uiState.incentiveDisplay,
            animationAlreadyPlayed = uiState.inviteCardAnimationPlayed,
            onAnimationCompleted = onInviteCardAnimationCompleted,
            modifier = Modifier
              .widthIn(max = 280.dp)
              .fillMaxWidth(),
          )
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        OnboardingStepButtons(
          primaryText = stringResource(Res.string.general_continue_button),
          onPrimaryClick = onContinue,
          secondaryText = stringResource(Res.string.ONBOARDING_INVITE_FRIEND_TITLE),
          secondaryAbovePrimary = true,
          onSecondaryClick = onInviteFriend,
        )
      }
    }
  }
}

@Composable
private fun ExampleReferralsCard(
  incentiveDisplay: String,
  animationAlreadyPlayed: Boolean,
  onAnimationCompleted: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // Illustrative, hardcoded example names showing what the referral list looks like once populated.
  val exampleNames = listOf("Hampus", "Li", "Elin")
  var visibleRows by remember { mutableIntStateOf(if (animationAlreadyPlayed) exampleNames.size else 1) }
  LaunchedEffect(Unit) {
    if (animationAlreadyPlayed) return@LaunchedEffect
    delay(RowRevealInitialDelayMillis.milliseconds)
    visibleRows = 2
    delay(RowRevealStaggerDelayMillis.milliseconds)
    visibleRows = 3
    onAnimationCompleted()
  }
  Surface(
    modifier = modifier.hedvigDropShadow(HedvigTheme.shapes.cornerLarge),
    shape = HedvigTheme.shapes.cornerLarge,
    color = HedvigTheme.colorScheme.backgroundPrimary,
    border = HedvigTheme.colorScheme.borderPrimary,
  ) {
    Column(Modifier.padding(horizontal = 16.dp)) {
      exampleNames.forEachIndexed { index, name ->
        if (index == 0) {
          ExampleReferralRow(name = name, incentiveDisplay = incentiveDisplay)
        } else {
          AnimatedVisibility(
            visible = visibleRows > index,
            // Expand from the top so the divider is unclipped from the first frames and fades in with
            // the row, rather than popping in when the clip reaches it at the end of the expansion.
            enter = expandVertically(rowRevealAnimationSpec(), expandFrom = Alignment.Top) +
              fadeIn(rowRevealAnimationSpec()),
          ) {
            ExampleReferralRow(
              name = name,
              incentiveDisplay = incentiveDisplay,
              modifier = Modifier.horizontalDivider(
                position = DividerPosition.Top,
                color = HedvigTheme.colorScheme.borderPrimary,
              ),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ExampleReferralRow(name: String, incentiveDisplay: String, modifier: Modifier = Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.padding(vertical = 16.dp),
  ) {
    Box(
      Modifier
        .size(16.dp)
        .clip(CircleShape)
        .background(HedvigTheme.colorScheme.signalGreenElement),
    )
    Spacer(Modifier.width(8.dp))
    // The name takes the remaining width so the incentive stays right-aligned and always fits, even
    // at large font scales where the name wraps, keeping the gap between them tight rather than
    // ballooning on wide (e.g. landscape) layouts.
    HedvigText(text = name, style = HedvigTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    Spacer(Modifier.width(8.dp))
    HedvigText(text = "-$incentiveDisplay", style = HedvigTheme.typography.bodySmall)
  }
}

private const val RowRevealInitialDelayMillis = 550L
private const val RowRevealStaggerDelayMillis = 1000L
private const val RowRevealDurationMillis = MotionTokens.DurationLong4.toInt()

private fun <T> rowRevealAnimationSpec(): TweenSpec<T> = tween(
  durationMillis = RowRevealDurationMillis,
  easing = MotionTokens.EasingEmphasizedDecelerateCubicBezier,
)

@HedvigPreview
@Composable
private fun PreviewOnboardingInviteScreen(
  @PreviewParameter(OnboardingInviteUiStateProvider::class) uiState: OnboardingInviteUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingInviteScreen(
        uiState = uiState,
        progressAnimation = remember { OnboardingProgressBarAnimation() },
        navigateUp = {},
        onClose = {},
        onRetry = {},
        onContinue = {},
        onInviteFriend = {},
        onInviteCardAnimationCompleted = {},
      )
    }
  }
}

private class OnboardingInviteUiStateProvider : CollectionPreviewParameterProvider<OnboardingInviteUiState>(
  listOf(
    OnboardingInviteUiState.Loading,
    OnboardingInviteUiState.Error,
    OnboardingInviteUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 4),
      incentiveDisplay = "10 kr",
      inviteCardAnimationPlayed = true,
    ),
  ),
)
