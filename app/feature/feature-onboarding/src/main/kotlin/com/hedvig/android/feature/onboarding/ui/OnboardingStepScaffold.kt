package com.hedvig.android.feature.onboarding.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.onboarding.data.OnboardingSession
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId

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

@Composable
internal fun OnboardingStepScaffold(
  progress: OnboardingProgress?,
  showBackButton: Boolean,
  onBackClick: () -> Unit,
  onCloseClick: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
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
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .padding(horizontal = 16.dp),
      ) {
        if (showBackButton) {
          IconButton(
            onClick = onBackClick,
          ) {
            Icon(
              imageVector = HedvigIcons.ArrowLeft,
              // TODO: Add "Go back" / "Gå tillbaka" to Lokalise (or reuse an existing a11y string)
              contentDescription = "Go back",
              modifier = Modifier.size(24.dp),
            )
          }
        } else {
          Spacer(Modifier.size(40.dp))
        }
        if (progress != null) {
          OnboardingProgressBar(
            progress = progress,
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
            // TODO: Add "Close" / "Stäng" to Lokalise (or reuse an existing a11y string)
            contentDescription = "Close",
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
private fun OnboardingProgressBar(progress: OnboardingProgress, modifier: Modifier = Modifier) {
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
    repeat(progress.totalSteps) { index ->
      val isActivated = index <= progress.currentIndex
      Box(
        Modifier
          .weight(1f)
          .height(2.dp)
          .clip(CircleShape)
          .background(
            if (isActivated) {
              HedvigTheme.colorScheme.fillPrimary
            } else {
              HedvigTheme.colorScheme.surfaceSecondary
            },
          ),
      )
    }
  }
}

@Composable
internal fun OnboardingStepHeader(title: String, description: String, modifier: Modifier = Modifier) {
  Column(modifier.padding(horizontal = 16.dp)) {
    HedvigText(text = title)
    HedvigText(text = description, color = HedvigTheme.colorScheme.textSecondary)
  }
}

/** Bottom-anchored button area: primary on top, optional ghost secondary below, matching Figma. */
@Composable
internal fun ColumnScope.OnboardingStepButtons(
  primaryText: String,
  onPrimaryClick: () -> Unit,
  primaryEnabled: Boolean = true,
  secondaryText: String? = null,
  onSecondaryClick: (() -> Unit)? = null,
) {
  Spacer(Modifier.weight(1f))
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = primaryText,
    onClick = onPrimaryClick,
    enabled = primaryEnabled,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  if (secondaryText != null && onSecondaryClick != null) {
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = secondaryText,
      onClick = onSecondaryClick,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
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
      OnboardingStepButtons(
        primaryText = "Get started",
        onPrimaryClick = {},
      )
    }
  }
}
